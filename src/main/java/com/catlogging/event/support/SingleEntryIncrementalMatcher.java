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

import com.catlogging.event.Event;
import com.catlogging.event.IncrementData;
import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Scanner;
import com.catlogging.model.*;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Matcher based on a single entry analysis. It supports natively the
 * incremental scanning.
 * 
 * @author Tester
 * 
 */
@Slf4j
public abstract class SingleEntryIncrementalMatcher implements Scanner {

	@Override
	public <R extends LogRawAccess<? extends LogInputStream>> void find(final LogEntryReader<R> reader,
			final LogEntryReaderStrategy readerStrategy, final Log logg, final R logAccess,
			final IncrementData incrementData, final EventConsumer eventConsumer) throws IOException, FormatException {
		try {
			reader.readEntries(logg, logAccess, incrementData.getNextOffset(logAccess), new LogEntryConsumer() {
				@Override
				public boolean consume(final Log logg, final LogPointerFactory pointerFactory, final LogEntry entry)
						throws IOException {
					incrementData.setNextOffset(entry.getEndOffset());
					Event event;
					try {
						event = matches(entry);
					} catch (final FormatException e) {
						throw new IOException(e);
					}
					if (event != null) {
						log.debug("Entry matches the interest: {}", entry);
						final ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
						entries.add(entry);
						event.setEntries(entries);
						eventConsumer.consume(event);
					}
					return readerStrategy.continueReading(logg, pointerFactory, entry);
				}
			});
		} catch (final IOException e) {
			if (e.getCause() instanceof FormatException) {
				throw (FormatException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	/**
	 * Returns the event data if given entry matches the scanner criteria and
	 * null otherwise.
	 * 
	 * @param entry
	 *            the entry to match
	 * @return the event data if given entry matches the scanner criteria and
	 *         null otherwise
	 */
	public abstract Event matches(LogEntry entry) throws IOException, FormatException;

}
