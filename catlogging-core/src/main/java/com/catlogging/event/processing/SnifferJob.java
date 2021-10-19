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
package com.catlogging.event.processing;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.app.ContextProvider;
import com.catlogging.event.Event;
import com.catlogging.event.EventPersistence;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Publisher;
import com.catlogging.event.Publisher.PublishException;
import com.catlogging.event.Scanner.EventConsumer;
import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferScheduler;
import com.catlogging.event.SnifferScheduler.ScheduleInfo;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.util.StatisticsLogger;
import com.catlogging.util.sql.TxExecutor;
import com.catlogging.util.sql.TxExecutor.Execution;
import com.catlogging.util.sql.TxExecutor.TxNestedException;

/**
 * Quartz job for sniffing logs. Each scheduled jobs relates to a sniffer and a
 * {@link LogSource}.
 * 
 * @author Tester
 * 
 */
@Component
@DisallowConcurrentExecution
public class SnifferJob implements ContextAwareJob, InterruptableJob {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final static Logger statisticsLogger = LoggerFactory.getLogger(StatisticsLogger.class);

	private final InterruptionStatus interruption = new InterruptionStatus();

	@Override
	public final void execute(final JobExecutionContext jobCtx) throws JobExecutionException {
		ContextProvider.getContext().getBean(SnifferJob.class).executeInContext(jobCtx, interruption);

	}

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private LogSourceProvider logSourceProvider;

	@Autowired
	private EventPersistence eventPersister;

	@Autowired
	private TxExecutor txExecutor;

	@Autowired
	private SnifferScheduler snifferManager;

	@Autowired
	private ScheduleInfoAccess scheduleInfoAccess;

	@Override
	public void executeInContext(final JobExecutionContext jobCtx, final InterruptionStatus interruption)
			throws JobExecutionException {
		final long snifferId = SnifferJobManager.getSnifferId(jobCtx.getJobDetail().getKey());
		final long logSourceId = SnifferJobManager.getLogSourceId(jobCtx.getJobDetail().getKey());
		final ScheduleInfo scheduleInfo = scheduleInfoAccess.getScheduleInfo(snifferId);
		scheduleInfo.setLastFireTime(new Date());
		scheduleInfoAccess.updateScheduleInfo(snifferId, scheduleInfo);
		logger.debug("Start sniffing job processing for sniffer with id {} and log source {}", snifferId, logSourceId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			logger.error("Sniffer not found for id {}, stopping cron job for log source {}", snifferId, logSourceId);
			deleteJob(jobCtx.getScheduler(), snifferId);
			return;
		}
		final LogSource<LogRawAccess<? extends LogInputStream>> logSource = logSourceProvider
				.getSourceById(logSourceId);
		if (logSource == null) {
			logger.error("Log source not found for id {}, stopping cron job for sniffer {}", logSourceId, snifferId);
			deleteJob(jobCtx.getScheduler(), snifferId);
			return;
		}
		try {
			sniff(sniffer, logSource, interruption);
		} catch (final Exception e) {
			logger.error("Failed sniffing in context of sniffer={} and log source={}", sniffer, logSource);
			throw new JobExecutionException("Failed sniffing", e, false);
		} finally {
			logger.debug("Stopped sniffing job processing for sniffer with id {} and log source {}", snifferId,
					logSourceId);
		}
	}

	protected void deleteJob(final Scheduler scheduler, final long snifferId) throws JobExecutionException {
		try {
			snifferManager.stopSniffing(snifferId);
		} catch (final SchedulerException e) {
			throw new JobExecutionException("Failed to stop cron job for sniffer " + snifferId, e, false);
		}
	}

	protected void sniff(final Sniffer sniffer,
			final LogSource<? extends LogRawAccess<? extends LogInputStream>> source,
			final InterruptionStatus interruption) throws IOException, ParseException, PublishException {
		for (final Log log : source.getLogs()) {
			try {
				sniff(sniffer, source, log, interruption);
			} catch (final Exception e) {
				logger.error(
						"Failed sniffing log={} in context of sniffer={} and log source={}, continue with further logs",
						log, sniffer, source, e);
			}
		}
	}

	/**
	 * Container for sniff statistics.
	 * 
	 * @author Tester
	 * 
	 */
	private static final class SniffStatistics {
		long entriesCount = 0;
		long eventsCount = 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void sniff(final Sniffer sniffer,
			final LogSource<? extends LogRawAccess<? extends LogInputStream>> source, final Log log,
			final InterruptionStatus interruption) throws IOException, FormatException, PublishException {
		logger.debug("Sniffing log={} in context of sniffer={} and log source={}", log, sniffer, source);
		final IncrementData incData = snifferPersistence.getIncrementData(sniffer, source, log);
		final long startTime = System.currentTimeMillis();
		final SniffStatistics stats = new SniffStatistics();
		final LogRawAccess<LogInputStream> logAccess = (LogRawAccess<LogInputStream>) source.getLogAccess(log);
		final LogPointer startPointer = incData.getNextOffset(logAccess);
		sniffer.getReaderStrategy().reset(log, logAccess, startPointer);
		sniffer.getScanner().find((LogEntryReader) source.getReader(), new LogEntryReaderStrategy() {

			@Override
			public void reset(final Log log, final LogPointerFactory pointerFactory, final LogPointer start)
					throws IOException {
				sniffer.getReaderStrategy().reset(log, pointerFactory, start);
			}

			@Override
			public boolean continueReading(final Log log, final LogPointerFactory pointerFactory,
					final LogEntry currentReadEntry) throws IOException {
				stats.entriesCount++;
				if (interruption.isInterrupted()) {
					logger.info("Interrupted reader for sniffer {} and log {}", sniffer, log);
					return false;
				}
				return sniffer.getReaderStrategy().continueReading(log, pointerFactory, currentReadEntry);
			}
		}, log, logAccess, incData, new EventConsumer() {
			@Override
			public void consume(final Event event) throws IOException {
				stats.eventsCount++;
				event.setSnifferId(sniffer.getId());
				event.setLogSourceId(source.getId());
				event.setLogPath(log.getPath());
				event.setPublished(new Date());
				try {
					txExecutor.execute(new Execution<Object>() {
						@Override
						public Object execute() throws TxNestedException {
							event.setId(eventPersister.persist(event));
							snifferPersistence.storeIncrementalData(sniffer, source, log, incData);
							for (final Publisher publisher : sniffer.getPublishers()) {
								logger.debug("Publishing event={} to publisher: {}", event, publisher);
								try {
									publisher.publish(event);
								} catch (final PublishException e) {
									logger.error("Failed to publish event={} to publisher: {}", event, publisher, e);
								}
							}
							return null;
						}
					});
				} catch (final TxNestedException e) {
					throw new IOException("Failed to store sniffing data", e);
				}
			}
		});
		snifferPersistence.storeIncrementalData(sniffer, source, log, incData);
		if (statisticsLogger.isInfoEnabled() && incData.getNextOffset() != null) {
			final long duration = System.currentTimeMillis() - startTime;
			final long amount = logAccess.getDifference(startPointer, incData.getNextOffset(logAccess));
			statisticsLogger.info("Statistics for sniffer '{}': {}ms, {} bytes, {} KB/s, {} entries, {} events",
					sniffer.getName(), duration, amount,
					duration > 0 ? ((double) amount / 1024 / duration * 1000) : "-", stats.entriesCount,
					stats.eventsCount);
		}
		logger.debug("Sniffing log={} in context of sniffer={} and log source={} finished", logAccess, sniffer, source);
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		interruption.setInterrupted(true);
	}

}
