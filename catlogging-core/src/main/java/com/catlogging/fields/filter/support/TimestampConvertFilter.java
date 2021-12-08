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
import com.catlogging.model.LogEntry;
import com.catlogging.validators.SimpleDateFormatConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Converts a value in {@link #getSourceField()} into a {@link Date} value
 * regarding the {@link SimpleDateFormat} pattern and stores it in
 * {@link #getTargetField()}.
 *
 * @author Tester
 *
 */
@Slf4j
public class TimestampConvertFilter extends AbstractTransformationFilter<Date> {
	@JsonProperty
	@NotNull
	@SimpleDateFormatConstraint
	private String pattern;

	@JsonProperty
	private Locale locale;

	@JsonProperty
	private String timeZone;

	private SimpleDateFormat parsedPattern;

	public TimestampConvertFilter() {
		setTargetField(LogEntry.FIELD_TIMESTAMP);
	}

	@Override
	protected FieldBaseTypes getTargetType() {
		return FieldBaseTypes.DATE;
	}

	@Override
	protected Date transform(final String sourceValue) {
		try {
			if (parsedPattern == null) {
				if (locale != null) {
					parsedPattern = new SimpleDateFormat(pattern, locale);
				} else {
					parsedPattern = new SimpleDateFormat(pattern);
				}
				if (StringUtils.isNotBlank(timeZone)) {
					parsedPattern.setTimeZone(TimeZone.getTimeZone(timeZone));
				}
			}
			return parsedPattern.parse(sourceValue);
		} catch (ParseException | IllegalArgumentException e) {
			if (log.isDebugEnabled()) {
				log.debug("Failed to parse date in format '" + pattern + "' from string: " + sourceValue, e);
			}
		}
		return null;
	}

	@Override
	protected Date getFallback() {
		return null;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern(final String pattern) {
		this.pattern = pattern;
		parsedPattern = null;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *            the locale to set
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone
	 *            the timeZone to set
	 */
	public void setTimeZone(final String timeZone) {
		this.timeZone = timeZone;
	}
}
