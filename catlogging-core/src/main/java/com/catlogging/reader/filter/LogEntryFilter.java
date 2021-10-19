package com.catlogging.reader.filter;

import java.util.List;

import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.model.LogEntry;
import com.catlogging.model.SeverityLevel;
import com.catlogging.reader.LogEntryReader;

/**
 * Additional filter capabilities for {@link LogEntry}s.
 * 
 * @author Tester
 *
 */
public interface LogEntryFilter extends FieldsFilter {
	/**
	 * Filters supported severities.
	 * 
	 * @param severities
	 *            fields supported by a {@link LogEntryReader}.
	 */
	void filterSupportedSeverities(List<SeverityLevel> severities);
}
