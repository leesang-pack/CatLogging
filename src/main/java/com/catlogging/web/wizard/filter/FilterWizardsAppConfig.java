package com.catlogging.web.wizard.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.fields.filter.support.JsonParseFilter;
import com.catlogging.fields.filter.support.RegexFilter;
import com.catlogging.fields.filter.support.SeverityMappingFilter;
import com.catlogging.fields.filter.support.TimestampConvertFilter;
import com.catlogging.web.wizard.ConfigBeanWizard;
import com.catlogging.web.wizard.SimpleBeanWizard;

/**
 * Exposes wizards for {@link FieldsFilter}s.
 * 
 * @author Tester
 *
 */
@Configuration
public class FilterWizardsAppConfig {
	@Bean
	public ConfigBeanWizard<RegexFilter> regexFilterWizard() {
		return new SimpleBeanWizard<RegexFilter>("catlogging.wizard.filter.regexFilter",
				"templates/wizards/filter/regexFilter", RegexFilter.class, new RegexFilter());
	}

	@Bean
	public ConfigBeanWizard<SeverityMappingFilter> severityMappingFilterWizard() {
		return new SimpleBeanWizard<SeverityMappingFilter>("catlogging.wizard.filter.severityMappingFilter",
				"templates/wizards/filter/severityMapping", SeverityMappingFilter.class, new SeverityMappingFilter());
	}

	@Bean
	public ConfigBeanWizard<JsonParseFilter> jsonFilterWizard() {
		return new SimpleBeanWizard<JsonParseFilter>("catlogging.wizard.filter.jsonParseFilter",
				"templates/wizards/filter/jsonParser", JsonParseFilter.class, new JsonParseFilter());
	}

	@Bean
	public ConfigBeanWizard<TimestampConvertFilter> timestampConvertFilterWizard() {
		return new SimpleBeanWizard<TimestampConvertFilter>("catlogging.wizard.filter.timestampConvert",
				"templates/wizards/filter/timestampConvert", TimestampConvertFilter.class, new TimestampConvertFilter());
	}

}
