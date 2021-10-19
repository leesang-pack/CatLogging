/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 
 *
 * catlogging is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * catlogging is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.catlogging.event.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.catlogging.aspect.AspectProvider;
import com.catlogging.aspect.PostAspectProvider;
import com.catlogging.aspect.sql.QueryAdaptor;
import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfigException;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Publisher;
import com.catlogging.event.Publisher.PublisherWrapper;
import com.catlogging.event.Scanner;
import com.catlogging.event.Scanner.LogEntryReaderStrategyWrapper;
import com.catlogging.event.Scanner.ScannerWrapper;
import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferScheduler.ScheduleInfo;
import com.catlogging.event.filter.FilteredScanner;
import com.catlogging.event.filter.FilteredScanner.FilteredScannerWrapper;
import com.catlogging.model.Log;
import com.catlogging.model.LogSource;
import com.catlogging.model.support.JsonLogPointer;
import com.catlogging.util.LazyList;
import com.catlogging.util.LazyList.ListFactory;
import com.catlogging.util.PageableResult;
import com.catlogging.util.messages.Message;
import com.catlogging.util.messages.Message.MessageType;

import net.sf.json.JSONObject;

/**
 * H2 persistence for sniffers.
 * 
 * @author Tester
 * 
 */
@Component
public class H2SnifferPersistence implements SnifferPersistence {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationEventPublisher appEventPublisher;

	/**
	 * Row mapper for sniffer's.
	 * 
	 * @author Tester
	 * 
	 */
	private class SnifferRowMapper implements RowMapper<AspectSniffer> {
		private static final String SQL_PROJECTION = "SELECT ID, NAME, SCANNER_CONFIG, READER_STRATEGY_CONFIG, PUBLISHERS_CONFIG, CRON_EXPR,"
				+ " SOURCE FROM SNIFFERS ORDER BY NAME";

		@Override
		public AspectSniffer mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final AspectSniffer sniffer = new AspectSniffer();
			sniffer.setId(rs.getLong("ID"));
			sniffer.setName(rs.getString("NAME"));
			sniffer.setScheduleCronExpression(rs.getString("CRON_EXPR"));
			sniffer.setLogSourceId(rs.getLong("SOURCE"));
			final String strategyConfigStr = rs.getString("READER_STRATEGY_CONFIG");
			sniffer.setReaderStrategy(new LogEntryReaderStrategyWrapper() {
				@Override
				public LogEntryReaderStrategy getWrapped() throws ConfigException {
					return configManager.createBeanFromJSON(LogEntryReaderStrategy.class, strategyConfigStr);
				}
			});
			final String scannerConfigStr = rs.getString("SCANNER_CONFIG");
			if (StringUtils.isNotEmpty(scannerConfigStr)) {
				sniffer.setScanner(new FilteredScannerWrapper() {

					@Override
					public FilteredScanner getWrapped() throws ConfigException {
						try {
							return configManager.createBeanFromJSON(FilteredScanner.class, scannerConfigStr);
						} catch (final ConfigException e) {
							logger.warn(
									"Failed to deserilize scanner as filtered scanner, try to map as common scanner: "
											+ scannerConfigStr,
									e);
							return new FilteredScanner(
									configManager.createBeanFromJSON(Scanner.class, scannerConfigStr));
						}
					}
				});

			}

			final String publishersConfigStr = rs.getString("PUBLISHERS_CONFIG");
			if (StringUtils.isNotEmpty(publishersConfigStr))

