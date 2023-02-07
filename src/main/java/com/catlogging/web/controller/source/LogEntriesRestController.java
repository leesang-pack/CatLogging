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
package com.catlogging.web.controller.source;

import com.catlogging.event.Event;
import com.catlogging.event.IncrementData;
import com.catlogging.event.Scanner;
import com.catlogging.event.Scanner.EventConsumer;
import com.catlogging.event.support.TimeoutReaderStrategy;
import com.catlogging.model.*;
import com.catlogging.model.LogPointerFactory.NavigationFuture;
import com.catlogging.model.Navigation.DateOffsetNavigation;
import com.catlogging.model.Navigation.NavigationType;
import com.catlogging.model.support.ByteLogAccess;
import com.catlogging.model.support.TimestampNavigation;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.support.BufferedConsumer;
import com.catlogging.reader.support.InverseReader;
import com.catlogging.util.excption.ResourceNotFoundException;
import com.catlogging.web.controller.LogEntriesResult;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * REST controller for several log entries purposes.
 * 
 * @author Tester
 * 
 */
@Slf4j
@RestController
public class LogEntriesRestController {
	@Autowired
	private LogSourceProvider logsSourceProvider;

	private LogSource<LogRawAccess<? extends LogInputStream>> getActiveLogSource(final long logSourceId)
			throws ResourceNotFoundException {
		final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = logsSourceProvider
				.getSourceById(logSourceId);
		if (activeLogSource != null) {
			return activeLogSource;
		} else {
			throw new ResourceNotFoundException(LogSource.class, logSourceId,
					"Log source not found for id: " + logSourceId);
		}
	}

	private int getHighlightEntryIndex(final LogPointer desiredPointer, final int desiredCount,
			final List<LogEntry> entries) {
		if (!entries.isEmpty()) {
			if (desiredCount >= 0) {
				if (desiredPointer == null) {
					return 0;
				} else {
					for (int i = 0; i < entries.size(); i++) {
						if (desiredPointer.equals(entries.get(i).getStartOffset())) {
							return i;
						}
					}
				}

			} else {
				if (desiredPointer == null) {
					return entries.size() - 1;
				} else {
					for (int i = entries.size() - 1; i >= 0; i--) {
						if (desiredPointer.equals(entries.get(i).getEndOffset())) {
							return i;
						}
					}

				}
			}
		}
		return -1;
	}

