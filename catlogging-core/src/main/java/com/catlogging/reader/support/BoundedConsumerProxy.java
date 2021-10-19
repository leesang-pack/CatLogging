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

import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;

/**
 * Proxy consumer which delegates entry cosnuming until a given boundary is
 * reached.
 * 
 * @author Tester
 * 
 * @param
 */
public class BoundedConsumerProxy implements LogEntryConsumer {
	private final LogEntryConsumer targetConsumer;
	private final LogPointer boundary;

	public BoundedConsumerProxy(final LogEntryConsumer targetConsumer,
			final LogPointer boundary) {
		super();
		this.targetConsumer = targetConsumer;
		this.boundary = boundary;
	}

	@Override
	public boolean consume(final Log log, LogPointerFactory pointerFactory,
			final LogEntry entry) throws IOException {
		if (pointerFactory.getDifference(entry.getEndOffset(), boundary) >= 0) {
			// Within boundary, thus delegate
			return targetConsumer.consume(log, pointerFactory, entry);
		}
		// Boundary reached
		return false;
	}

}