			{
				sniffer.setPublishers(new LazyList<Publisher>(new ListFactory<Publisher>() {
					@Override
					public List<Publisher> createList() {
						final List<Publisher> pubs = new ArrayList<Publisher>();
						try {
							final Publisher[] deserialized = objectMapper.readValue(publishersConfigStr,
									Publisher[].class);
							for (final Publisher p : deserialized) {
								pubs.add(p);
							}
						} catch (final IOException e) {
							throw new ConfigException("Failed to deserialize publishers", e);
						}
						return pubs;
					}
				}));
			}
			return sniffer;
		}
	}

	private class SnifferCreator implements PreparedStatementCreator {
		private static final String SQL_SET = "SNIFFERS SET NAME=?, CRON_EXPR=?, SOURCE=?, SCANNER_CONFIG=?, PUBLISHERS_CONFIG=?, READER_STRATEGY_CONFIG=?";
		private static final String SQL_INSERT = "INSERT INTO " + SQL_SET;
		private static final String SQL_UPDATE = "UPDATE " + SQL_SET + " WHERE ID=?";
		private final Sniffer sniffer;
		private final boolean insert;

		private SnifferCreator(final boolean insert, final Sniffer sniffer) {
			this.insert = insert;
			this.sniffer = sniffer;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement ps = con.prepareStatement(insert ? SQL_INSERT : SQL_UPDATE);
			try {
				int c = 1;
				ps.setString(c++, sniffer.getName());
				ps.setString(c++, sniffer.getScheduleCronExpression());
				ps.setLong(c++, sniffer.getLogSourceId());
				if (sniffer.getScanner() != null) {
					final String scannerJsonStr = configManager
							.saveBeanToJSON(ScannerWrapper.unwrap(sniffer.getScanner()));
					ps.setString(c++, scannerJsonStr);
				} else {
					ps.setString(c++, (String) null);
				}
				if (sniffer.getPublishers() != null) {
					final StringBuilder jsonArray = new StringBuilder("[");
					for (final Publisher pub : sniffer.getPublishers()) {
						if (jsonArray.length() > 1) {
							jsonArray.append(",");
						}
						jsonArray.append(configManager.saveBeanToJSON(PublisherWrapper.unwrap(pub)));
					}
					jsonArray.append("]");
					ps.setString(c++, jsonArray.toString());
				} else {
					ps.setString(c++, "[]");
				}
				if (sniffer.getReaderStrategy() != null) {
					ps.setString(c++, configManager
							.saveBeanToJSON(LogEntryReaderStrategyWrapper.unwrap(sniffer.getReaderStrategy())));
				} else {
					ps.setString(c++, (String) null);
				}
				if (!insert) {
					ps.setLong(c++, sniffer.getId());
				}
				return ps;
			} catch (final ConfigException e) {
				throw new SQLException("Not able to serialize config data", e);
			}
		}

	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public SnifferListBuilder getSnifferListBuilder() {
		return new SnifferListBuilder() {
			private AspectProvider<AspectSniffer, Integer> eventsCounter;

			private QueryAdaptor<AspectSniffer, ScheduleInfo> scheduleInfoAdaptor;

			@Override
			public PageableResult<AspectSniffer> list() {
				String query = SnifferRowMapper.SQL_PROJECTION;
				RowMapper<AspectSniffer> rowMapper = new SnifferRowMapper();
				List<Object> args = new ArrayList<Object>();
				if (eventsCounter instanceof QueryAdaptor) {
					final QueryAdaptor<AspectSniffer, Integer> outer = (QueryAdaptor<AspectSniffer, Integer>) eventsCounter;
					query = outer.getQuery(query);
					rowMapper = outer.getRowMapper(rowMapper);
					args = outer.getQueryArgs(args);
				}
				if (scheduleInfoAdaptor != null) {
					query = scheduleInfoAdaptor.getQuery(query);
					rowMapper = scheduleInfoAdaptor.getRowMapper(rowMapper);
					args = scheduleInfoAdaptor.getQueryArgs(args);
				}
				final List<AspectSniffer> sniffers = jdbcTemplate.query(query, args.toArray(new Object[args.size()]),
						rowMapper);
				final PageableResult<AspectSniffer> result = new PageableResult<SnifferPersistence.AspectSniffer>(-1,
						sniffers);
				try {
					if (eventsCounter instanceof PostAspectProvider) {
						((PostAspectProvider<AspectSniffer, Integer>) eventsCounter).injectAspect(sniffers);
					}
				} catch (final Exception e) {
					logger.error("Failed to access event count", e);
					result.getMessages()
							.add(new Message(MessageType.ERROR, "Failed to access event counts: " + e.getMessage()));
				}
				return result;
			}

			@Override
			public SnifferListBuilder withEventsCounter(final AspectProvider<AspectSniffer, Integer> eventsCounter) {
				this.eventsCounter = eventsCounter;
				return this;
			}

			@Override
			public SnifferListBuilder withScheduleInfo(final QueryAdaptor<AspectSniffer, ScheduleInfo> adaptor) {
				this.scheduleInfoAdaptor = adaptor;
				return this;
			}
		};
	}

	@Override
	public Sniffer getSniffer(final long id) {
		final List<AspectSniffer> sniffers = jdbcTemplate.query(
				"SELECT * FROM (" + SnifferRowMapper.SQL_PROJECTION + ") WHERE ID=?", new Object[] { id },
				new SnifferRowMapper());
		if (sniffers.size() == 1) {
			return sniffers.get(0);
		}
		return null;
	}

	@Override
	public IncrementData getIncrementData(final Sniffer sniffer, final LogSource<?> source, final Log log) {
		final List<IncrementData> idatas = jdbcTemplate.query(
				"SELECT NEXT_POINTER, DATA FROM SNIFFERS_SCANNER_IDATA WHERE SNIFFER=? AND SOURCE=? AND LOG=?",
				new Object[] { sniffer.getId(), source.getId(), log.getPath() }, new RowMapper<IncrementData>() {
					@Override
					public IncrementData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						final IncrementData data = new IncrementData();
						data.setData(JSONObject.fromObject(rs.getString("DATA")));
						final String jsonStr = rs.getString("NEXT_POINTER");
						if (StringUtils.isNotBlank(jsonStr)) {
							data.setNextOffset(new JsonLogPointer(jsonStr));
						}
						return data;
					}
				});
		if (idatas.size() == 0) {
			logger.debug("No increment data for sniffer={}, source={} and log={} found, create an empty one", sniffer,
					source, log);
			return new IncrementData();
		} else {
			return idatas.get(0);
		}
	}

	@Override
	public Map<Log, IncrementData> getIncrementDataByLog(final Sniffer sniffer, final LogSource<?> source)
			throws IOException {
		final List<Log> logs = source.getLogs();
		if (logs.size() > 0) {
			final HashMap<Log, IncrementData> incs = new HashMap<Log, IncrementData>();
			final HashMap<String, Log> logMapping = new HashMap<String, Log>();
			for (final Log log : logs) {
				logMapping.put(log.getPath(), log);
			}
			jdbcTemplate.query(
					"SELECT NEXT_POINTER, DATA, LOG FROM SNIFFERS_SCANNER_IDATA WHERE SNIFFER=? AND SOURCE=? AND LOG IN ("
							+ StringUtils.repeat("?", ",", logs.size()) + ") ORDER BY LOG",
					ArrayUtils.addAll(new Object[] { sniffer.getId(), source.getId() },
							logMapping.keySet().toArray(new Object[logMapping.size()])),
					new RowCallbackHandler() {
						@Override
						public void processRow(final ResultSet rs) throws SQLException {
							final String logPath = rs.getString("LOG");
							final Log log = logMapping.get(logPath);
							if (log != null) {
								final IncrementData data = new IncrementData();
								data.setData(JSONObject.fromObject(rs.getString("DATA")));
								try {
									final String jsonStr = rs.getString("NEXT_POINTER");
									if (StringUtils.isNotBlank(jsonStr)) {
										data.setNextOffset(source.getLogAccess(log).getFromJSON(jsonStr));
									}
									incs.put(log, data);
								} catch (final IOException e) {
									throw new SQLException("Failed to construct pointer in log: " + log, e);
								}
							} else {
								logger.error("Didn't find log '{}' for selected incrementdata", logPath);
							}
						}
					});
			// Create empty entries for not yet persisted
			for (final Log log : logMapping.values()) {
				if (!incs.containsKey(log)) {
					incs.put(log, new IncrementData());
				}
			}
			return incs;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public void storeIncrementalData(final Sniffer observer, final LogSource<?> source, final Log log,
			final IncrementData data) {
		final ArrayList<Object> args = new ArrayList<Object>();
		args.add(data.getNextOffset() != null ? data.getNextOffset().getJson() : "");
		args.add(data.getData().toString());
		args.add(observer.getId());
		args.add(source.getId());
		args.add(log.getPath());
		final Object[] a = args.toArray(new Object[args.size()]);
		logger.debug("Storing inc data for sniffer={}, source={} and log={} with next offset: {}", observer, source,
				log, data.getNextOffset());
		if (jdbcTemplate.update(
				"UPDATE SNIFFERS_SCANNER_IDATA SET NEXT_POINTER=?, DATA=? WHERE SNIFFER=? AND SOURCE=? AND LOG=?",
				a) == 0) {
			logger.debug("Inc data for sniffer={}, source={} and log={} doesn't exists, create it", observer, source,
					log);
			jdbcTemplate.update(
					"INSERT INTO SNIFFERS_SCANNER_IDATA SET NEXT_POINTER=?, DATA=?, SNIFFER=?, SOURCE=?, LOG=?", a);
		}
	}

	@Override
	public long createSniffer(final Sniffer sniffer) {
		final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new SnifferCreator(true, sniffer), keyHolder);
		final long snifferId = keyHolder.getKey().longValue();
		sniffer.setId(snifferId);
		logger.debug("Persisted new sniffer with id {}", snifferId);
		appEventPublisher.publishEvent(new SnifferChangedEvent(sniffer));
		return snifferId;
	}

	@Override
	public void updateSniffer(final Sniffer sniffer) {
		jdbcTemplate.update(new SnifferCreator(false, sniffer));
		logger.debug("Updated sniffer {}", sniffer.getId());
		appEventPublisher.publishEvent(new SnifferChangedEvent(sniffer));
	}

	@Override
	@Transactional
	public void deleteSniffer(final Sniffer sniffer) {
		jdbcTemplate.update("DELETE FROM SNIFFERS_SCHEDULE_INFO WHERE SNIFFER=?", sniffer.getId());
		jdbcTemplate.update("DELETE FROM SNIFFERS_SCANNER_IDATA WHERE SNIFFER=?", sniffer.getId());
		jdbcTemplate.update("DELETE FROM SNIFFERS_EVENTS WHERE SNIFFER=?", sniffer.getId());
		jdbcTemplate.update("DELETE FROM SNIFFERS WHERE ID=?", sniffer.getId());
		logger.info("Deleted sniffer for id: {}", sniffer.getId());
	}

}
