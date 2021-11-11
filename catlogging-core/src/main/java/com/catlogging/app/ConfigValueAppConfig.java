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
package com.catlogging.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

import com.catlogging.util.value.ConfigInjector;
import com.catlogging.util.value.support.PropertiesBasedSource;

/**
 * Enables the configuration injection and refreshing.
 * 
 * @author Tester
 * 
 */
@Configuration
public class ConfigValueAppConfig {
	public static final String catlogging_BASE_URL = "catlogging.baseUrl";

	public static final String catlogging_VALIDATION_PATH="catlogging.validationPath";

	@Bean
	public ConfigInjector configInjector() {
		return new ConfigInjector();
	}

	@Bean
	public PropertiesBasedSource propertiesBasedSource() {
		return new PropertiesBasedSource();
	}

	@Bean
	public ConversionService conversionService() {
		return new DefaultFormattingConversionService();
	}
}
