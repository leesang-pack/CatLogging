package com.catlogging.model.support;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catlogging.model.Log;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogPointerFactory;
import com.catlogging.model.LogPointerFactory.NavigationFuture;
import com.catlogging.model.Navigation;
import com.catlogging.reader.LogEntryReader;
import com.catlogging.reader.LogEntryReader.LogEntryConsumer;

/**
 * Navigates in a log by log entry timestamps (field
 * {@link LogEntry#FIELD_TIMESTAMP}. This implementation is based on the binary
 * search algorithm with the assumption that the log is ordered by timestamps.
 * 
 * @author Tester
 *
 */
public class TimestampNavigation implements Navigation<Date> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimestampNavigation.class);
	private final ByteLogAccess logAccess;
	private final LogEntryReader<ByteLogAccess> reader;
	private final Log log;

	public TimestampNavigation(final Log log, final ByteLogAccess logAccess,
			final LogEntryReader<ByteLogAccess> reader) {
		this.log = log;
		this.logAccess = logAccess;
		this.reader = reader;
	}

	private LogPointer navigate(final long haystackOffset, long leftBound, long rightBound) throws IOException {
		int i = 0;
		LogEntry lastEntry = null;
		final long start = System.currentTimeMillis();
		while (leftBound <= rightBound) {
			i++;
			final long mid = leftBound + (rightBound - leftBound) / 2;
			final LogPointer midPointer = logAccess.createRelative(null, mid);
			final LogEntry entry = getEntryNextTo(midPointer);
			if (entry != null) {
				final long entryOffset = entry.getTimeStamp().getTime();
				if (lastEntry != null && lastEntry.getStartOffset().equals(entry.getStartOffset())) {
					LOGGER.info(
							"Found in {} the desired timestamp {} at position {} after {} iterations ({}ms) and no changes during repositioning",
							log, haystackOffset, entry.getStartOffset(), i, System.currentTimeMillis() - start);
					return entry.getStartOffset();
				}
				lastEntry = entry;
				if (entryOffset == haystackOffset) {
					LOGGER.info(
							"Found in {} exactly the desired timestamp {} at position {} after {} iterations ({}ms)",
							log, haystackOffset, entry.getStartOffset(), i, System.currentTimeMillis() - start);
					return entry.getStartOffset();
				} else if (entryOffset < haystackOffset) {
					leftBound = Math.max(mid + 1, logAccess.getDifference(null, entry.getEndOffset()));
					LOGGER.debug("Adjusting left bound, new bounds: {}-{}", leftBound, rightBound);
				} else {
					rightBound = mid - 1;
					LOGGER.debug("Adjusting right bound, new bounds: {}-{}", leftBound, rightBound);
				}
			} else {
				LOGGER.debug(
						"Failed to determine a log entry with a valid timestamp in {} next to position {}, use this as right bound for the further search",
						log, midPointer);
				rightBound = mid - 1;
			}
		}
		if (lastEntry != null) {
			final long lastEntryOffset = lastEntry.getTimeStamp().getTime();
			if (lastEntryOffset >= haystackOffset) {
				LOGGER.info(
						"Found in {} an entry with timestamp {} next to the desired timestamp {} in {} iterations ({}ms), returning the start offset: {}",
						log, lastEntryOffset, haystackOffset, i, System.currentTimeMillis() - start,
						lastEntry.getStartOffset());
				return lastEntry.getStartOffset();
			} else {
				LOGGER.info(
						"Found in {} an entry with timestamp {} before to desired timestamp {} in {} iterations ({}ms), returning the end offset: {}",
						log, lastEntry.getTimeStamp(), haystackOffset, i, System.currentTimeMillis() - start,
						lastEntry.getEndOffset());
				return lastEntry.getEndOffset();

			}
		} else {
			LOGGER.warn("Failed to find in {} after {} iterations ({}ms) an entry near to {}", log, i,
					System.currentTimeMillis() - start, haystackOffset);
		}
		return null;
	}

	private LogEntry getEntryNextTo(final LogPointer p) throws IOException {
		final NextToConsumer c = new NextToConsumer();
		reader.readEntries(log, logAccess, p, c);
		return c.nextToEntry;
	}

	@Override
	public NavigationFuture absolute(final Date offset) throws IOException {
		return new NavigationFuture() {

			@Override
			public LogPointer get() throws IOException {
				return navigate(offset.getTime(), 0, log.getSize());
			}
		};
	}

	private static class NextToConsumer implements LogEntryConsumer {
		private LogEntry nextToEntry;
		private int unformattedCount = 0;

		@Override
		public boolean consume(final Log log, final LogPointerFactory pointerFactory, final LogEntry entry)
				throws IOException {
			if (entry.getTimeStamp() != null) {
				nextToEntry = entry;
				return false;
			} else {
				unformattedCount++;
			}
			return unformattedCount < 5;
		}

	}
}
