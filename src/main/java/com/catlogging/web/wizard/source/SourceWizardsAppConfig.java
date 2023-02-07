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
package com.catlogging.web.wizard.source;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.model.file.RollingLogsSource;
import com.catlogging.model.file.RollingLogsSourceDynamicLiveName;
import com.catlogging.model.file.WildcardLogsSource;
import com.catlogging.source.compound.CompoundLogSource;
import com.catlogging.web.wizard.ConfigBeanWizard;
import com.catlogging.web.wizard.SimpleBeanWizard;

/**
 * Exposes standard wizards for log sources.
 * 
 * @author Tester
 * 
 */
@Configuration
public class SourceWizardsAppConfig {
	@Bean
	public ConfigBeanWizard<WildcardLogsSource> wildcardFileSourceWizard() {
		return new SimpleBeanWizard<WildcardLogsSource>("catlogging.wizard.source.file.wildcard",
				"templates/wizards/source/file.wildcard", WildcardLogsSource.class, new WildcardLogsSource());
	}

	@Bean
	public ConfigBeanWizard<RollingLogsSource> rollingFileStaticLiveSourceWizard() {
		return new SimpleBeanWizard<RollingLogsSource>("catlogging.wizard.source.file.timestampRollingStaticLiveName",
				"templates/wizards/source/timestampRollingFileStaticLiveName", RollingLogsSource.class, new RollingLogsSource());
	}

	@Bean
	public ConfigBeanWizard<RollingLogsSourceDynamicLiveName> rollingFileDynamicLiveSourceWizard() {
		return new SimpleBeanWizard<RollingLogsSourceDynamicLiveName>(
				"catlogging.wizard.source.file.timestampRollingDynamicLiveName",
				"templates/wizards/source/timestampRollingFileDynamicLiveName", RollingLogsSourceDynamicLiveName.class, new RollingLogsSourceDynamicLiveName());
	}

	@Bean
	public ConfigBeanWizard<CompoundLogSource> compoundLogSourceWizard() {
		return new SimpleBeanWizard<CompoundLogSource>("catlogging.wizard.source.compoundLogSource",
				"templates/wizards/source/compoundLog", CompoundLogSource.class, new CompoundLogSource());
	}
}
