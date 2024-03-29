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

import javax.validation.constraints.Min;
import java.io.IOException;

/**
 * Reader strategy that reads at minimum the configured amount of bytes
 * {@link #getMinBytesAmount()} (100k by default) from log during the scan
 * process.
 * 
 * @author Tester
 * 
 */
@Slf4j
public class MinBAmountReadStrategy implements LogEntryReaderStrategy {
	@JsonIgnore
	private LogPointer startedAt;

	/**
	 * 100k by default
	 */
	@JsonProperty
	@Min(value = 4096)
	private long minBytesAmount = 1024 * 100;

	public MinBAmountReadStrategy() {
		super();
	}

	public MinBAmountReadStrategy(final long minBytesAmouunt) {
		super();
		this.minBytesAmount = minBytesAmouunt;
	}

	@Override
	public void reset(final Log logg, final LogPointerFactory pointerFactory,
			final LogPointer start) {
		this.startedAt = start;
	}

	@Override
	public boolean continueReading(final Log logg,
			final LogPointerFactory pointerFactory,
			final LogEntry currentReadEntry) throws IOException {
		long read = pointerFactory.getDifference(startedAt,
				currentReadEntry.getEndOffset());
		if (read < getMinBytesAmount()) {
			return true;
		} else {
			log.debug(
					"Interrupt further scanning due to already read the destined min bytes amount: {}",
					read);
			return false;
		}
	}

	/**
	 * @return the minBytesAmouunt
	 */
	public long getMinBytesAmount() {
		return minBytesAmount;
	}

	/**
	 * @param minBytesAmouunt
	 *            the minBytesAmouunt to set
	 */
	public void setMinBytesAmount(final long minBytesAmouunt) {
		this.minBytesAmount = minBytesAmouunt;
	}

}
