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
package com.catlogging.validators;

import com.catlogging.app.ContextProvider;
import com.catlogging.validators.JsonStringConastraint.JsonValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.io.IOException;
import java.lang.annotation.*;

/**
 * Validates a string value for JSON syntax.
 * 
 * @author Tester
 * 
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = JsonValidator.class)
public @interface JsonStringConastraint {
	String message() default "Not a valid JSON syntax";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public static class JsonValidator implements
			ConstraintValidator<JsonStringConastraint, String> {
		private static Logger logger = LoggerFactory
				.getLogger(JsonValidator.class);
		private ObjectMapper objectMapper;

		@Override
		public void initialize(final JsonStringConastraint arg0) {
			objectMapper = ContextProvider.getContext().getBean(
					ObjectMapper.class);
		}

		@Override
		public boolean isValid(final String expr,
				final ConstraintValidatorContext ctx) {
			try {
				if (StringUtils.isNotEmpty(expr)) {
					objectMapper.readValue(expr, Object.class);
				}
				return true;
			} catch (IOException e) {
				ctx.buildConstraintViolationWithTemplate("Not a valid JSON syntax: "
						+ e.getMessage());
				logger.info("Expression '{}' isn't a valid JSON object: {}",
						expr, e.getMessage());
				return false;
			}
		}

	}
}
