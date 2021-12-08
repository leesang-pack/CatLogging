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
package com.catlogging.reader.log4j;

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.SeverityLevel.SeverityClassification;
import com.catlogging.model.support.*;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;
import com.catlogging.reader.support.BufferedConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for {@link Log4jParser}.
 * 
 * @author Tester
 * 
 */
@Slf4j
public class Log4jTextReaderTest {

	@Test
	public void testParsingConversionPattern() throws FormatException {
		final Log4jTextReader r = new Log4jTextReader();
		r.setFormatPattern("%d{ABSOLUTE} %-5p [%c] %m%n");
		r.setCharset("UTF-8");
		r.setSpecifiersFieldMapping(Collections.singletonMap("m", "Message"));
		final String[] fieldNames = r.getFieldTypes().keySet().toArray(new String[0]);
		Assert.assertEquals(7, fieldNames.length);
		Assert.assertEquals(LogEntry.FIELD_RAW_CONTENT, fieldNames[0]);
		Assert.assertEquals("d", fieldNames[1]);
		Assert.assertEquals("p", fieldNames[2]);
		Assert.assertEquals("c", fieldNames[3]);
		Assert.assertEquals("Message", fieldNames[4]);
		Assert.assertEquals(LogEntry.FIELD_TIMESTAMP, fieldNames[5]);
		Assert.assertEquals(LogEntry.FIELD_SEVERITY_LEVEL, fieldNames[6]);

		Assert.assertEquals(FieldBaseTypes.DATE, r.getFieldTypes().get(LogEntry.FIELD_TIMESTAMP));
		Assert.assertEquals(FieldBaseTypes.STRING, r.getFieldTypes().get(fieldNames[0]));
		Assert.assertEquals(FieldBaseTypes.STRING, r.getFieldTypes().get(fieldNames[1]));
		Assert.assertEquals(FieldBaseTypes.STRING, r.getFieldTypes().get(fieldNames[2]));
		Assert.assertEquals(FieldBaseTypes.STRING, r.getFieldTypes().get(fieldNames[3]));
		Assert.assertEquals(FieldBaseTypes.STRING, r.getFieldTypes().get(fieldNames[4]));
		Assert.assertEquals(FieldBaseTypes.DATE, r.getFieldTypes().get(fieldNames[5]));
		Assert.assertEquals(FieldBaseTypes.SEVERITY, r.getFieldTypes().get(fieldNames[6]));
	}

	public static LogEntry[] readEntries(final LogEntryReader<ByteLogAccess> reader, final ByteArrayLog log,
			final LogPointer start, final int size) throws IOException, FormatException {
		final BufferedConsumer c = new BufferedConsumer(size);
		reader.readEntries(log, log, start, c);
		return c.getBuffer().toArray(new LogEntry[0]);
	}

	@Test
	public void testParsingOneLine() throws FormatException, UnsupportedEncodingException, IOException, ParseException {
		final Log4jTextReader reader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		final String logLine1 = "00:27:29,456 DEBUG [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern";
		final ByteArrayLog log = createLog(0, logLine1);
		final LogPointer start = log.getInputStream(null).getPointer();
		final LogEntry[] entries = readEntries(reader, log, null, 1);

		Assert.assertEquals(1, entries.length);
		Assert.assertEquals(logLine1, entries[0].getRawContent());
		Assert.assertEquals("00:27:29,456", entries[0].getFields().get("d"));
		Assert.assertEquals(SeverityClassification.DEBUG, entries[0].getSeverity().getClassification());
		Assert.assertEquals(new SimpleDateFormat("HH:mm:ss,SSS").parse("00:27:29,456"), entries[0].getTimeStamp());
		Assert.assertEquals(0, log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals(logLine1.getBytes("UTF-8").length, log.getDifference(start, entries[0].getEndOffset()));
	}

	@Test
	public void testISO8601DateFormat()
			throws FormatException, UnsupportedEncodingException, IOException, ParseException {
		final Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] %m%n", "UTF-8");
		final String logLine1 = "2013-03-24 00:27:29,456 DEBUG [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern";
		final ByteArrayLog log = createLog(0, logLine1);
		final LogPointer start = log.getInputStream(null).getPointer();
		final LogEntry[] entries = readEntries(reader, log, null, 1);
		Assert.assertEquals(1, entries.length);
		Assert.assertEquals(logLine1, entries[0].getRawContent());
		Assert.assertEquals(SeverityClassification.DEBUG, entries[0].getSeverity().getClassification());
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").parse("2013-03-24 00:27:29,456"),
				entries[0].getTimeStamp());
		Assert.assertEquals(0, log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals(logLine1.getBytes("UTF-8").length, log.getDifference(start, entries[0].getEndOffset()));
	}

