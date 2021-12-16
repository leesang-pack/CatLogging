package com.catlogging.reader.support;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.Log;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.SeverityLevel;
import com.catlogging.reader.FormatException;
import com.catlogging.reader.LogEntryReader;

/**
 * Inverts read direction of the target reader.
 * 
 * @author Tester
 *
 * @param <ACCESSTYPE>
 */
public class InverseReader<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>>
		implements LogEntryReader<ACCESSTYPE> {

	private final LogEntryReader<ACCESSTYPE> targetReader;

	public InverseReader(final LogEntryReader<ACCESSTYPE> targetReader) {
		this.targetReader = targetReader;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return targetReader.getFieldTypes();
	}

	@Override
	public void readEntries(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final com.catlogging.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		targetReader.readEntriesReverse(log, logAccess, startOffset, consumer);
	}

	@Override
	public void readEntriesReverse(final Log log, final ACCESSTYPE logAccess, final LogPointer startOffset,
			final com.catlogging.reader.LogEntryReader.LogEntryConsumer consumer) throws IOException {
		targetReader.readEntries(log, logAccess, startOffset, consumer);
	}

	@Override
	public List<SeverityLevel> getSupportedSeverities() {
		return targetReader.getSupportedSeverities();
	}

}
