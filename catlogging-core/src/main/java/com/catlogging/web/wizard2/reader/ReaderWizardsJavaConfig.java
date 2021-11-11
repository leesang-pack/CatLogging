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
package com.catlogging.web.wizard2.reader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.reader.log4j.Log4jTextReader;
import com.catlogging.web.wizard2.ConfigBeanWizard;
import com.catlogging.web.wizard2.SimpleBeanWizard;

/**
 * Exposes simple reader wizards.
 *
 * @author Tester
 *
 */
@Configuration
public class ReaderWizardsJavaConfig {
	@Bean
	public ConfigBeanWizard<Log4jTextReader> log4jTextReaderWizard() {
		return new SimpleBeanWizard<Log4jTextReader>("catlogging.wizard.reader.log4j", "wizards/readers/log4j",
				Log4jTextReader.class, new Log4jTextReader());
	}

}
