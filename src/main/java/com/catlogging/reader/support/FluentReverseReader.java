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
package com.catlogging.reader.support;

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.SeverityLevel;
import com.catlogging.model.support.ByteLogAccess;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Consumes log entries from a byte log access in a reverse order direction
 * fluently.
 * 
 * @author Tester
 * 
 */
@Slf4j
public class FluentReverseReader<ACCESSORTYPE extends ByteLogAccess> implements LogEntryReader<ACCESSORTYPE> {
	private final LogEntryReader<ACCESSORTYPE> forwardReader;

	public FluentReverseReader(final LogEntryReader<ACCESSORTYPE> forwardReader) {
		super();
		this.forwardReader = forwardReader;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void readEntries(final Log logg, final ACCESSORTYPE logAccess, final LogPointer startOffset,
			final LogEntryConsumer consumer) throws IOException, FormatException {
		log.debug("Starting fluent backward consumption in {} from: {}", logg, startOffset);
		List<LogEntry> entries = new BackwardReader(forwardReader).readEntries(logg, logAccess, startOffset, -101);
		log.debug("Starting fluent backward consumption with first block of {} entries", entries.size());
		while (entries.size() > 0) {
			// Record the pointer to continue from in next iteration due to the
			// log entry can be changed after consumption
			final LogPointer toContinueFrom = entries.get(0).getStartOffset();
			for (int i = entries.size() - 1; i >= 0; i--) {
				if (!consumer.consume(logg, logAccess, entries.get(i))) {
					log.debug("Cancelled fluent backward consumption");
					return;
				}
			}
			entries = new BackwardReader(forwardReader).readEntries(logg, logAccess, toContinueFrom, -101);
			log.debug("Continue fluent backward consumption with next block of {} entries", entries.size());
		}
		log.debug("Finished fluent backward consumption because of SOF");

	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return forwardReader.getSupportedSeverities();
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return forwardReader.getFieldTypes();
	}

	@Override
	public void readEntriesReverse(final Log log, final ACCESSORTYPE logAccess, final LogPointer startOffset,
			final com.catlogging.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		// Reverse
		forwardReader.readEntries(log, logAccess, startOffset, consumer);
	}

}
