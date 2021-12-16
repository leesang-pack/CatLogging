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

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.fields.FieldsMap;
import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.model.SeverityLevel;
import com.catlogging.reader.filter.LogEntryFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Abstract filter class concerning transformation of a source field value into
 * a target field.
 *
 *
 * @author Tester
 *
 */
@Slf4j
public abstract class AbstractTransformationFilter<T> implements FieldsFilter, LogEntryFilter {
	@JsonProperty
	@NotNull
	private String targetField;

	@JsonProperty
	@NotNull
	private String sourceField;

	@JsonProperty
	private boolean override = true;

	/**
	 * @return the targetField
	 */
	public String getTargetField() {
		return targetField;
	}

	/**
	 * @param targetField
	 *            the targetField to set
	 */
	public void setTargetField(final String targetField) {
		this.targetField = targetField;
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

	/**
	 * @return the override
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * @param override
	 *            the override to set
	 */
	public void setOverride(final boolean override) {
		this.override = override;
	}

	/**
	 *
	 * @return the type of the transformed target
	 */
	protected abstract FieldBaseTypes getTargetType();

	/**
	 * Transform the string value to the target representation.
	 *
	 * @param sourceValue
	 *            the not empty source value
	 * @return the transformed value or null if transformation fails
	 */
	protected abstract T transform(String sourceValue);

	/**
	 * Returns the fallback value if transformation fails or the source value is
	 * empty or null.
	 *
	 * @return fallback value if transformation fails or the source value is
	 *         empty or null. The fallback can also be null to skip filtering
	 *         totally..
	 */
	protected abstract T getFallback();

	@Override
	public final void filter(final FieldsMap fields) {
		if (override || !fields.containsKey(getTargetField())) {
			final Object sourceStr = fields.get(getSourceField());
			if (sourceStr != null) {
				try {
					final T transformed = transform(sourceStr.toString());
					if (transformed != null) {
						fields.put(getTargetField(), transformed);
						return;
					}
				} catch (final Exception e) {
					log.debug("Failed to transform value", e);
					// Fallback
				}
			}
			final T fallback = getFallback();
			if (override || fallback != null) {
				fields.put(getTargetField(), fallback);
			}
		}
	}

	@Override
	public final void filterKnownFields(final LinkedHashMap<String, FieldBaseTypes> knownFields) {
		knownFields.put(targetField, getTargetType());
	}

	@Override
	public void filterSupportedSeverities(final List<SeverityLevel> severities) {
		// Nothing todo
	}

}
