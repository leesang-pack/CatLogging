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
package com.catlogging.fields.filter.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.LogEntry;
import com.catlogging.model.SeverityLevel;

/**
 * Maps a source string field to a severity object.
 *
 * @author Tester
 *
 */
public final class SeverityMappingFilter extends AbstractTransformationFilter<SeverityLevel> {
	@JsonProperty
	@Valid
	private Map<String, SeverityLevel> severityLevels = new HashMap<>();

	private Map<String, SeverityLevel> _levels = null;

	@JsonProperty
	private boolean ignoreCase = true;

	@JsonProperty
	@Valid
	private SeverityLevel fallback;

	{
		setTargetField(LogEntry.FIELD_SEVERITY_LEVEL);
	}

	private void resetInternalLevels() {
		_levels = new HashMap<>();
		if (severityLevels != null) {
			if (ignoreCase) {
				for (final String s : severityLevels.keySet()) {
					_levels.put(s.toLowerCase().trim(), severityLevels.get(s));
				}
			} else {
				for (final String s : severityLevels.keySet()) {
					_levels.put(s.trim(), severityLevels.get(s));
				}
			}
		}
	}

	/**
	 * @return the severityLevels
	 */
	public Map<String, SeverityLevel> getSeverityLevels() {
		return severityLevels;
	}

	/**
	 * @param severityLevels
	 *            the severityLevels to set
	 */
	public void setSeverityLevels(final Map<String, SeverityLevel> severityLevels) {
		this.severityLevels = severityLevels;
		resetInternalLevels();
	}

	/**
	 * @return the fallback
	 */
	@Override
	public SeverityLevel getFallback() {
		return fallback;
	}

	/**
	 * @param fallback
	 *            the fallback to set
	 */
	public void setFallback(final SeverityLevel fallback) {
		this.fallback = fallback;
	}

	/**
	 * @return the ignoreCase
	 */
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	/**
	 * @param ignoreCase
	 *            the ignoreCase to set
	 */
	public void setIgnoreCase(final boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		resetInternalLevels();
	}

	@Override
	public void filterSupportedSeverities(final List<SeverityLevel> severities) {
		severities.addAll(new HashSet<>(severityLevels.values()));
		if (fallback != null) {
			severities.add(fallback);
		}
	}

	@Override
	protected FieldBaseTypes getTargetType() {
		return FieldBaseTypes.SEVERITY;
	}

	@Override
	protected SeverityLevel transform(final String sourceValue) {
		if (_levels == null) {
			resetInternalLevels();
		}
		String strSource = sourceValue.toString().trim();
		if (ignoreCase) {
			strSource = strSource.toLowerCase();
		}
		final SeverityLevel mapping = _levels.get(strSource);
		return mapping;
	}

}
