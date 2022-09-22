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

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.config.PostConstructed;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.validators.JsonStringConastraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Parses the string value of a source field and converts it to an (JSON) object
 * field.
 *
 * @author Tester
 *
 */
@Slf4j
@PostConstructed(constructor = JsonParseFilter.JsonParseFilterBuilder.class)
public final class JsonParseFilter extends AbstractTransformationFilter<Object> {
	private ObjectMapper objectMapper;

	@Component
	public static final class JsonParseFilterBuilder implements BeanPostConstructor<JsonParseFilter> {
		@Autowired
		private ObjectMapper objectMapper;

//		final BeanConfigFactoryManager configManager)
		@Override
		public void postConstruct(final JsonParseFilter bean) throws ConfigException {
			bean.objectMapper = objectMapper;
			bean.parseFallback();
		}

	}

	@JsonProperty
	@JsonStringConastraint
	private String fallbackJsonValue;

	private Object fallbackJsonObject;


	/**
	 * @return the fallbackJsonValue
	 */
	public String getFallbackJsonValue() {
		return fallbackJsonValue;
	}

	/**
	 * @param fallbackJsonValue
	 *            the fallbackJsonValue to set
	 */
	public void setFallbackJsonValue(final String fallbackJsonValue) {
		this.fallbackJsonValue = fallbackJsonValue;
		parseFallback();
	}

	private void parseFallback() {
		if (objectMapper != null) {
			if (StringUtils.isNotEmpty(fallbackJsonValue)) {
				try {
					fallbackJsonObject = objectMapper.readValue(
							fallbackJsonValue, Object.class);
				} catch (IOException e) {
					log.warn("Failed to deserialize fallback JSON string: "
							+ fallbackJsonValue, e);
				}
			} else {
				fallbackJsonObject = null;
			}
		}
	}


	@Override
	protected FieldBaseTypes getTargetType() {
		return FieldBaseTypes.OBJECT;
	}

	@Override
	protected Object transform(String sourceValue) {
		try {
			return objectMapper.readValue(sourceValue, Object.class);
		} catch (IOException e) {
			log.trace("Failed to parse source as JSON",e);
			return null;
		}
	}

	@Override
	protected Object getFallback() {
		return fallbackJsonObject;
	}
}
