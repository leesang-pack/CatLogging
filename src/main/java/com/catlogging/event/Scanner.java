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
package com.catlogging.event;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.catlogging.config.ConfigException;
import com.catlogging.config.ConfiguredBean;
import com.catlogging.config.WrappedBean;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.fields.FieldsHost;
import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogRawAccess;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;

/**
 * Incremental event scanner.
 * 
 * @author Tester
 * 
 */
public interface Scanner extends ConfiguredBean, FieldsHost {
	/**
	 * Consumes events to allow pipeline processing.
	 * 
	 * @author Tester
	 * 
	 */
	public static interface EventConsumer {
		void consume(Event eventData) throws IOException, FormatException;
	}

	/**
	 * Incremental routine to search for next log events. The routine should
	 * store in incrementData values to continue the search process in the next
	 * time it's called without the need to start from log's start.
	 * 
	 * @param reader
	 *            reader for the log
	 * @param readerStrategy
	 *            Strategy for log reader describing how long the scanner should
	 *            read from log.
	 * @param log
	 *            the log access to search for events in
	 * @param incrementData
	 *            the persistent data between multiple calls to support
	 *            incremental log scanning for event.
	 * @param eventConsumer
	 *            event consumer
	 */
	public <R extends LogRawAccess<? extends LogInputStream>> void find(LogEntryReader<R> reader,
			LogEntryReaderStrategy readerStrategy, Log log, R logAccess, IncrementData incrementData,
			EventConsumer eventConsumer) throws IOException, FormatException;

	/**
	 * Wrapper for delegated strategy e.g. to allow lazy initiation.
	 * 
	 * @author Tester
	 */
	public static abstract class LogEntryReaderStrategyWrapper
			implements LogEntryReaderStrategy, WrappedBean<LogEntryReaderStrategy> {
		private LogEntryReaderStrategy wrapped;

		public static final LogEntryReaderStrategy unwrap(final LogEntryReaderStrategy possiblyWrapped) {
			if (possiblyWrapped instanceof LogEntryReaderStrategyWrapper) {
				return ((LogEntryReaderStrategyWrapper) possiblyWrapped).getWrappedStrategy();
			}
			return possiblyWrapped;
		}

		public final LogEntryReaderStrategy getWrappedStrategy() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public void reset(final Log log, final LogPointerFactory pointerFactory, final LogPointer start)
				throws IOException {
			getWrappedStrategy().reset(log, pointerFactory, start);
		}

		@Override
		public boolean continueReading(final Log log, final LogPointerFactory pointerFactory,
				final LogEntry currentReadEntry) throws IOException {
			return getWrappedStrategy().continueReading(log, pointerFactory, currentReadEntry);
		}

	}

	/**
	 * Wrapper for delegated log scanner e.g. to allow lazy initiation.
	 * 
	 * @author Tester
	 */
	public static abstract class ScannerWrapper implements Scanner, WrappedBean<Scanner> {
		private Scanner wrapped;

		public static final Scanner unwrap(final Scanner possiblyWrapped) {
			if (possiblyWrapped instanceof ScannerWrapper) {
				return ((ScannerWrapper) possiblyWrapped).getWrappedScanner();
			}
			return possiblyWrapped;
		}

		public final Scanner getWrappedScanner() throws ConfigException {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public <R extends LogRawAccess<? extends LogInputStream>> void find(final LogEntryReader<R> reader,
				final LogEntryReaderStrategy readerStrategy, final Log log, final R logAccess,
				final IncrementData incrementData, final EventConsumer eventConsumer)
						throws IOException, FormatException {
			try {
				getWrappedScanner().find(reader, readerStrategy, log, logAccess, incrementData, eventConsumer);
			} catch (final ConfigException e) {
				throw new IOException("Failed to create configured scanner", e);
			}
		}

		@Override
		public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
			try {
				return getWrappedScanner().getFieldTypes();
			} catch (final ConfigException e) {
				throw new FormatException("Failed to create configured scanner", e);
			}
		}

	}
}
