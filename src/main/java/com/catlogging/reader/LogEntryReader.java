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
package com.catlogging.reader;

import java.io.IOException;
import java.util.List;

import com.catlogging.config.ConfiguredBean;
import com.catlogging.fields.FieldsHost;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.SeverityLevel;

/**
 * Format dependent log reader. Reading is performed pipeline like.
 * 
 * @author Tester
 * 
 */
public interface LogEntryReader<ACCESSORTYPE extends LogRawAccess<? extends LogInputStream>>
		extends ConfiguredBean, FieldsHost {

	/**
	 * Consumer for log entries, called sequentially when a new entry was read.
	 * 
	 * @author Tester
	 * 
	 */
	public static interface LogEntryConsumer {
		/**
		 * Called to consume the new read log entry.
		 * 
		 * @param log
		 *            the log
		 * @param pointerFactory
		 *            the pointer factory
		 * @param entry
		 *            the read entry
		 * @return return true to continue reading (if EOF isn't reached) or
		 *         false to interrupt further reading.
		 * @throws IOException
		 *             in case of any errors
		 */
		boolean consume(Log log, LogPointerFactory pointerFactory, LogEntry entry) throws IOException;
	}

	/**
	 * Reads non-blocking the log entries beginning with the byte offset in log.
	 * The read entries will be propagated sequentially to the given consumer.
	 * The method returns back when {@link LogEntryConsumer#consume(LogEntry)}
	 * returns false or the boundary is reached.
	 * 
	 * @param log
	 *            the log to read
	 * @param logAccess
	 *            the access to the log to read from
	 * @param startOffset
	 *            the offset pointer in the log to start reading on. A null
	 *            value means start from beginning.
	 * 
	 * @param consumer
	 *            consumer to propagate read entries to
	 */
	public void readEntries(Log log, ACCESSORTYPE logAccess, LogPointer startOffset, LogEntryConsumer consumer)
			throws IOException;

	/**
	 * Reads log entries in a reverse order beginning from the given offset. The
	 * read entries will be propagated sequentially to the given consumer. The
	 * method returns back when {@link LogEntryConsumer#consume(LogEntry)}
	 * returns false or the boundary is reached.
	 * 
	 * @param log
	 *            the log to read
	 * @param logAccess
	 *            the access to the log to read from
	 * @param startOffset
	 *            the offset pointer in the log to start reading on. A null
	 *            value means start from beginning.
	 * 
	 * @param consumer
	 *            consumer to propagate read entries to
	 */
	public void readEntriesReverse(Log log, ACCESSORTYPE logAccess, LogPointer startOffset, LogEntryConsumer consumer)
			throws IOException;

	/**
	 * 
	 * @return list of supported and provided severity levels.
	 */
	public List<SeverityLevel> getSupportedSeverities();

	/**
	 * Wrapper for delegated log entry reader e.g. to allow lazy initiating of
	 * readers.
	 * 
	 * @author Tester
	 * 
	 * @param <ContentType>
	 *            the entry type
	 */
	public static abstract class LogEntryReaderWrapper
			implements LogEntryReader<LogRawAccess<? extends LogInputStream>> {
		private LogEntryReader<LogRawAccess<? extends LogInputStream>> wrapped;

		protected abstract LogEntryReader<LogRawAccess<? extends LogInputStream>> getWrapped()
				throws IOException, FormatException;

		private LogEntryReader<LogRawAccess<? extends LogInputStream>> getReader() throws IOException, FormatException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void readEntries(final Log log, final LogRawAccess<? extends LogInputStream> logAccess,
				final LogPointer startOffset, final LogEntryConsumer consumer) throws IOException, FormatException {
			getReader().readEntries(log, logAccess, startOffset, consumer);
		}

		@Override
		public List<SeverityLevel> getSupportedSeverities() {
			try {
				return getReader().getSupportedSeverities();
			} catch (final Exception e) {
				throw new RuntimeException("Unexpected", e);
			}
		}

	}
}
