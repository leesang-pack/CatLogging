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
package com.catlogging.model.support;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.catlogging.model.LogPointer;

/**
 * Test for {@link DailyRollingLog}.
 * 
 * @author Tester
 * 
 */
public class DailyRollingLogTest {
	@Test
	public void testReadingOnlyLive() throws IOException {
		final String log1Text = "live";
		final ByteArrayLog blog = new ByteArrayLog(log1Text.getBytes());
		final DailyRollingLog log = new DailyRollingLog("abc", "abc.txt", blog);
		Assert.assertEquals(log1Text.length(), log.getSize());
		final DailyRollingLogAccess logAccess = new DailyRollingLogAccess(blog, log);
		final LineInputStream lis = new LineInputStream(logAccess, logAccess.getInputStream(null), "UTF-8");
		Assert.assertEquals(true, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals(log1Text, lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(true, lis.getPointer().isEOF());
		Assert.assertNull(lis.readNextLine());
		lis.close();
	}

	@Test
	public void testRollingReading() throws IOException {
		final String liveText = "live";
		final String pastText = "old-start\nold-next\n";

		final DailyRollingLog log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog(liveText.getBytes()),
				new ByteArrayLog(pastText.getBytes()));
		Assert.assertEquals(liveText.length() + pastText.length(), log.getSize());

		final DailyRollingLogAccess logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		LineInputStream lis = new LineInputStream(logAccess, logAccess.getInputStream(null), "UTF-8");
		Assert.assertEquals(true, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("old-start", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		final LogPointer rollingMarkOldMiddle = lis.getPointer();
		Assert.assertEquals("old-next", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		final LogPointer rollingMarkLiveStart = lis.getPointer();
		Assert.assertEquals("live", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(true, lis.getPointer().isEOF());
		Assert.assertNull(lis.readNextLine());
		lis.close();

		// Test mark in old
		Assert.assertEquals(rollingMarkOldMiddle, logAccess.getFromJSON(rollingMarkOldMiddle.getJson()));
		lis = new LineInputStream(logAccess, logAccess.getInputStream(rollingMarkOldMiddle), "UTF-8");
		Assert.assertEquals("old-next", lis.readNextLine());
		Assert.assertEquals("live", lis.readNextLine());
		Assert.assertNull(lis.readNextLine());
		lis.close();

		// Test mark in live
		Assert.assertEquals(rollingMarkLiveStart, logAccess.getFromJSON(rollingMarkLiveStart.getJson()));
		lis = new LineInputStream(logAccess, logAccess.getInputStream(rollingMarkLiveStart), "UTF-8");
		Assert.assertEquals("live", lis.readNextLine());
		Assert.assertNull(lis.readNextLine());
		lis.close();
	}

	@Test
	public void testPointerWithRolledLive() throws IOException {
		final String liveNewText = "liveNew1\nliveNew2\r\n";
		final String liveOldText = "liveOld1\nliveOld2\n";
		final String pastText = "old-start\nold-next\n";
		DailyRollingLog log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog(liveOldText.getBytes()),
				new ByteArrayLog(pastText.getBytes()));

		DailyRollingLogAccess logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		LineInputStream lis = new LineInputStream(logAccess, logAccess.getInputStream(null), "UTF-8");
		Assert.assertEquals("old-start", lis.readNextLine());
		Assert.assertEquals("old-next", lis.readNextLine());
		Assert.assertEquals("liveOld1", lis.readNextLine());
		final LogPointer oldLiveMark = lis.getPointer();
		lis.close();

		// Try the mark with the same log
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog(liveOldText.getBytes()),
				new ByteArrayLog(pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(oldLiveMark), "UTF-8");
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("liveOld2", lis.readNextLine());
		Assert.assertEquals(true, lis.getPointer().isEOF());
		lis.close();

		// Now roll the liveOld
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog(liveNewText.getBytes()),
				new ByteArrayLog(liveOldText.getBytes()), new ByteArrayLog(pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(oldLiveMark), "UTF-8");
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("liveOld2", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("liveNew1", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("liveNew2", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(true, lis.getPointer().isEOF());
		Assert.assertNull(lis.readNextLine());
		lis.close();
	}

	@Test
	public void testRollingErrors() throws IOException {
		final String liveNewText = "liveNew1\nliveNew2\r\n";
		final String liveOldText = "liveOld1\nliveOld2\n";
		final String pastText = "old-start\nold-next\n";
		DailyRollingLog log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("past", pastText.getBytes()));
		DailyRollingLogAccess logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		LineInputStream lis = new LineInputStream(logAccess, logAccess.getInputStream(null), "UTF-8");
		Assert.assertEquals("old-start", lis.readNextLine());
		LogPointer mark = lis.getPointer();
		lis.close();

		// Detecting next archive failed
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("past", liveOldText.getBytes()),
				new ByteArrayLog("other", pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		Assert.assertEquals(true, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("old-start", lis.readNextLine());
		lis.close();

		// Unknown mark
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("pastUNKNOWN", liveOldText.getBytes()),
				new ByteArrayLog("other", pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		Assert.assertEquals(true, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("old-start", lis.readNextLine());
		lis.close();

		// Continue reading after two rolls
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("liveNew", liveNewText.getBytes()),
				new ByteArrayLog("liveOld", liveOldText.getBytes()), new ByteArrayLog("past", pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(null), "UTF-8");
		Assert.assertEquals("old-start", lis.readNextLine());
		Assert.assertEquals("old-next", lis.readNextLine());
		Assert.assertEquals("liveOld1", lis.readNextLine());
		Assert.assertEquals("liveOld2", lis.readNextLine());
		Assert.assertEquals("liveNew1", lis.readNextLine());
		mark = lis.getPointer();
		lis.close();

		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("liveNew", "zz\n".getBytes()),
				new ByteArrayLog("unknown", "unknown\n".getBytes()),
				new ByteArrayLog("continueHere", liveNewText.getBytes()),
				new ByteArrayLog("liveOld", liveOldText.getBytes()), new ByteArrayLog("past", pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("liveNew2", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("unknown", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("zz", lis.readNextLine());
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(true, lis.getPointer().isEOF());
		lis.close();

		// The same, but with missing reference "liveOld" => Start from
		// beginning
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("liveNew", "zz\n".getBytes()),
				new ByteArrayLog("unknown", "unknown\n".getBytes()),
				new ByteArrayLog("continueHere", liveNewText.getBytes()),
				new ByteArrayLog("past", pastText.getBytes()));
		logAccess = new DailyRollingLogAccess(new ByteArrayLog(new byte[0]), log);
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		mark = lis.getPointer();
		lis.close();
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		Assert.assertEquals(true, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("old-start", lis.readNextLine());
		mark = lis.getPointer();
		lis.close();

		// Mark in the past and with list changes, but with still stable pointer
		log = new DailyRollingLog("abc", "abc.txt", new ByteArrayLog("liveNew", "zz\n".getBytes()),
				new ByteArrayLog("continueHere", liveNewText.getBytes()),
				new ByteArrayLog("past", pastText.getBytes()));
		lis = new LineInputStream(logAccess, logAccess.getInputStream(mark), "UTF-8");
		Assert.assertEquals(false, lis.getPointer().isSOF());
		Assert.assertEquals(false, lis.getPointer().isEOF());
		Assert.assertEquals("old-next", lis.readNextLine());
		lis.close();
	}
}