	@Test
	public void testParsingOneLineWithException()
			throws ParseException, UnsupportedEncodingException, IOException, FormatException {
		final Log4jTextReader reader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		final String[] logLines = new String[] {
				"00:27:29,456 ERROR [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern",
				"java.lang.Exception: kll",
				"at com.catlogging.parser.log4j.Log4jParser.setConversionPattern(Log4jParser.java:280)",
				"at com.catlogging.parser.log4j.Log4jParserTest.testParsingOneLineWithException(Log4jParserTest.java:44",
				"22:27:29,456 INFO  [com.catlogging.parser.log4j.Log4jParser] Finished" };
		final ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		final LogPointer start = log.getInputStream(null).getPointer();
		final LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n" + logLines[2] + "\n" + logLines[3],
				entries[0].getRawContent());
		Assert.assertEquals("Prepared parsing pattern\n" + logLines[1] + "\n" + logLines[2] + "\n" + logLines[3],
				entries[0].getFields().get("m"));
		Assert.assertEquals(SeverityClassification.ERROR, entries[0].getSeverity().getClassification());
		Assert.assertEquals(new SimpleDateFormat("HH:mm:ss,SSS").parse("00:27:29,456"), entries[0].getTimeStamp());
		Assert.assertEquals(0, log.getDifference(start, entries[0].getStartOffset()));
		Assert.assertEquals((logLines[0] + logLines[1] + logLines[2] + logLines[3]).getBytes("UTF-8").length + 4,
				log.getDifference(start, entries[0].getEndOffset()));

