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
package com.catlogging.model.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfigException;
import com.catlogging.model.Log;
import com.catlogging.model.LogRawAccessor;
import com.catlogging.model.file.AbstractTimestampRollingLogsSource.PastLogsType;
import com.catlogging.model.support.ByteLogAccess;
import com.catlogging.model.support.DailyRollingLog;
import com.catlogging.model.support.DailyRollingLogAccess;
import com.catlogging.reader.filter.FilteredLogEntryReader;
import com.catlogging.reader.log4j.Log4jTextReader;

/**
 * Test for {@link RollingLogsSource}.
 * 
 * @author Tester
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class, RollingLogSourceTest.class })
@Configuration
public class RollingLogSourceTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BeanConfigFactoryManager configManager;

	private File logDir;

	@Before
	public void setUp() throws IOException, InterruptedException {
		logDir = new File(File.createTempFile("log", "log").getPath() + "dir");
		logDir.mkdirs();
		FileUtils.writeStringToFile(new File(logDir, "server.log"), "live\n");
		FileUtils.writeStringToFile(new File(logDir, "server.log.2013-03-27"), "log from 2013-03-27\n");
		Thread.sleep(1200);
		FileUtils.writeStringToFile(new File(logDir, "server.log.2013-03-26"), "log from 2013-03-26\n");
	}

	private static class DirectFileLogAccessor implements LogRawAccessor<ByteLogAccess, Log> {

		@Override
		public ByteLogAccess getLogAccess(final Log log) throws IOException {
			return new DirectFileLogAccess((FileLog) log);
		}

	}

	@Test
	public void testRollingLogAccess() throws Exception {
		final RollingLogsSource source = new RollingLogsSource();
		source.setPattern(logDir.getPath() + "/server.log");
		source.setPastLogsSuffixPattern(".UNKNOWN");
		source.setPastLogsType(PastLogsType.NAME);

		// Test only live
		Log[] rolledLogs = source.getLogs().toArray(new Log[0]);
		Assert.assertEquals(1, rolledLogs.length);
		Assert.assertEquals(5, rolledLogs[0].getSize());
		Assert.assertEquals("live",
				IOUtils.readLines(
						new DailyRollingLogAccess(new DirectFileLogAccessor(), (DailyRollingLog) rolledLogs[0])
								.getInputStream(null))
						.get(0));

		// Test past ordered desc by name
		source.setPastLogsSuffixPattern(".*");
		rolledLogs = source.getLogs().toArray(new Log[0]);
		Assert.assertEquals(1, rolledLogs.length);
		BufferedReader lr = new BufferedReader(new InputStreamReader(
				new DailyRollingLogAccess(new DirectFileLogAccessor(), (DailyRollingLog) rolledLogs[0])
						.getInputStream(null)));
		Assert.assertEquals("log from 2013-03-26", lr.readLine());
		Assert.assertEquals("log from 2013-03-27", lr.readLine());
		Assert.assertEquals("live", lr.readLine());

		// Test ordered by modification date
		source.setPastLogsType(PastLogsType.LAST_MODIFIED);
		rolledLogs = source.getLogs().toArray(new Log[0]);
		lr = new BufferedReader(new InputStreamReader(
				new DailyRollingLogAccess(new DirectFileLogAccessor(), (DailyRollingLog) rolledLogs[0])
						.getInputStream(null)));
		Assert.assertEquals("log from 2013-03-27", lr.readLine());
		Assert.assertEquals("log from 2013-03-26", lr.readLine());
		Assert.assertEquals("live", lr.readLine());
	}

	@Test
	public void testMultipleLiveLogs() throws IOException {
		final RollingLogsSource source = new RollingLogsSource();
		source.setPattern(logDir.getPath() + "/server*.log");
		source.setPastLogsSuffixPattern(".*");

		FileUtils.writeStringToFile(new File(logDir, "server2.log"), "live2\n");
		FileUtils.writeStringToFile(new File(logDir, "server2.log.old"), "oldlive2\n");
		final Log[] rolledLogs = source.getLogs().toArray(new Log[0]);
		Assert.assertEquals(2, rolledLogs.length);
		Log server = null;
		Log server2 = null;
		if (rolledLogs[0].getPath().equals(new File(logDir, "server2.log").getPath())) {
			server2 = rolledLogs[0];
			server = rolledLogs[1];
		} else {
			server2 = rolledLogs[1];
			server = rolledLogs[0];
		}

		// Read from server.log~
		BufferedReader lr = new BufferedReader(new InputStreamReader(
				new DailyRollingLogAccess(new DirectFileLogAccessor(), (DailyRollingLog) server).getInputStream(null)));
		Assert.assertEquals("log from 2013-03-26", lr.readLine());
		Assert.assertEquals("log from 2013-03-27", lr.readLine());
		Assert.assertEquals("live", lr.readLine());
		Assert.assertNull(lr.readLine());

		// Read from server2.log~
		lr = new BufferedReader(
				new InputStreamReader(new DailyRollingLogAccess(new DirectFileLogAccessor(), (DailyRollingLog) server2)
						.getInputStream(null)));
		Assert.assertEquals("oldlive2", lr.readLine());
		Assert.assertEquals("live2", lr.readLine());
		Assert.assertNull(lr.readLine());
	}

	@Test
	public void testConfigViaJSON() throws ConfigException, ParseException {
		final RollingLogsSource source = new RollingLogsSource();
		source.setPattern("server*.log");
		source.setPastLogsSuffixPattern(".*");
		source.setPastLogsType(PastLogsType.LAST_MODIFIED);
		final Log4jTextReader reader = new Log4jTextReader();
		reader.setFormatPattern("%d{ABSOLUTE} %-5p [%c] %m%n");
		source.setReader(new FilteredLogEntryReader<ByteLogAccess>(reader, null));

		final String json = configManager.saveBeanToJSON(source);
		logger.info("Saved log source config to JSON: {}", json);

		final RollingLogsSource checkSource = configManager.createBeanFromJSON(RollingLogsSource.class, json);
		Assert.assertEquals(source.getPattern(), checkSource.getPattern());
		Assert.assertEquals(source.getPastLogsSuffixPattern(), checkSource.getPastLogsSuffixPattern());
		Assert.assertEquals(source.getPastLogsType(), checkSource.getPastLogsType());
		Assert.assertEquals(true, checkSource.getReader().getTargetReader() instanceof Log4jTextReader);
		Assert.assertEquals(reader.getFormatPattern(),
				((Log4jTextReader) checkSource.getReader().getTargetReader()).getFormatPattern());
	}
}
