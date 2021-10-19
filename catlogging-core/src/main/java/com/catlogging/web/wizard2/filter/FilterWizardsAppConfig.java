package com.catlogging.web.wizard2.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.fields.filter.FieldsFilter;
import com.catlogging.fields.filter.support.JsonParseFilter;
import com.catlogging.fields.filter.support.RegexFilter;
import com.catlogging.fields.filter.support.SeverityMappingFilter;
import com.catlogging.fields.filter.support.TimestampConvertFilter;
import com.catlogging.web.wizard2.ConfigBeanWizard;
import com.catlogging.web.wizard2.SimpleBeanWizard;

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
				"/ng/wizards/filter/regexFilter.html", RegexFilter.class, new RegexFilter());
	}

	@Bean
	public ConfigBeanWizard<SeverityMappingFilter> severityMappingFilterWizard() {
		return new SimpleBeanWizard<SeverityMappingFilter>("catlogging.wizard.filter.severityMappingFilter",
				"/ng/wizards/filter/severityMapping.html", SeverityMappingFilter.class, new SeverityMappingFilter());
	}

	@Bean
	public ConfigBeanWizard<JsonParseFilter> jsonFilterWizard() {
		return new SimpleBeanWizard<JsonParseFilter>("catlogging.wizard.filter.jsonParseFilter",
				"/ng/wizards/filter/jsonParser.html", JsonParseFilter.class, new JsonParseFilter());
	}

	@Bean
	public ConfigBeanWizard<TimestampConvertFilter> timestampConvertFilterWizard() {
		return new SimpleBeanWizard<TimestampConvertFilter>("catlogging.wizard.filter.timestampConvert",
				"/ng/wizards/filter/timestampConvert.html", TimestampConvertFilter.class, new TimestampConvertFilter());
	}

}
