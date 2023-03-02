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
package com.catlogging.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.catlogging.config.ConfiguredBean;
import com.catlogging.config.WrappedBean;
import com.catlogging.fields.FieldsMap;
import com.catlogging.reader.filter.FilteredLogEntryReader;

/**
 * A source for logs related to the same reader.
 * 
 * @author Tester
 * 
 */
public interface LogSource<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>> extends ConfiguredBean, LogRawAccessor<ACCESSTYPE, Log> {
	/**
	 * @return the id
	 */
	public long getId();

	/**
	 * @return the name
	 */
	public String getName();

	/**
	 * @return the reader
	 */
	public FilteredLogEntryReader<ACCESSTYPE> getReader();

	public FieldsMap getUiSettings();

	// public int resolveLogs();

	// public List<String> getLogs(int offset, int len) throws IOException;

	public List<Log> getLogs() throws IOException;

	/**
	 * Returns the log associated with given path or null if log not found.
	 * 
	 * @param path
	 *            log path
	 * @return log associated with given path or null if log not found
	 * @throws IOException
	 *             in case of errors
	 */
	public Log getLog(String path) throws IOException;

	/**
	 * Wrapper for delegated log source e.g. to allow lazy inititaing of
	 * sources.
	 * 
	 * @author Tester
	 * 
	 */
	public static abstract class LogSourceWrapper implements LogSource<LogRawAccess<? extends LogInputStream>>,	WrappedBean<LogSource<LogRawAccess<? extends LogInputStream>>> {
		private LogSource<LogRawAccess<? extends LogInputStream>> wrapped;

		@SuppressWarnings("unchecked")
		public static final LogSource<LogRawAccess<? extends LogInputStream>> unwrap(final LogSource<? extends LogRawAccess<? extends LogInputStream>> possiblyWrapped) {
			if (possiblyWrapped instanceof LogSourceWrapper) {
				return ((LogSourceWrapper) possiblyWrapped).getSource();
			}
			return (LogSource<LogRawAccess<? extends LogInputStream>>) possiblyWrapped;
		}

		private LogSource<LogRawAccess<? extends LogInputStream>> getSource() {
			if (wrapped == null) {
				wrapped = getWrapped();
			}
			return wrapped;
		}

		@Override
		public long getId() {
			return getSource().getId();
		}

		@Override
		public String getName() {
			return getSource().getName();
		}

		@Override
		public FilteredLogEntryReader<LogRawAccess<? extends LogInputStream>> getReader() {
			return getSource().getReader();
		}

		@Override
		public List<Log> getLogs() throws IOException {
			return getSource().getLogs();
		}

		@Override
		public Log getLog(final String path) throws IOException {
			return getSource().getLog(path);
		}

		@Override
		public LogRawAccess<? extends LogInputStream> getLogAccess(final Log log) throws IOException {
			return getSource().getLogAccess(log);
		}

		@Override
		public FieldsMap getUiSettings() {
			return getSource().getUiSettings();
		}

	}

	public static final LogSource<LogRawAccess<? extends LogInputStream>> NULL_SOURCE = new LogSource<LogRawAccess<? extends LogInputStream>>() {

		@Override
		public LogRawAccess<LogInputStream> getLogAccess(final Log log) throws IOException {
			return null;
		}

		@Override
		public long getId() {
			return 0;
		}

		@Override
		public String getName() {
			return "undefined";
		}

		@Override
		public FilteredLogEntryReader<LogRawAccess<? extends LogInputStream>> getReader() {
			return null;
		}

		@Override
		public List<Log> getLogs() throws IOException {
			return new ArrayList<>();
		}

		@Override
		public Log getLog(final String path) throws IOException {
			return null;
		}

		@Override
		public FieldsMap getUiSettings() {
			return null;
		}

	};
}
