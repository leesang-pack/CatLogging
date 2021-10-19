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
package com.catlogging.reader.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.catlogging.model.LogEntry;
import com.catlogging.model.support.ByteArrayLog;
import com.catlogging.model.support.ByteLogAccess;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.log4j.Log4jTextReader;
import com.catlogging.reader.log4j.Log4jTextReaderTest;

/**
 * Test for {@link BackwardReader}.
 * 
 * @author Tester
 * 
 */
public class BackwardReaderTest {
	private ByteArrayLog log;
	private Log4jTextReader fwReader;
	private BackwardReader<ByteLogAccess> revReader;
	private final String[] logLines = new String[] {
			"00:27:29,456 ERROR [com.catlogging.parser.log4j.Log4jParser] Prepared parsing pattern",
			"java.lang.Exception: kll",
			"at com.catlogging.parser.log4j.Log4jParser.setConversionPattern(Log4jParser.java:280)",
			"at com.catlogging.parser.log4j.Log4jParserTest.testParsingOneLineWithException(Log4jParserTest.java:44",
			"22:27:29,456 INFO  [com.catlogging.parser.log4j.Log4jParser] Unfinished",
			"22:37:29,456 INFO  [com.catlogging.parser.log4j.Log4jParser] Finished" };

	static {
		System.setProperty(AbstractPatternLineReader.PROP_catlogging_READER_MAX_MULTIPLE_LINES, "1");
	}

	@Before
	public void setUp() throws FormatException, UnsupportedEncodingException, IOException {
		fwReader = new Log4jTextReader("%d{ABSOLUTE} %-5p [%c] %m%n", "UTF-8");
		log = Log4jTextReaderTest.createLog(0, StringUtils.join(logLines, "\n"));
		revReader = new BackwardReader<ByteLogAccess>(fwReader);
	}

	@Test
	public void testRevReadingAtStart() throws FormatException, UnsupportedEncodingException, IOException {
		Assert.assertEquals(0, revReader.readEntries(log, log, log.createRelative(null, 0), -1).size());
	}

	@Test
	public void testRevReadingAllFromTail() throws FormatException, UnsupportedEncodingException, IOException {
		final List<LogEntry> entries = revReader.readEntries(log, log, log.createRelative(null, Long.MAX_VALUE), -10);
		Assert.assertEquals(3, entries.size());
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n" + logLines[2] + "\n" + logLines[3],
				entries.get(0).getRawContent());
		Assert.assertEquals(logLines[4], entries.get(1).getRawContent());
		Assert.assertEquals(logLines[5], entries.get(2).getRawContent());
	}

	@Test
	public void testRevReadingIncrementallyFromTail()
			throws FormatException, UnsupportedEncodingException, IOException {
		List<LogEntry> entries = revReader.readEntries(log, log, log.createRelative(null, Long.MAX_VALUE), -1);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(logLines[5], entries.get(0).getRawContent());

		// Next prev line
		entries = revReader.readEntries(log, log, entries.get(0).getStartOffset(), -1);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(logLines[4], entries.get(0).getRawContent());

		// Next prev line
		entries = revReader.readEntries(log, log, entries.get(0).getStartOffset(), -1);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(logLines[0] + "\n" + logLines[1] + "\n" + logLines[2] + "\n" + logLines[3],
				entries.get(0).getRawContent());

		// Start reached
		Assert.assertEquals(0, revReader.readEntries(log, log, entries.get(0).getStartOffset(), -1).size());
	}

	@Test
	public void testRecursionBug() throws FormatException, UnsupportedEncodingException, IOException {
		log = Log4jTextReaderTest.createLog(0, StringUtils.repeat(StringUtils.repeat("abc", 72) + "\n", 1000));
		final Log4jTextReader log4jReader = new Log4jTextReader("unknown format", "UTF-8");
		log4jReader.setMaxUnfomattedLines(1);
		final BackwardReader<ByteLogAccess> reader = new BackwardReader<ByteLogAccess>(log4jReader);
		final List<LogEntry> entries = reader.readEntries(log, log, log.createRelative(null, 52000), -100);
		Assert.assertEquals(100, entries.size());
	}
}
