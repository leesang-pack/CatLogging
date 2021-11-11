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
package com.catlogging.event.processing;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.app.QaDataSourceAppConfig;
import com.catlogging.app.SchedulerAppConfig;
import com.catlogging.event.Event;
import com.catlogging.event.EventPersistence;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Publisher;
import com.catlogging.event.Scanner;
import com.catlogging.event.Scanner.EventConsumer;
import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferScheduler.ScheduleInfo;
import com.catlogging.event.filter.FilteredScanner;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.filter.FilteredLogEntryReader;
import com.catlogging.util.sql.DefaultTxExecutor;
import com.catlogging.util.sql.TxExecutor;

/**
 * Test for {@link SnifferJob} and {@link SnifferJobManager}.
 * 
 * @author Tester
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SnifferJobTest.class, CoreAppConfig.class, SchedulerAppConfig.class,
		QaDataSourceAppConfig.class })
@Configuration
public class SnifferJobTest {
	@Bean
	public SnifferJobManager jobManager() {
		return new SnifferJobManager();
	}

	@Bean
	public SnifferJob job() {
		return new SnifferJob();
	}

	@Bean
	public SnifferPersistence snifferPersistence() {
		return mock(SnifferPersistence.class);
	}

	@Bean
	public LogSourceProvider logSourceProvider() {
		return mock(LogSourceProvider.class);
	}

	@Bean
	public EventPersistence eventPersister() {
		return mock(EventPersistence.class);
	}

	@Bean
	public TxExecutor txExecutor() {
		return new DefaultTxExecutor();
	}

	@Bean
	public ScheduleInfoAccess scheduleInfoAccess() {
		return mock(ScheduleInfoAccess.class);
	}

	@Autowired
	private ScheduleInfoAccess scheduleInfoAccess;

	@Autowired
	private SnifferJobManager jobManager;

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private LogSourceProvider sourceProvider;

	@Autowired
	private Scheduler scheduler;

	@Test(expected = SchedulerException.class)
	@DirtiesContext
	public void testUnknownSniffer() throws Exception {
		jobManager.startSniffing(9889);
	}

	@Test
	@DirtiesContext
	public void testStoppingJobWhenResourcesNotFound() throws Exception {
		final Sniffer sniffer = new Sniffer();
		sniffer.setId(23);
		sniffer.setScheduleCronExpression("*/15 * * ? * * */50");
		sniffer.setLogSourceId(77);
		when(snifferPersistence.getSniffer(23)).thenReturn(sniffer);
		final ScheduleInfo info = new ScheduleInfo();
		when(scheduleInfoAccess.getScheduleInfo(23)).thenReturn(info);
		jobManager.startSniffing(sniffer.getId());
		Assert.assertEquals(true, info.isScheduled());
		Assert.assertNull(info.getLastFireTime());
		verify(scheduleInfoAccess).updateScheduleInfo(23, info);
		Assert.assertEquals(true, jobManager.isScheduled(sniffer.getId()));
		when(snifferPersistence.getSniffer(23)).thenReturn(null);
		scheduler.triggerJob(jobManager.getJobKey(sniffer, 77));
		Thread.sleep(2000);

		// Should be stopped
		verify(snifferPersistence, times(2)).getSniffer(23);
		verifyZeroInteractions(sourceProvider);
		Assert.assertFalse(scheduler.checkExists(jobManager.getJobKey(sniffer, 77)));

		Assert.assertEquals(false, info.isScheduled());
		Assert.assertNotNull(info.getLastFireTime());
		verify(scheduleInfoAccess, times(3)).updateScheduleInfo(23, info);

		// Pass sniffer, but no log source
		when(snifferPersistence.getSniffer(23)).thenReturn(sniffer);
		jobManager.startSniffing(sniffer.getId());
		scheduler.triggerJob(jobManager.getJobKey(sniffer, 77));
		Thread.sleep(2000);
		verify(snifferPersistence, times(4)).getSniffer(23);
		verify(sourceProvider, times(1)).getSourceById(77);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@DirtiesContext
	public void testProcessing() throws Exception {
		final Sniffer sniffer = new Sniffer();
		sniffer.setId(23);
		sniffer.setScheduleCronExpression("*/15 * * ? * * */50");
		sniffer.setLogSourceId(77);
		final Scanner scanner = mock(Scanner.class);
		sniffer.setScanner(new FilteredScanner(scanner));
		sniffer.setReaderStrategy(mock(LogEntryReaderStrategy.class));
		when(sniffer.getReaderStrategy().continueReading(any(Log.class), any(LogPointerFactory.class),
				any(LogEntry.class))).thenReturn(true);
		final Publisher publisher = mock(Publisher.class);
		sniffer.setPublishers(Collections.singletonList(publisher));

		when(snifferPersistence.getSniffer(23)).thenReturn(sniffer);
		final Log log1 = mock(Log.class);
		final LogRawAccess<LogInputStream> logAccess1 = mock(LogRawAccess.class);
		when(log1.getPath()).thenReturn("log1.log");
		final Log log2 = mock(Log.class);
		final LogRawAccess<LogInputStream> logAccess2 = mock(LogRawAccess.class);
		when(log2.getPath()).thenReturn("log2.log");
		final LogSource<LogRawAccess<LogInputStream>> source = mock(LogSource.class);
		when(source.getId()).thenReturn(77L);
		when(sourceProvider.getSourceById(77)).thenReturn((LogSource) source);
		when(source.getLogAccess(log1)).thenReturn(logAccess1);
		when(source.getLogAccess(log2)).thenReturn(logAccess2);
		final LogEntryReader<LogRawAccess<LogInputStream>> reader = mock(LogEntryReader.class);
		when(source.getReader()).thenReturn(new FilteredLogEntryReader<LogRawAccess<LogInputStream>>(reader, null));
		when(source.getLogs()).thenReturn(Arrays.asList(new Log[] { log1, log2 }));

		final IncrementData log1idata = new IncrementData();
		final IncrementData log2idata = new IncrementData();
		when(snifferPersistence.getIncrementData(sniffer, source, log1)).thenReturn(log1idata);
		when(snifferPersistence.getIncrementData(sniffer, source, log2)).thenReturn(log2idata);

		when(scheduleInfoAccess.getScheduleInfo(23)).thenReturn(new ScheduleInfo());
		jobManager.startSniffing(sniffer.getId());
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				final EventConsumer eConsumer = (EventConsumer) invocation.getArguments()[5];
				final Event e = new Event();
				eConsumer.consume(e);
				final IncrementData incData = (IncrementData) invocation.getArguments()[4];
				incData.setNextOffset(Mockito.mock(LogPointer.class));
				when(incData.getNextOffset().getJson()).thenReturn("last-pointer");
				return null;
			}
		}).when(scanner).find(any(FilteredLogEntryReader.class), any(LogEntryReaderStrategy.class), eq(log2),
				eq(logAccess2), eq(log2idata), any(EventConsumer.class));
		scheduler.triggerJob(jobManager.getJobKey(sniffer, 77));
		Thread.sleep(2000);
		verify(publisher).publish(argThat(new BaseMatcher<Event>() {
			@Override
			public boolean matches(final Object arg0) {
				final Event event = (Event) arg0;
				return event.getLogPath().equals("log2.log")
						&& System.currentTimeMillis() - event.getPublished().getTime() <= 5000
						&& event.getSnifferId() == sniffer.getId() && event.getLogSourceId() == source.getId();
			}

			@Override
			public void describeTo(final Description arg0) {
				// TODO Auto-generated method stub

			}
		}));
		verifyNoMoreInteractions(publisher);
		verify(snifferPersistence, times(1)).storeIncrementalData(sniffer, source, log1, log1idata);
		verify(snifferPersistence, times(2)).storeIncrementalData(sniffer, source, log2, log2idata);
		Assert.assertEquals("last-pointer", log2idata.getNextOffset().getJson());
		verify(sniffer.getReaderStrategy(), times(1)).reset(eq(log1), eq(logAccess1), any(LogPointer.class));
		verify(sniffer.getReaderStrategy(), times(1)).reset(eq(log2), eq(logAccess2), any(LogPointer.class));

		// Fail sniffing log1 to check continuous sniffing on log2
		doThrow(new IOException()).when(sniffer.getScanner().getTargetScanner()).find(eq(reader),
				any(LogEntryReaderStrategy.class), eq(log1), eq(logAccess1), eq(log1idata), any(EventConsumer.class));
		scheduler.triggerJob(jobManager.getJobKey(sniffer, 77));
		Thread.sleep(2000);
		verify(publisher, times(2)).publish(any(Event.class));

		// Reschedule the same again
		jobManager.startSniffing(sniffer.getId());
		// Stop sniffing
		jobManager.stopSniffing(sniffer.getId());
		Assert.assertEquals(false, scheduler.checkExists(jobManager.getJobKey(sniffer, 77)));

	}
}
