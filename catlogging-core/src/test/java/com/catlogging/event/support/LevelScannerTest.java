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
package com.catlogging.event.support;

import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.catlogging.event.Event;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Scanner.EventConsumer;
import static com.catlogging.event.support.LevelScanner.LevelComparatorType.*;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.SeverityLevel;
import com.catlogging.model.SeverityLevel.SeverityClassification;
import com.catlogging.model.support.DefaultPointer;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;

/**
 * Test for {@link LevelScanner}.
 * 
 * @author Tester
 * 
 */
public class LevelScannerTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testMatching() throws IOException, FormatException {
		final LevelScanner m = new LevelScanner();
		m.setComparator(EQ_OR_GREATER);
		m.setSeverityNumber(5); // e.g. WARN

		final DefaultPointer p = new DefaultPointer(0, 4);

		final Log log = Mockito.mock(Log.class);
		final LogRawAccess<LogInputStream> logAccess = Mockito.mock(LogRawAccess.class);
		final LogInputStream lis = Mockito.mock(LogInputStream.class);
		Mockito.when(logAccess.getFromJSON(Mockito.anyString())).thenAnswer(new Answer<LogPointer>() {
			@Override
			public LogPointer answer(final InvocationOnMock invocation) throws IOException {
				return DefaultPointer.fromJSON(invocation.getArguments()[0].toString());
			}
		});
		Mockito.when(lis.getPointer()).thenReturn(p);
		Mockito.when(logAccess.getInputStream(null)).thenReturn(lis);
		Mockito.when(log.getSize()).thenReturn(3l);
		final LogEntryReader<LogRawAccess<LogInputStream>> reader = Mockito.mock(LogEntryReader.class);
		final LogEntry entry1 = new LogEntry();
		entry1.setSeverity(new SeverityLevel("INFO", 3, SeverityClassification.INFORMATIONAL));
		entry1.setEndOffset(new DefaultPointer(2, 4));
		final LogEntry entry2 = new LogEntry();
		entry2.setRawContent("entry2-content");
		entry2.setSeverity(new SeverityLevel("WARN", 5, SeverityClassification.NOTICE));
		entry2.setStartOffset(new DefaultPointer(2, 4));
		entry2.setEndOffset(new DefaultPointer(3, 4));
		final LogEntry entry3 = new LogEntry();
		entry3.setSeverity(new SeverityLevel("ERROR", 6, SeverityClassification.ERROR));
		entry3.setEndOffset(new DefaultPointer(4, 4));

		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws IOException {
				((LogEntryConsumer) invocation.getArguments()[3]).consume(log, logAccess, entry1);
				return null;
			}
		}).when(reader).readEntries(Mockito.eq(log), Mockito.eq(logAccess), Mockito.isNull(LogPointer.class),
				Mockito.any(LogEntryConsumer.class));
		final IncrementData idata = new IncrementData();
		EventConsumer eventConsumer = Mockito.mock(EventConsumer.class);
		m.find(reader, Mockito.mock(LogEntryReaderStrategy.class), log, logAccess, idata, eventConsumer);
		Mockito.verifyZeroInteractions(eventConsumer);
		Assert.assertEquals(new DefaultPointer(2, 4), idata.getNextOffset());
		Assert.assertEquals(false, idata.getNextOffset(logAccess).isEOF());

		// Verify minBytesToRead
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws IOException {
				((LogEntryConsumer) invocation.getArguments()[3]).consume(log, logAccess, entry2);
				return null;
			}
		}).when(reader).readEntries(Mockito.eq(log), Mockito.eq(logAccess), Mockito.eq(new DefaultPointer(2, 4)),
				Mockito.any(LogEntryConsumer.class));

		eventConsumer = Mockito.mock(EventConsumer.class);
		final LogEntryReaderStrategy readerStrategy = Mockito.mock(LogEntryReaderStrategy.class);
		Mockito.when(readerStrategy.continueReading(log, logAccess, entry1)).thenReturn(true);
		Mockito.when(readerStrategy.continueReading(log, logAccess, entry2)).thenReturn(false);
		m.find(reader, readerStrategy, log, logAccess, idata, eventConsumer);
		Mockito.verify(eventConsumer, Mockito.times(1)).consume(Mockito.any(Event.class));
		Mockito.verify(eventConsumer, Mockito.times(1)).consume(Mockito.argThat(new BaseMatcher<Event>() {
			@Override
			public boolean matches(final Object arg0) {
				final Event event = (Event) arg0;
				return event.getEntries().size() == 1 && event.getEntries().get(0).equals(entry2);
			}

			@Override
			public void describeTo(final Description arg0) {
				// TODO Auto-generated method stub

			}
		}));
		Assert.assertEquals(false, idata.getNextOffset(logAccess).isEOF());
	}

	@Test
	public void testComparators() {
		// EQ
		Assert.assertEquals(true, EQ.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, EQ.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, EQ.matches(0, new SeverityLevel(null, 1, null)));

		// NEQ
		Assert.assertEquals(false, NEQ.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, NEQ.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, NEQ.matches(0, new SeverityLevel(null, 1, null)));

		// EQ_LESS
		Assert.assertEquals(true, EQ_OR_LESS.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, EQ_OR_LESS.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, EQ_OR_LESS.matches(0, new SeverityLevel(null, 1, null)));

		// LESS
		Assert.assertEquals(false, LESS.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, LESS.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, LESS.matches(0, new SeverityLevel(null, 1, null)));

		// GREATER
		Assert.assertEquals(false, GREATER.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, GREATER.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, GREATER.matches(0, new SeverityLevel(null, 1, null)));

		// EQ_GREATER
		Assert.assertEquals(true, EQ_OR_GREATER.matches(1, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(false, EQ_OR_GREATER.matches(2, new SeverityLevel(null, 1, null)));
		Assert.assertEquals(true, EQ_OR_GREATER.matches(0, new SeverityLevel(null, 1, null)));
	}
}
