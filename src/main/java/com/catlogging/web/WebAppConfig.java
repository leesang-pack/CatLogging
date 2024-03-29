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
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

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
	 * anglerjs에서 route사용 시 html말고 binding된 템플릿을 사용 시
	 * 해당 html을 view에 등록해줘야함.
	 * viewName으로 angler에서 접근함.
	 * @return
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/templates/sniffers/event/detail/**").setViewName("templates/sniffers/event/detail/eventDetail"); //At here, you mapping the nick name and indicate the template is actually located under templates/pages
		registry.addViewController("/templates/sniffers/event/list/**").setViewName("templates/sniffers/event/list/eventsList"); //At here, you mapping the nick name and indicate the template is actually located under templates/pages

		//angler 1.5.3 route를 쓰지 않으면 단순 html load만됨.. angular 안태우고 직접호출 시에는 binding됨.
		//1.8.2 bugfix
		registry.addViewController("/templates/entry/logNavigationByDate").setViewName("templates/entry/logNavigationByDate");
		registry.addViewController("/templates/entry/logPosition").setViewName("templates/entry/logPosition");
		registry.addViewController("/templates/entry/logViewer").setViewName("templates/entry/logViewer");


		// AE 'M' 이아닌 view들.. M은 코멘트 포함
		registry.addViewController("/templates/utils/modelEditor").setViewName("templates/utils/modelEditor");
		registry.addViewController("/templates/entry/viewerFieldsConfig").setViewName("templates/entry/viewerFieldsConfig");
		registry.addViewController("/templates/entry/zoomEntry").setViewName("templates/entry/zoomEntry");
		registry.addViewController("/templates/help/listGroks").setViewName("templates/help/listGroks");
		registry.addViewController("/templates/sources/log/readerTest").setViewName("templates/sources/log/readerTest");
		registry.addViewController("/templates/sniffers/event/snifferTest").setViewName("templates/sniffers/event/snifferTest");
		registry.addViewController("/templates/sniffers/event/statusReposition").setViewName("templates/sniffers/event/statusReposition");
		registry.addViewController("/templates/sniffers/event/statusRepositionZoomBottomBar").setViewName("templates/sniffers/event/statusRepositionZoomBottomBar");

		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	/**
	 * static 말고 view 딴에서 추가적인 url이 필요 시에 다음과 같이 설정해야함
	 * sources/*.jsp
	 * index.html
	 * ...
	 * 뷰들이 /c/sources/* 로 접근할 수 있다.
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
	 * InternalResourceViewResolver는 마지막 우선 순위를 갖도록 지정해야 함
	 * "order" 프로퍼티의 값이 작을수록 우선 순위가 높으며 우선순위가 높은 ViewResolver가 null을 리턴하면, 다음 우선순위를 갖는 ViewResolver에 뷰을 요청
	 * jsp파일 보는 뷰
	 * @return
	 */
//	@Bean
//	public ViewResolver jspViewResolver() {
//		log.debug("INIT [jspViewResolver] Start.");
//		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//		resolver.setPrefix("/WEB-INF/views/");
//		resolver.setSuffix(".jsp");
//		resolver.setOrder(1);
//
//		return resolver;
//	}

	/**
	 * Thymeleaf 뷰 리졸버 설정
	 * thymeleaf 경로로 포함한 요청/view path가 올 시 html th로 뷰전환
	 * jsp와 같이 쓰고 싶을때 순위를 주고 셋팅해야함..
	 * @return
	 */

	@Bean
	public SpringResourceTemplateResolver templateResolver() {
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		// classpath로 안할 시.
		// 기본 webapp에서 찾고
		// 커스텀으로 path 추가
		templateResolver.setPrefix("classpath:/static/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);

		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(templateResolver());
		// Dialect를 쓰기위해 반드시 추가해야함.
		templateEngine.addDialect(layoutDialect());
		return templateEngine;
	}
	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}

	@Bean
	ThymeleafViewResolver viewResolver() {
		log.debug("INIT [ThymeleafViewResolver] Start.");
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(templateEngine());
		// static의 디렉토리 안에 커스텀한 th 디렉토리의 파일, 뷰어주소를 맵핑한다.
		// thumeleaf에서는 안할 시 setViewNames해도 기본적으로 /resources/templates로 찾는다.
		// 하지만 앞에 /static의 기준이므로 서로 찾는 위치가 달라 파일을 접근할 수 없다.
		// 위의 값 토대로
		// static/templates/로 찾게된다.
		resolver.setViewNames(new String[] {"templates/*"});

		// 한글 깨짐(???) 방지
		resolver.setCharacterEncoding("UTF-8");;
		resolver.setOrder(0);
		return resolver;
	}

	/**
	 * 변경된 언어 정보를 기억할 로케일 리졸퍼를 생성한다.
	 * 여기서는 쿠키로 저장하는 방식을 사용한다.
	 */
	@Bean
	public LocaleResolver localeResolver() {
		return new CookieLocaleResolver();
	}

	/**
	 * 언어 변경을 위한 인터셉터를 생성한다.
	 * 인터셉터를 등록한다.
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
