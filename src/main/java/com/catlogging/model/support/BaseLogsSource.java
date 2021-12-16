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
package com.catlogging.model.support;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.catlogging.fields.FieldsMap;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.Navigation.NavigationType;
import com.catlogging.reader.filter.FilteredLogEntryReader;
import com.catlogging.util.json.Views;

/**
 * Base log source.
 * 
 * @author Tester
 * 
 */
public abstract class BaseLogsSource<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>>
		implements LogSource<ACCESSTYPE> {
	@JsonProperty
	@JsonView(Views.Info.class)
	private long id;

	@JsonProperty
	@JsonView(Views.Info.class)
	@NotNull
	private String name;

	@JsonProperty
	@JsonView(Views.Info.class)
	@Valid
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	private FilteredLogEntryReader<ACCESSTYPE> reader = new FilteredLogEntryReader<>();

	@JsonProperty
	@JsonView(Views.Info.class)
	private FieldsMap uiSettings = new FieldsMap();

	@JsonProperty
	@JsonView(Views.Info.class)
	protected boolean readerConfigurable = true;

	/**
	 * @return the id
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the reader
	 */
	@Override
	public FilteredLogEntryReader<ACCESSTYPE> getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	public void setReader(final FilteredLogEntryReader<ACCESSTYPE> reader) {
		this.reader = reader;
	}

	/**
	 * @return the uiSettings
	 */
	@Override
	public FieldsMap getUiSettings() {
		return uiSettings;
	}

	/**
	 * @param uiSettings
	 *            the uiSettings to set
	 */
	public void setUiSettings(final FieldsMap uiSettings) {
		this.uiSettings = uiSettings;
	}

	/**
	 * Indicates if the reader is configurable for this source. Sometimes a log
	 * source brings its own reader.
	 * 
	 * @return the readerConfigurable
	 */
	public boolean isReaderConfigurable() {
		return readerConfigurable;
	}

	/**
	 * Returns the natively supported navigation type by this source.
	 * 
	 * @return the natively supported navigation type by this source.
	 */
	@JsonProperty(access = Access.READ_ONLY)
	@JsonView(Views.Info.class)
	public abstract NavigationType getNavigationType();

	@Override
	public String toString() {
		return "id=" + id + ", name=" + name;
	}
}