		// Check last entry
		Assert.assertEquals(logLines[4], entries[1].getRawContent());
		Assert.assertEquals(SeverityClassification.INFORMATIONAL, entries[1].getSeverity().getClassification());
		Assert.assertEquals(new SimpleDateFormat("HH:mm:ss,SSS").parse("22:27:29,456"), entries[1].getTimeStamp());
		Assert.assertEquals((logLines[0] + logLines[1] + logLines[2] + logLines[3]).getBytes("UTF-8").length + 4,
				log.getDifference(start, entries[1].getStartOffset()));
		Assert.assertEquals(StringUtils.join(logLines, "\n").getBytes("UTF-8").length,
				log.getDifference(start, entries[1].getEndOffset()));
	}

	@Test
	public void testOnlyOverflow() throws ParseException, UnsupportedEncodingException, IOException, FormatException {
		final Log4jTextReader reader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		final String[] logLines = new String[] { "java.lang.Exception: kll",
				"at com.catlogging.parser.log4j.Log4jParser.setConversionPattern(Log4jParser.java:280)",
				"at com.catlogging.parser.log4j.Log4jParserTest.testParsingOneLineWithException(Log4jParserTest.java:44",
				"22:27:29,456 INFO  [com.catlogging.parser.log4j.Log4jParser] Finished" };
		final ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		final LogPointer start = log.getInputStream(null).getPointer();
		final LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n" + logLines[2], entries[0].getRawContent());
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n" + logLines[2], entries[0].getFields().get("m"));
		Assert.assertNull(entries[0].getSeverity());
		Assert.assertNull(entries[0].getTimeStamp());
		Assert.assertEquals(0, log.getDifference(start, entries[0].getStartOffset()));
	}

	@Test
	public void testInvalidPattern() throws UnsupportedEncodingException, IOException, FormatException {
		final Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] (%t) %m%n", "UTF-8");
		final String[] logLines = {
				"2013-09-28 01:28:27,145 INFO  [org.jboss.resource.deployers.RARDeployment] (main) Required license terms exist, view vfszip:/D:/work/test3/selunit-cloud-backend-jboss/dev/jboss-5.1.0.GA/server/default/deploy/jboss-local-jdbc.rar/META-INF/ra.xml",
				"2013-09-28 01:28:27,193 INFO  [org.jboss.resource.deployers.RARDeployment] (main) Required license terms exist, view vfszip:/D:/work/test3/selunit-cloud-backend-jboss/dev/jboss-5.1.0.GA/server/default/deploy/jboss-xa-jdbc.rar/META-INF/ra.xml" };
		final ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		final LogEntry[] entries = readEntries(reader, log, null, 2);

		// Check error entry
		Assert.assertEquals(2, entries.length);
		Assert.assertEquals(logLines[0], entries[0].getRawContent());
		Assert.assertEquals(logLines[1], entries[1].getRawContent());
	}

	/**
	 * Tests matching bug caused by new line character caused by
	 * {@link LineInputStream}.
	 */
	@Test
	public void testInvalidPatternFromFile() throws UnsupportedEncodingException, IOException, FormatException {
		final Log4jTextReader reader = new Log4jTextReader("%d %-5p [%c] (%t) %m%n", "UTF-8");
		final File f = new File("src/test/resources/logs/nl-triming.txt");
		final LogEntry[] entries = readEntries(reader, new ByteArrayLog(FileUtils.readFileToByteArray(f)), null, 2000);

		Assert.assertEquals(46, entries.length);
	}

	@Test
	public void testReadingFirstLineOnlyWithUnmodifiedEndOffset()
			throws FormatException, UnsupportedEncodingException, IOException, ParseException {
		final String[] logLines = new String[] {
				"00:27:29,456 ERROR [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern",
				"22:27:29,456 INFO  [com.catlogging.parser.log4j.Log4jParser] Finished" };
		final ByteArrayLog log = createLog(0, StringUtils.join(logLines, "\n"));
		final LogPointer start = log.getInputStream(null).getPointer();
		final Log4jTextReader reader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		final List<LogEntry> entries = new ArrayList<>();
		reader.readEntries(log, log, start, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				entry.setRawContent(entry.getRawContent() + entry.getRawContent());
				Assert.assertNotNull(entry.getEndOffset());
				entry.setEndOffset(null);
				entries.add(entry);
				return false;
			}
		});
		Assert.assertEquals(1, entries.size());
		Assert.assertNull(entries.get(0).getEndOffset());
		Assert.assertEquals(logLines[0] + logLines[0], entries.get(0).getRawContent());
	}

	@Test(timeout = 200 * 1000)
	public void testPerformanceAndMultiThreading() throws IOException, ParseException {
		final Log4jTextReader reader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		final byte[] logLine = "00:27:29,456 DEBUG [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern\n"
				.getBytes();
		final ByteLogAccess mockAccess = Mockito.mock(ByteLogAccess.class);
		Mockito.when(mockAccess.createRelative(Mockito.any(LogPointer.class), Mockito.anyLong()))
				.thenAnswer(new Answer<LogPointer>() {
					@Override
					public LogPointer answer(final InvocationOnMock invocation) throws Throwable {
						final DefaultPointer from = (DefaultPointer) invocation.getArguments()[0];
						final long offset = (long) invocation.getArguments()[1];
						return new DefaultPointer(from == null ? offset : from.getOffset() + offset, Long.MAX_VALUE);
					}
				});
		Mockito.when(mockAccess.getInputStream(null)).thenReturn(new ByteLogInputStream() {
			int i = 0;

			@Override
			public LogPointer getPointer() throws IOException {
				return new DefaultPointer(i, Long.MAX_VALUE);
			}

			@Override
			public int read() throws IOException {
				return logLine[i++ % logLine.length];
			}
		});
		final int linesToRead = 100000;
		final long start = System.currentTimeMillis();
		final AtomicInteger count = new AtomicInteger(0);
		reader.readEntries(Mockito.mock(Log.class), mockAccess, null, new LogEntryConsumer() {
			@Override
			public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
					throws IOException {
				try {
					Thread.sleep(1);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				return count.incrementAndGet() < linesToRead;
			}
		});
		final long size = logLine.length * (count.get() - 1);
		final long time = System.currentTimeMillis() - start;
		log.info("Read {} lines {} total bytes in {}ms: {} bytes/s", count.get() - 1, size, time,
				size / time * 1000);
		Assert.assertEquals(linesToRead, count.get());
	}

	public static ByteArrayLog createLog(final long offest, final String lines)
			throws UnsupportedEncodingException, IOException {
		return new ByteArrayLog(lines.getBytes("UTF-8"));
	}
}
