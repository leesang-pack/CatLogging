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

import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Continue reading log within a specified timeout.
 * 
 * @author Tester
 * 
 */
@Slf4j
public class TimeoutReaderStrategy implements LogEntryReaderStrategy {
	@JsonIgnore
	private long startedAt = -1;

	@JsonProperty
	private long timeout;

	public TimeoutReaderStrategy() {
		super();
	}

	public TimeoutReaderStrategy(final long timeout) {
		this();
		this.timeout = timeout;
	}

	@Override
	public void reset(final Log log, final LogPointerFactory pointerFactory,
			final LogPointer start) throws IOException {
		startedAt = System.currentTimeMillis();
	}

	@Override
	public boolean continueReading(final Log logg,
			final LogPointerFactory pointerFactory,
			final LogEntry currentReadEntry) throws IOException {
		if (startedAt < 0) {
			startedAt = System.currentTimeMillis();
		}
		long taken = System.currentTimeMillis() - startedAt;
		if (taken >= timeout) {
			log.debug(
					"Stop reading {} after {}ms because reached timeout of {}ms",
					logg, taken, timeout);
			return false;
		} else {
			return true;
		}
	}
}
