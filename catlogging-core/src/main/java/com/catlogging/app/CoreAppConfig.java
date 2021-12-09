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

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfiguredBean;
import com.catlogging.fields.FieldJsonMapper;
import com.catlogging.fields.FieldsMap;
import com.catlogging.fields.FieldsMap.FieldsMapMixInLikeSerializer;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Core app config.
 * 
 * @author Tester
 * 
 */
@Configuration
@Slf4j
@Import({ StartupAppConfig.class, ConfigValueAppConfig.class })
public class CoreAppConfig {
	public static final String BEAN_catlogging_PROPS = "catloggingProps";
	public static final String catlogging_PROPERTIES_FILE = "config.properties";

	@Autowired
	private ApplicationContext context;

	/**
	 * Registers the {@link ContextProvider}.
	 * 
	 * @return the context provider.
	 */
	@Bean
	public ContextProvider contextProvider() {
		ContextProvider.setContext(context);
		return new ContextProvider();
	}

	@Bean(name = { BEAN_catlogging_PROPS })
	@Autowired
	public PropertiesFactoryBean catloggingProperties(final ApplicationContext ctx) throws IOException {
		if (ctx.getEnvironment().acceptsProfiles("!" + ContextProvider.PROFILE_NONE_QA)) {
			final File qaFile = File.createTempFile("catlogging", "qa");
			qaFile.delete();
			final String qaHomeDir = qaFile.getPath();
			log.info("QA mode active, setting random home directory: {}", qaHomeDir);
			System.setProperty("catlogging.home", qaHomeDir);
		}
		final PathMatchingResourcePatternResolver pathMatcher = new PathMatchingResourcePatternResolver();
		Resource[] classPathProperties = pathMatcher.getResources("classpath*:/config/**/catlogging-*.properties");
		final Resource[] metainfProperties = pathMatcher.getResources("classpath*:/META-INF/**/catlogging-*.properties");
		final PropertiesFactoryBean p = new PropertiesFactoryBean();
		for (final Resource r : metainfProperties) {
			classPathProperties = (Resource[]) ArrayUtils.add(classPathProperties, r);
		}
		classPathProperties = (Resource[]) ArrayUtils.add(classPathProperties, new FileSystemResource(System.getProperty("catlogging.home") + "/" + catlogging_PROPERTIES_FILE));
		p.setLocations(classPathProperties);
		p.setProperties(System.getProperties());
		p.setLocalOverride(true);
		p.setIgnoreResourceNotFound(true);
		return p;
	}

	/**
	 * Returns a general properties placeholder configurer based on
	 * {@link #catloggingProperties()}.
	 * 
	 * @param props
	 *            autowired catloggingProperties bean
	 * @return A general properties placeholder configurer.
	 * @throws IOException
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	@Autowired
	public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(
			@Qualifier(BEAN_catlogging_PROPS) final Properties props) throws IOException {
		final PropertyPlaceholderConfigurer c = new PropertyPlaceholderConfigurer();
		c.setIgnoreResourceNotFound(true);
		c.setIgnoreUnresolvablePlaceholders(true);
		c.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
		c.setProperties(props);
		return c;
	}

	@Bean
	public ObjectMapper jsonObjectMapper() {
		final ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jsonMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		jsonMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

		final SimpleModule module = new SimpleModule("FieldsMapping", Version.unknownVersion());
		module.setSerializerModifier(new BeanSerializerModifier() {
			@Override
			public JsonSerializer<?> modifyMapSerializer(final SerializationConfig config, final MapType valueType,
					final BeanDescription beanDesc, final JsonSerializer<?> serializer) {
				if (FieldsMap.class.isAssignableFrom(valueType.getRawClass())) {
					return new FieldsMapMixInLikeSerializer();
				} else {
					return super.modifyMapSerializer(config, valueType, beanDesc, serializer);
				}
			}
		});
		jsonMapper.registerModule(module);
		return jsonMapper;
	}

	/**
	 * Used for proper serilization/deserilization of {@link FieldsMap}s.
	 * 
	 * @return
	 */
	@Bean
	public FieldJsonMapper fieldJsonMapper() {
		return new FieldJsonMapper();
	}

	/**
	 * Used for proper serilization/deserilization of {@link ConfiguredBean}s as
	 * key concept for persisting models in catlogging.
	 * 
	 * @return a {@link BeanConfigFactoryManager} instance
	 */
	@Bean
	public BeanConfigFactoryManager beanConfigFactoryManager() {
		return new BeanConfigFactoryManager();
	}
}
