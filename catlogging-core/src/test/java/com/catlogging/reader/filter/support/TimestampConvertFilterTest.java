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
package com.catlogging.reader.filter.support;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.catlogging.fields.FieldsMap;
import com.catlogging.fields.filter.support.TimestampConvertFilter;
import com.catlogging.model.LogEntry;

/**
 * Test for {@link TimestampConvertFilter}.
 *
 * @author Tester
 *
 */
public class TimestampConvertFilterTest {
	@Test
	public void testOverrideWithNullDueToInvalidFormat() {
		final TimestampConvertFilter filter = new TimestampConvertFilter();
		filter.setOverride(true);
		filter.setSourceField("abc");
		filter.setPattern("sddse");
		final FieldsMap map = new FieldsMap();
		map.put(LogEntry.FIELD_TIMESTAMP, new Date());
		map.put("abc", "2015");
		filter.filter(map);
		Assert.assertNull(map.get(LogEntry.FIELD_TIMESTAMP));
	}

	@Test
	public void testYearFormat() {
		final TimestampConvertFilter filter = new TimestampConvertFilter();
		filter.setOverride(true);
		filter.setSourceField("abc");
		filter.setPattern("yyyy");
		final FieldsMap map = new FieldsMap();
		map.put(LogEntry.FIELD_TIMESTAMP, new Date());
		map.put("abc", "2015");
		filter.filter(map);
		Assert.assertNotNull(map.get(LogEntry.FIELD_TIMESTAMP));
		final Calendar c = Calendar.getInstance();
		c.setTime(((Date) map.get(LogEntry.FIELD_TIMESTAMP)));
		Assert.assertEquals(2015, c.get(Calendar.YEAR));
	}
}