	@RequestMapping(value = "/sources/entries", method = RequestMethod.POST)
	@ResponseBody
	LogEntriesResult getEntries(
			@Valid @RequestBody final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count)
			throws IOException, FormatException, ResourceNotFoundException {
		log.debug("Start load entries log={} from source={}, mark={}, count={}", logPath, activeLogSource, mark,
				count);
		try {
			final Log log = getLog(activeLogSource, logPath);
			LogPointer pointer = null;
			final LogRawAccess<? extends LogInputStream> logAccess = activeLogSource.getLogAccess(log);
			if (StringUtils.isNotEmpty(mark)) {
				pointer = logAccess.getFromJSON(mark);
			} else {
				if (count < 0) {
					// Tail
					pointer = logAccess.end();
				}
			}
			if (count > 0) {
				final BufferedConsumer bc = new BufferedConsumer(count);
				activeLogSource.getReader().readEntries(log, logAccess, pointer, bc);
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), bc.getBuffer(),
						getHighlightEntryIndex(pointer, count, bc.getBuffer()));
			} else {
				final BufferedConsumer bc = new BufferedConsumer(-count);
				activeLogSource.getReader().readEntriesReverse(log, logAccess, pointer, bc);
				final List<LogEntry> readEntries = bc.getBuffer();
				Collections.reverse(readEntries);
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries,
						getHighlightEntryIndex(pointer, count, readEntries));
			}

		} finally {
			log.debug("Finished log entries from log={} and source={}", logPath, activeLogSource);
		}
	}

	@RequestMapping(value = "/sources/{logSource}/entries", method = RequestMethod.GET)
	@ResponseBody
	LogEntriesResult getEntries(@PathVariable("logSource") final long logSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count)
			throws IOException, FormatException, ResourceNotFoundException {
		log.debug("Start load entries log={} from source={}, mark={}, count={}", logPath, logSource, mark, count);
		try {
			final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = getActiveLogSource(logSource);
			return getEntries(activeLogSource, logPath, mark, count);
		} finally {
			log.debug("Finished log entries from log={} and source={}", logPath, logSource);
		}
	}

	private NavigationResolver getResolver(final NavigationType type) {
		if (type == NavigationType.BYTE) {
			return new ByteNavigationResolver();
		} else {
			return new DateNavigationResolver();
		}
	}

	@RequestMapping(value = "/sources/randomAccessEntries", method = RequestMethod.POST)
	@ResponseBody
	LogEntriesResult getRandomAccessEntries(
			@Valid @RequestBody final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "navType", defaultValue = "BYTE") final NavigationType navType,
			@RequestParam(value = "mark") final String position, @RequestParam(value = "count") final int count)
			throws IOException, FormatException, ResourceNotFoundException {
		log.debug("Start loading random access entries log={} from source={}, navType={}, position={}, count={}",
				logPath, activeLogSource, navType, position, count);
		try {
			final Log log = getLog(activeLogSource, logPath);
			final LogRawAccess<? extends LogInputStream> logAccess = activeLogSource.getLogAccess(log);
			LogPointer pointer = null;
			if (StringUtils.isNotEmpty(position)) {
				pointer = getResolver(navType).navigate(activeLogSource, logAccess, log, position).get();
			}
			final BufferedConsumer bc = new BufferedConsumer(count + 1);
			if (pointer != null) {
				if (pointer.isEOF()) {
					// End pointer, return the last 10 simply
					final BufferedConsumer bcLast = new BufferedConsumer(10);
					activeLogSource.getReader().readEntriesReverse(log, logAccess, pointer, bcLast);
					final List<LogEntry> readEntries = bcLast.getBuffer();
					Collections.reverse(readEntries);
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries,
							getHighlightEntryIndex(pointer, -10, readEntries));
				}
			}
			activeLogSource.getReader().readEntries(log, logAccess, pointer != null ? pointer : null, bc);
			final List<LogEntry> entries = bc.getBuffer();
			if (entries.size() > 0) {
				final LogEntry first = entries.get(0);
				final LogEntry last = entries.get(entries.size() - 1);
				if (!first.getStartOffset().isSOF() && last.getEndOffset().isEOF() && entries.size() < 10) {
					// Hm, EOF reached
					final BufferedConsumer bcLast = new BufferedConsumer(10);
					activeLogSource.getReader().readEntriesReverse(log, logAccess, last.getEndOffset(), bcLast);
					final List<LogEntry> readEntries = bcLast.getBuffer();
					Collections.reverse(readEntries);
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), readEntries, -1);
				} else if (!first.isUnformatted()) {
					// -1 without effect, return from beginning
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), entries,
							getHighlightEntryIndex(pointer, 0, entries));
				} else {
					// Return from the second one
					return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(),
							entries.subList(1, entries.size()), getHighlightEntryIndex(pointer, 0, entries) - 1);
				}
			} else {
				return new LogEntriesResult(activeLogSource.getReader().getFieldTypes(), entries, -1);
			}
		} finally {
			log.debug("Finished loading random access entries from log={} and source={}", logPath, activeLogSource);
		}
	}

	@RequestMapping(value = "/sources/{logSource}/randomAccessEntries", method = RequestMethod.GET)
	@ResponseBody
	LogEntriesResult getRandomAccessEntries(@PathVariable("logSource") final long logSource,
			@RequestParam("log") final String logPath,
			@RequestParam(value = "navType", defaultValue = "BYTE") final NavigationType navType,
			@RequestParam(value = "mark") final String position, @RequestParam(value = "count") final int count)
			throws IOException, FormatException, ResourceNotFoundException {
		log.debug("Start loading random access entries log={} from source={}, navType={}, position={}, count={}",
				logPath, logSource, navType, position, count);
		try {
			final LogSource<LogRawAccess<? extends LogInputStream>> activeLogSource = getActiveLogSource(logSource);
			return getRandomAccessEntries(activeLogSource, logPath, navType, position, count);
		} finally {
			log.debug("Finished loading random access entries from log={} and source={}", logPath, logSource);
		}
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class SearchResult {
		@JsonIgnore
		private boolean sofReached = false;
		private LogEntriesResult entries;
		private LogEntry lastEntry;
		private long scannedSize;
		private long scannedTime;
		private Event event;

		/**
		 * @return the entries
		 */
		public LogEntriesResult getEntries() {
			return entries;
		}

		public LogEntry getLastEntry() {
			return lastEntry;
		}

		/**
		 * @return the scannedSize
		 */
		public long getScannedSize() {
			return scannedSize;
		}

		/**
		 * @return the scannedTime
		 */
		public long getScannedTime() {
			return scannedTime;
		}

		/**
		 * @return the event
		 */
		public Event getEvent() {
			return event;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/sources/{logSource}/search", method = RequestMethod.POST)
	@ResponseBody
	SearchResult searchEntries(@RequestBody @Valid final Scanner scanner,
			@PathVariable("logSource") final long logSource, @RequestParam("log") final String logPath,
			@RequestParam(value = "mark", required = false) final String mark,
			@RequestParam(value = "count") final int count, final BindingResult bResult)
			throws IOException, FormatException, ResourceNotFoundException {
		final long start = System.currentTimeMillis();
		log.debug("Start searching entries log={} from source={}, mark={}, count={}", logPath, logSource, mark,
				count);
		final LogSource<LogRawAccess<? extends LogInputStream>> source = getActiveLogSource(logSource);
		final Log logg = getLog(source, logPath);
		final LogRawAccess<? extends LogInputStream> logAccess = source.getLogAccess(logg);
		final IncrementData incData = new IncrementData();
		LogPointer searchPointer = null;
		if (StringUtils.isNotEmpty(mark)) {
			searchPointer = logAccess.getFromJSON(mark);
		}
		incData.setNextOffset(searchPointer);
		final SearchResult searchResult = new SearchResult();
		LogEntryReader<LogRawAccess<? extends LogInputStream>> reader = null;
		if (count >= 0) {
			reader = ((LogSource) source).getReader();
		} else {
			reader = new InverseReader<>(source.getReader());
		}
		scanner.find(reader, new TimeoutReaderStrategy(3 * 1000) {
			@Override
			public boolean continueReading(final Log logg, final LogPointerFactory pointerFactory,
					final LogEntry currentReadEntry) throws IOException {
				searchResult.lastEntry = currentReadEntry;
				if (count < 0 && currentReadEntry.getStartOffset().isSOF()) {
					// File start reached
					searchResult.sofReached = true;
				}
				return !searchResult.sofReached && searchResult.event == null
						&& super.continueReading(logg, pointerFactory, currentReadEntry);
			}
		}, logg, logAccess, incData, new EventConsumer() {
			@Override
			public void consume(final Event eventData) throws IOException {
				searchResult.event = eventData;
				// TODO Check after merge of events and entries
				if (eventData.getEntries() != null && !eventData.getEntries().isEmpty()) {
					searchResult.lastEntry = eventData.getEntries().get(0);
				}
			}
		});
		searchResult.scannedTime = System.currentTimeMillis() - start;
		LogPointer pointerForResult = searchResult.lastEntry != null ? searchResult.lastEntry.getStartOffset() : null;
		if (searchResult.event != null) {
			// Found
			log.debug("Found next entry of interest in {}: {}", logg, searchResult.event);
			final BufferedConsumer bc = new BufferedConsumer(Math.abs(count));
			source.getReader().readEntries(logg, logAccess, pointerForResult, bc);
			searchResult.entries = new LogEntriesResult(source.getReader().getFieldTypes(), bc.getBuffer(), 0);
		} else if (searchResult.sofReached) {
			// Return start pointer
			pointerForResult = logAccess.start();
		} else {
			// Nothing found in that round
			pointerForResult = incData.getNextOffset();
		}
		searchResult.scannedSize = Math.abs(logAccess.getDifference(searchPointer, pointerForResult));
		return searchResult;
	}

	private Log getLog(final LogSource<? extends LogRawAccess<? extends LogInputStream>> activeLogSource,
			final String logPath) throws IOException, ResourceNotFoundException {
		if (logPath == null) {
			return null;
		}
		final Log log = activeLogSource.getLog(logPath);
		if (log != null) {
			return log;
		} else {
			throw new ResourceNotFoundException(Log.class, logPath,
					"Log not found in source " + activeLogSource + ": " + logPath);
		}
	}

	private static interface NavigationResolver {
		NavigationFuture navigate(LogSource<? extends LogRawAccess<? extends LogInputStream>> logSource,
				LogRawAccess<? extends LogInputStream> logAccess, Log log, String strPosition) throws IOException;
	}

	/**
	 * Byte navigation. FIXME: Historically the byte position was transfered via
	 * a JSON pointer with manipulated byte offset. This implementation sticks
	 * still to this concept.
	 * 
	 * @author Tester
	 *
	 */
	private static class ByteNavigationResolver implements NavigationResolver {

		@Override
		public NavigationFuture navigate(final LogSource<? extends LogRawAccess<? extends LogInputStream>> logSource,
				final LogRawAccess<? extends LogInputStream> logAccess, final Log log, final String strPosition) {
			return new NavigationFuture() {
				@Override
				public LogPointer get() throws IOException {
					return logAccess.getFromJSON(strPosition);
				}
			};
		}
	}

	private static class DateNavigationResolver implements NavigationResolver {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public NavigationFuture navigate(final LogSource<? extends LogRawAccess<? extends LogInputStream>> logSource,
				final LogRawAccess<? extends LogInputStream> logAccess, final Log log, final String strPosition)
				throws IOException {
			if (NumberUtils.isNumber(strPosition)) {
				final Date from = new Date(Long.parseLong(strPosition));
				if (logAccess instanceof ByteLogAccess) {
					final LogEntryReader reader = logSource.getReader();
					if (reader.getFieldTypes().containsKey(LogEntry.FIELD_TIMESTAMP)) {
						return new TimestampNavigation(log, (ByteLogAccess) logAccess, reader).absolute(from);
					} else {
						throw new IOException(
								"Navigation by date isn't supported, because the reader doesn't list the mandatory field: "
										+ LogEntry.FIELD_TIMESTAMP);
					}
				} else {
					final Navigation<?> nav = logAccess.getNavigation();
					if (nav instanceof DateOffsetNavigation) {
						return ((DateOffsetNavigation) nav).absolute(from);
					}
					throw new IOException("Navigation by date isn't supported by this log source: " + logSource);
				}
			} else {
				throw new IOException("Position isn't of type numer/date: " + strPosition);
			}
		}

	}
}
