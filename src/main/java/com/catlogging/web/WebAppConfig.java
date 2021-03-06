/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
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
package com.catlogging.web;

import java.util.List;

import com.catlogging.fields.FieldsMap;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.ISpringTemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Main web app config.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Configuration
public class WebAppConfig implements WebMvcConfigurer {

	/**
	 * static ?????? view ????????? ???????????? url??? ?????? ?????? ????????? ?????? ???????????????
	 * sources/*.jsp
	 * index.jsp
	 * ...
	 * ????????? /c/sources/* ??? ????????? ??? ??????.
	 * @return
	 */
	@Bean
	public ServletRegistrationBean restServlet() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();

		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(CustomServlet.class);
		dispatcherServlet.setApplicationContext(applicationContext);

		ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/c/*");
		servletRegistrationBean.setName("web-mvc");
		servletRegistrationBean.setLoadOnStartup(0);

		return servletRegistrationBean;
	}

	/**
	 * Defines view resolver.
	 * InternalResourceViewResolver??? ????????? ?????? ????????? ????????? ???????????? ???
	 * "order" ??????????????? ?????? ???????????? ?????? ????????? ????????? ??????????????? ?????? ViewResolver??? null??? ????????????, ?????? ??????????????? ?????? ViewResolver??? ?????? ??????
	 * jsp?????? ?????? ???
	 * @return
	 */
	@Bean
	public ViewResolver jspViewResolver() {
		log.debug("INIT [jspViewResolver] Start.");
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		resolver.setOrder(1);

		return resolver;
	}

	/**
	 * Thymeleaf ??? ????????? ??????
	 * thymeleaf ????????? ????????? ??????/view path??? ??? ??? html th??? ?????????
	 * jsp??? ?????? ?????? ????????? ????????? ?????? ???????????????..
	 * @return
	 */

	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		// classpath??? ?????? ???.
		// ?????? webapp?????? ??????
		// ??????????????? path ??????
		templateResolver.setPrefix("classpath:/static/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);

		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		return templateEngine;
	}
	@Bean
	ThymeleafViewResolver viewResolver() {
		log.debug("INIT [ThymeleafViewResolver] Start.");
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		// static??? ???????????? ?????? ???????????? th ??????????????? ??????, ??????????????? ????????????.
		resolver.setViewNames(new String[] {"th/*"});

		// ?????? ??????(???) ??????
		resolver.setCharacterEncoding("UTF-8");;
		resolver.setOrder(0);
		return resolver;
	}

	/**
	 * ????????? ?????? ????????? ????????? ????????? ???????????? ????????????.
	 * ???????????? ????????? ???????????? ????????? ????????????.
	 */
	@Bean
	public LocaleResolver localeResolver() {
		return new CookieLocaleResolver();
	}

	/**
	 * ?????? ????????? ?????? ??????????????? ????????????.
	 * ??????????????? ????????????.
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		registry.addInterceptor(localeChangeInterceptor);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

		final ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		jsonMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		jsonMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

		final SimpleModule module = new SimpleModule("FieldsMapping", Version.unknownVersion());
		module.setSerializerModifier(new BeanSerializerModifier() {
			@Override
			public JsonSerializer<?> modifyMapSerializer(final SerializationConfig config, final MapType valueType,
														 final BeanDescription beanDesc, final JsonSerializer<?> serializer) {
				if (FieldsMap.class.isAssignableFrom(valueType.getRawClass())) {
					return new FieldsMap.FieldsMapMixInLikeSerializer();
				} else {
					return super.modifyMapSerializer(config, valueType, beanDesc, serializer);
				}
			}
		});
		jsonMapper.registerModule(module);

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(jsonMapper);
		converters.add(converter);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		// registry.addConverter(String.class, Class.class,
		// new StringToClassConverter());
	}
}
