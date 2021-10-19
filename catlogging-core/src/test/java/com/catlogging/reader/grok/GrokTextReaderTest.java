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
package com.catlogging.reader.grok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.model.LogEntry;
import com.catlogging.model.support.ByteArrayLog;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.log4j.Log4jTextReaderTest;
import com.catlogging.reader.support.AbstractPatternLineReader;
import com.catlogging.util.grok.GrokAppConfig;
import com.catlogging.util.grok.GrokConsumerConstructor;
import com.catlogging.util.grok.GroksRegistry;

/**
 * Test for {@link GrokTextReader}.
 * 
 * @author Tester
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { GrokAppConfig.class, GrokTextReaderTest.class, CoreAppConfig.class })
@Configuration
public class GrokTextReaderTest {

	@Bean
	public GrokConsumerConstructor grokReaderConstructor() {
		return new GrokConsumerConstructor();
	}

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private GroksRegistry grokRegistry;

	private GrokTextReader grokReader;

	static {
		System.setProperty(AbstractPatternLineReader.PROP_catlogging_READER_MAX_MULTIPLE_LINES, "3");
	}

	@Before
	public void initReader() {
		grokReader = new GrokTextReader();
		grokReader.initGrokFactory(grokRegistry);
		grokReader.getGrokBean().setPattern("%{SYSLOGBASE}-my");
		grokReader.setOverflowAttribute("msg");
		final String jsonStr = configManager.saveBeanToJSON(grokReader);
		grokReader = configManager.createBeanFromJSON(GrokTextReader.class, jsonStr);
	}

	@Test
	public void testReader() throws IOException, FormatException {
		assertEquals("%{SYSLOGBASE}-my", grokReader.getGrokBean().getPattern());
		assertEquals("msg", grokReader.getOverflowAttribute());
		final String logLine1 = "Nov  1 21:14:23 <1.3> localhost kernel:-my";
		final ByteArrayLog log = Log4jTextReaderTest.createLog(0, logLine1 + "\noverflow");
		grokReader.setOverflowAttribute(null);
		LogEntry[] entries = Log4jTextReaderTest.readEntries(grokReader, log, null, 1);
		assertEquals(1, entries.length);
		assertEquals("Nov  1 21:14:23", entries[0].get("timestamp"));
		assertEquals(1, entries[0].get("facility"));
		assertEquals(3, entries[0].get("priority"));
		assertEquals("localhost", entries[0].get("logsource"));
		assertEquals("kernel", entries[0].get("program"));
		assertNull(entries[0].get("msg"));

		// Test overflow attachment to "msg" field
		grokReader.setOverflowAttribute("msg");
		entries = Log4jTextReaderTest.readEntries(grokReader,
				Log4jTextReaderTest.createLog(0, logLine1 + "\noverflow1\noverflow2"), null, 1);
		assertEquals(1, entries.length);
		assertEquals("Nov  1 21:14:23", entries[0].get("timestamp"));
		assertEquals(1, entries[0].get("facility"));
		assertEquals(3, entries[0].get("priority"));
		assertEquals("localhost", entries[0].get("logsource"));
		assertEquals("kernel", entries[0].get("program"));
		assertEquals("overflow1\noverflow2", entries[0].get("msg"));

		// Test overflow to existing field "program"
		grokReader.setOverflowAttribute("program");
		entries = Log4jTextReaderTest
				.readEntries(grokReader,
						Log4jTextReaderTest.createLog(0,
								logLine1 + "\noverflow\nline2\nline3\nline4\nline5\nline6\nline7\n" + logLine1),
						null, 5);
		assertEquals(4, entries.length);
		assertEquals(false, entries[0].isUnformatted());
		assertEquals(true, entries[1].isUnformatted());
		assertEquals(true, entries[2].isUnformatted());
		assertEquals(false, entries[3].isUnformatted());
		assertEquals("kernel\noverflow\nline2\nline3", entries[0].get("program"));
		assertEquals("line4\nline5\nline6", entries[1].get("program"));
		assertEquals("line7", entries[2].get("program"));
		assertEquals("kernel", entries[3].get("program"));
	}

	@Test
	public void testDeserializationFromUnrwappedGrokPatternBean() {
		final String json = "{\"@type\":\"GrokTextReader\",\"charset\":\"UTF-8\",\"pattern\":\"%{SYSLOGBASE}-my\",\"subStringSearch\":false,\"multiLine\":false,\"dotAll\":false,\"caseInsensitive\":false,\"overflowAttribute\":\"msg\",\"fieldTypes\":{\"lf_raw\":\"STRING\",\"timestamp\":\"STRING\",\"facility\":\"INTEGER\",\"priority\":\"INTEGER\",\"logsource\":\"STRING\",\"program\":\"STRING\",\"pid\":\"STRING\",\"msg\":\"STRING\"}}";
		final GrokTextReader reader = configManager.createBeanFromJSON(GrokTextReader.class, json);
		Assert.assertEquals("%{SYSLOGBASE}-my", reader.getGrokBean().getPattern());
		Assert.assertEquals(false, reader.getGrokBean().isCaseInsensitive());
		Assert.assertEquals(false, reader.getGrokBean().isMultiLine());
		Assert.assertEquals(false, reader.getGrokBean().isDotAll());
		Assert.assertEquals(false, reader.getGrokBean().isSubStringSearch());
	}
}
