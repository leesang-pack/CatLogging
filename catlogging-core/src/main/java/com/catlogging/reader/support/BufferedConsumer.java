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

import java.util.ArrayList;
import java.util.List;

import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;

/**
 * Consumes the entries and puts these to a buffer. Consumes so long as the
 * given buffer size isn't filled.
 * 
 * @author Tester
 * 
 * @param <ContentType>
 */
public class BufferedConsumer implements LogEntryConsumer {
	private final int size;
	private final List<LogEntry> buffer;

	public BufferedConsumer(final int limit) {
		super();
		this.size = limit;
		this.buffer = new ArrayList<LogEntry>();
	}

	@Override
	public boolean consume(final Log log, LogPointerFactory pointerFactory,
			final LogEntry entry) {
		buffer.add(entry);
		return buffer.size() < size;
	}

	/**
	 * @return the buffer
	 */
	public List<LogEntry> getBuffer() {
		return buffer;
	}

}
