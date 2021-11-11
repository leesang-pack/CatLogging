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
package com.catlogging.web.controller;

import java.util.LinkedHashMap;
import java.util.List;

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;

/**
 * Model bean for entries result containing additional field information.
 * 
 * @author Tester
 * 
 */
public class LogEntriesResult {
	private LinkedHashMap<String, FieldBaseTypes> fieldTypes;

	private List<LogEntry> entries;

	private int highlightEntry;

	public LogEntriesResult(final LinkedHashMap<String, FieldBaseTypes> fieldTypes, final List<LogEntry> entries,
			int highlightEntry) {
		this.fieldTypes = fieldTypes;
		this.entries = entries;
		this.highlightEntry = highlightEntry;
	}

	public LogEntriesResult() {
		super();
	}

	/**
	 * @return the fieldTypes
	 */
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * @param fieldTypes
	 *            the fieldTypes to set
	 */
	public void setFieldTypes(final LinkedHashMap<String, FieldBaseTypes> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	/**
	 * @return the entries
	 */
	public List<LogEntry> getEntries() {
		return entries;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries(final List<LogEntry> entries) {
		this.entries = entries;
	}

	public int getHighlightEntry() {
		return highlightEntry;
	}

	public void setHighlightEntry(int highlightEntry) {
		this.highlightEntry = highlightEntry;
	}

}
