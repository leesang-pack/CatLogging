/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
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
package com.catlogging.model.h2;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfigException;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSource.LogSourceWrapper;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.model.support.BaseLogsSource;
import com.catlogging.util.excption.ReferenceIntegrityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;

/**
 * H2 based source provider.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class H2LogSourceProvider implements LogSourceProvider {

	@Autowired
	private BeanConfigFactoryManager configManager;

	private class LogSourceCreator implements PreparedStatementCreator {
		private static final String SQL_INSERT = "INSERT INTO LOG_SOURCES (NAME, CONFIG) VALUES(?,?)";
		private static final String SQL_UPDATE = "UPDATE LOG_SOURCES SET NAME=?, CONFIG=? WHERE ID=?";
		private final LogSource<? extends LogRawAccess<? extends LogInputStream>> source;
		private final boolean insert;

		private LogSourceCreator(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source,
				final boolean insert) {
			this.source = source;
			this.insert = insert;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			try {
				final PreparedStatement ps = con.prepareStatement(insert ? SQL_INSERT : SQL_UPDATE, Statement.RETURN_GENERATED_KEYS);
				int c = 1;
				ps.setString(c++, source.getName());
				ps.setString(c++, configManager.saveBeanToJSON(source));
				if (!insert) {
					ps.setLong(c++, source.getId());
				}
				return ps;
			} catch (final ConfigException e) {
				throw new SQLException("Not able to serialize config data", e);
			}
		}
	}

	/**
	 * Row mapper for log sources.
	 * 
	 * @author Tester
	 * 
	 */
	private class SourceRowMapper implements RowMapper<LogSource<LogRawAccess<? extends LogInputStream>>> {
		private static final String SQL_PROJECTION = "SELECT ID, NAME, CONFIG FROM LOG_SOURCES";

		@Override
		public LogSource<LogRawAccess<? extends LogInputStream>> mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			final long id = rs.getLong("ID");
			final String name = rs.getString("NAME");
			final String config = rs.getString("CONFIG");
			final LogSource<LogRawAccess<? extends LogInputStream>> source = new LogSourceWrapper() {
				@SuppressWarnings("unchecked")
				@Override
				public LogSource<LogRawAccess<? extends LogInputStream>> getWrapped() {
					try {
						final BaseLogsSource<LogRawAccess<? extends LogInputStream>> wrapped = (BaseLogsSource<LogRawAccess<? extends LogInputStream>>) configManager
								.createBeanFromJSON(LogSource.class, config);
						wrapped.setId(id);
						return wrapped;
					} catch (final ConfigException e) {
						log.error("Failed to deserialize log source: " + id, e);
						return LogSource.NULL_SOURCE;
					}
				}

				@Override
				public long getId() {
					return id;
				}

				@Override
				public String getName() {
					return name;
				}
			};
			return source;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public long createSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) {
		final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new LogSourceCreator(source, true), keyHolder);
		final long id = keyHolder.getKey().longValue();
		return id;
	}

	@Override
	public List<LogSource<LogRawAccess<? extends LogInputStream>>> getSources() {
		return jdbcTemplate.query(SourceRowMapper.SQL_PROJECTION + " ORDER BY NAME", new SourceRowMapper());
	}

	@Override
	public LogSource<LogRawAccess<? extends LogInputStream>> getSourceById(final long id) {
		final List<LogSource<LogRawAccess<? extends LogInputStream>>> sources = jdbcTemplate
				.query(SourceRowMapper.SQL_PROJECTION + " WHERE ID=?", new Object[] { id }, new SourceRowMapper());
		return sources.size() > 0 ? sources.get(0) : null;
	}

	@Override
	public void updateSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) {
		jdbcTemplate.update(new LogSourceCreator(source, false));
	}

	@Override
	public void deleteSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source)
			throws ReferenceIntegrityException {
		try {
			jdbcTemplate.update("DELETE FROM LOG_SOURCES WHERE ID=?", source.getId());
			log.info("Deleted source with id: {}", source.getId());
		} catch (final DataIntegrityViolationException e) {
			log.info("Deleting source with id {} failed due to references", source.getId());
			throw new ReferenceIntegrityException(LogSource.class, e);
		}
	}

}
