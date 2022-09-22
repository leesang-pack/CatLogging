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
package com.catlogging.event.support;

import java.util.LinkedHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.config.PostConstructed;
import com.catlogging.event.Event;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;
import com.catlogging.reader.FormatException;
import com.catlogging.util.grok.Grok;
import com.catlogging.util.grok.GrokConsumerConstructor;
import com.catlogging.util.grok.GrokConsumerConstructor.GrokConsumer;
import com.catlogging.util.grok.GrokMatcher;
import com.catlogging.util.grok.GrokPatternBean;
import com.catlogging.util.grok.GrokPatternBeanJsonModel;
import com.catlogging.util.grok.GroksRegistry;

/**
 * Scans for log entries matching a regex / Grok pattern.
 * 
 * @author Tester
 *
 */
@PostConstructed(constructor = GrokConsumerConstructor.class)
public class RegexPatternScanner extends SingleEntryIncrementalMatcher implements GrokConsumer, GrokPatternBeanJsonModel {
	@JsonIgnore
	private GroksRegistry groksRegistry;

	@JsonProperty
	@NotNull
	@Valid
	private GrokPatternBean grokBean = new GrokPatternBean();

	@JsonProperty
	@NotNull
	private String sourceField = LogEntry.FIELD_RAW_CONTENT;

	@Override
	public Event matches(final LogEntry entry) throws FormatException {
		final Object value = entry.get(sourceField);
		if (value != null) {
			final Grok grok = grokBean.getGrok(groksRegistry);
			final GrokMatcher matcher = grok.matcher(value.toString());
			if (matcher.matches()) {
				final Event event = new Event();
				matcher.setMatchingGroupsToFields(event, false);
				return event;
			}
		}
		return null;
	}

	/**
	 * @return the grokBean
	 */
	public GrokPatternBean getGrokBean() {
		return grokBean;
	}

	/**
	 * @param grokBean
	 *            the grokBean to set
	 */
	public void setGrokBean(final GrokPatternBean grokBean) {
		this.grokBean = grokBean;
	}

	@Override
	public void initGrokFactory(final GroksRegistry groksRegistry) {
		this.groksRegistry = groksRegistry;
	}

	/**
	 * @return the sourceField
	 */
	public String getSourceField() {
		return sourceField;
	}

	/**
	 * @param sourceField
	 *            the sourceField to set
	 */
	public void setSourceField(final String sourceField) {
		this.sourceField = sourceField;
	}

	@Override
	public LinkedHashMap<String, FieldBaseTypes> getFieldTypes() throws FormatException {
		return grokBean.getGrok(groksRegistry).getFieldTypes();
	}

	@Override
	@JsonIgnore
	@Deprecated
	public String getPattern() {
		return grokBean.getPattern();
	}

	@Override
	public void setPattern(final String pattern) {
		grokBean.setPattern(pattern);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isMultiLine() {
		return grokBean.isMultiLine();
	}

	@Override
	public void setMultiLine(final boolean multiLine) {
		grokBean.setMultiLine(multiLine);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isDotAll() {
		return grokBean.isDotAll();
	}

	@Override
	public void setDotAll(final boolean dotAll) {
		grokBean.setDotAll(dotAll);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isCaseInsensitive() {
		return grokBean.isCaseInsensitive();
	}

	@Override
	public void setCaseInsensitive(final boolean caseInsensitive) {
		grokBean.setCaseInsensitive(caseInsensitive);
	}

	@Override
	@JsonIgnore
	@Deprecated
	public boolean isSubStringSearch() {
		return grokBean.isSubStringSearch();
	}

	@Override
	public void setSubStringSearch(final boolean subStringSearch) {
		grokBean.setSubStringSearch(subStringSearch);
	}
}
