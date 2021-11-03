package com.catlogging.config;

import com.catlogging.message.h2.H2MessageProvider;
import com.catlogging.message.h2.LanguageEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * message는 동적으로 처리하기 위해 DB에서 키로 읽는 방식으로 한다.
 *
 * @author Tester
 *
 */
@Component("messageSource")
public class DBMessageSource extends AbstractMessageSource {

	@Autowired
	private H2MessageProvider h2MessageProvider;

	private static final String DEFAULT_LOCALE_CODE = "en";

	@Override
	protected MessageFormat resolveCode(String key, Locale locale) {

		LanguageEntity message = h2MessageProvider.findByKeyAndLocale(key, locale.getLanguage());
		if (message == null) {
			message = h2MessageProvider.findByKeyAndLocale(key, DEFAULT_LOCALE_CODE);
		}

		if(message == null || StringUtils.isEmpty(message.getMessagecontent()))
			return new MessageFormat(key, locale);

		return new MessageFormat(message.getMessagecontent(), locale);
	}

	// TODO: 차후에 redis로 이용하는 방식으로 변경.
}
