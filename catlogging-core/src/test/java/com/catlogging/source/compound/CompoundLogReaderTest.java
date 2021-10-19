package com.catlogging.source.compound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.event.Event;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.SeverityLevel;
import com.catlogging.model.support.ByteArrayLog;
import com.catlogging.model.support.DefaultPointer;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;
import com.catlogging.reader.support.BufferedConsumer;

/**
 * Test for {@link CompositionReader}.
 * 
 * @author Tester
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
public class CompoundLogReaderTest {
	private static Logger logger = LoggerFactory.getLogger(CompoundLogReaderTest.class);

	private static final class DummySubReader implements LogEntryReader<LogRawAccess<LogInputStream>> {
		private final int maxCount;
		private final double factor;
		private final int start;
		private final int exceptionAt;

		public DummySubReader(final int maxCount, final float factor, final int start) {
			this(maxCount, factor, start, -1);
		}

		public DummySubReader(final int maxCount, final double factor, final int start, final int exceptionAt) {
			super();
			this.maxCount = maxCount;
			this.factor = factor;
			this.start = start;
			this.exceptionAt = exceptionAt;
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			return null;
		}

		@Override
		public void readEntries(final Log log, final LogRawAccess<LogInputStream> logAccess,
				final LogPointer startOffset, final com.catlogging.reader.LogEntryReader.LogEntryConsumer consumer)
				throws IOException, FormatException {
			for (int i = 0; i < maxCount; i++) {
				if (exceptionAt == i) {
					throw new IOException("Throw error at " + i);
				}
				final LogEntry entry = new LogEntry();
				entry.setStartOffset(new DefaultPointer(i, maxCount));
				entry.setEndOffset(new DefaultPointer(i + 1, maxCount));
				entry.setTimeStamp(new Date((long) (i * factor + start)));
				consumer.consume(log, logAccess, entry);
			}
		}

		@Override
		public List<SeverityLevel> getSupportedSeverities() {
			return null;
		}

		@Override
		public void readEntriesReverse(final Log log, final LogRawAccess<LogInputStream> logAccess,
				final LogPointer startOffset, final com.catlogging.reader.LogEntryReader.LogEntryConsumer consumer)
				throws IOException {
			for (int i = maxCount - 1; i >= 0; i--) {
				if (exceptionAt == i) {
					throw new IOException("Throw error at " + i);
				}
				final LogEntry entry = new LogEntry();
				entry.setStartOffset(new DefaultPointer(i, maxCount));
				entry.setEndOffset(new DefaultPointer(i + 1, maxCount));
				entry.setTimeStamp(new Date((long) (i * factor + start)));
				consumer.consume(log, logAccess, entry);
			}
		}

	};

	@Test
	public void testCorrectComposition() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompoundLogReader r = new CompoundLogReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(200, 0.5f, 0)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(250, 0.5f, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		r.readEntries(Mockito.mock(Log.class), new CompoundLogAccess(Mockito.mock(Log.class), subLogs), null, c);

		Assert.assertEquals(450, c.getBuffer().size());
		for (int i = 0; i < 400; i++) {
			final LogEntry e = c.getBuffer().get(i);
			if (i < 2) {
				Assert.assertEquals("Error at entry " + i, 0, e.getTimeStamp().getTime());
			} else {
				Assert.assertEquals("Error at entry " + i, 1 + (i - 2) / 4, e.getTimeStamp().getTime());
			}
			if ((i % 4 == 2 || i % 4 == 3 || i < 4) && i < 398) {
				Assert.assertEquals("Error at entry " + i, 1l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log1", e.get(Event.FIELD_LOG_PATH));
			} else if (i % 4 == 0 || i % 4 == 1) {
				Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
			}
		}
		for (int i = 400; i < c.getBuffer().size(); i++) {
			final LogEntry e = c.getBuffer().get(i);
			Assert.assertEquals((long) ((i - 400) * 0.5 + 101), e.getTimeStamp().getTime());
			Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
			Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
		}
	}

	@Test
	public void testCorrectCompositionReverse() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompoundLogReader r = new CompoundLogReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(200, 2, 0)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(250, 2, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		r.readEntriesReverse(Mockito.mock(Log.class), new CompoundLogAccess(Mockito.mock(Log.class), subLogs), null, c);

		Assert.assertEquals(450, c.getBuffer().size());
		for (int i = 0; i < 50; i++) {
			final LogEntry e = c.getBuffer().get(i);
			Assert.assertEquals(499 - (i * 2), e.getTimeStamp().getTime());
			Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
			Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
		}
		for (int i = 50; i < 450; i++) {
			final LogEntry e = c.getBuffer().get(i);
			Assert.assertEquals(449 - i, e.getTimeStamp().getTime());
			if (i % 2 == 1) {
				Assert.assertEquals("Error at entry " + i, 1l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log1", e.get(Event.FIELD_LOG_PATH));
			} else if (i % 2 == 0) {
				Assert.assertEquals("Error at entry " + i, 2l, e.get(Event.FIELD_SOURCE_ID));
				Assert.assertEquals("Error at entry " + i, "log2", e.get(Event.FIELD_LOG_PATH));
			}
		}
	}

	@Test
	public void testErrorInOneSource() throws FormatException, IOException {
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompoundLogReader r = new CompoundLogReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(200, 2, 0, 100)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(250, 2, 1)));
		final BufferedConsumer c = new BufferedConsumer(15000);
		try {
			r.readEntries(Mockito.mock(Log.class), new CompoundLogAccess(Mockito.mock(Log.class), subLogs), null, c);
		} catch (final IOException e) {
			Assert.assertTrue("Actual buffer size: " + c.getBuffer().size(), c.getBuffer().size() < 200);
			return;
		}
		Assert.fail("Exception expected");
	}

	/**
	 * Composes 45 million entries.
	 * 
	 * @throws FormatException
	 * @throws IOException
	 */
	@Test(timeout = 1000 * 100 * 100)
	@Repeat(1)
	public void testLongComposition() throws FormatException, IOException {
		final long start = System.currentTimeMillis();
		final List<LogInstance> subLogs = new ArrayList<>();
		final CompoundLogReader r = new CompoundLogReader(subLogs);
		final Log log1 = new ByteArrayLog("log1", new byte[0]);
		final Log log2 = new ByteArrayLog("log2", new byte[0]);
		subLogs.add(new LogInstance(1, log1, Mockito.mock(LogRawAccess.class), new DummySubReader(20000000, 2, 0)));
		subLogs.add(new LogInstance(2, log2, Mockito.mock(LogRawAccess.class), new DummySubReader(25000000, 2, 1)));
		final AtomicInteger count = new AtomicInteger();
		r.readEntries(Mockito.mock(Log.class), new CompoundLogAccess(Mockito.mock(Log.class), subLogs), null,
				new LogEntryConsumer() {
					@Override
					public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
							throws IOException {
						count.incrementAndGet();
						return true;
					}
				});
		final long end = System.currentTimeMillis() - start;
		logger.info("Read composed {} entries in {}ms, throughput: {} entries/s", count.get(), end,
				Math.round((double) count.get() / (end / 1000)));
		Assert.assertEquals(45000000, count.get());

	}
}
