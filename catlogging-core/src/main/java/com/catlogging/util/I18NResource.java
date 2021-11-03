package com.catlogging.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.catlogging.model.LocaleInfo;
import com.catlogging.util.messages.Message;
import com.catlogging.web.controller.system.GeneralSettingsResourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * REST resource for I18N stuff.
 * 
 * @author Tester
 *
 */
@RestController
public class I18NResource {
	@Autowired
	MessageSource messageSource;

	private static final Logger logger = LoggerFactory.getLogger(I18NResource.class);

	/**
	 * Wrapper for locales and timezones.
	 * 
	 * @author Tester
	 *
	 */
	public static class LocalesAndTimezonesWrapper {
		private final List<Locale> locales;
		private final List<String> timezones;

		public LocalesAndTimezonesWrapper(final List<Locale> locales, final List<String> timezones) {
			super();
			this.locales = new ArrayList<Locale>();
			for (final Locale l : locales) {
				if (l.toString().length() > 0) {
					this.locales.add(l);
				}
			}
			this.timezones = timezones;
		}

		/**
		 * @return the locales
		 */
		public List<Locale> getLocales() {
			return locales;
		}

		/**
		 * @return the timezones
		 */
		public List<String> getTimezones() {
			return timezones;
		}

	}

	/**
	 * Exposes available locales.
	 * 
	 * @return available locales
	 */
	@RequestMapping(path = "utils/i18n/localesAndTimezones", method = RequestMethod.GET)
	LocalesAndTimezonesWrapper availableLocalesAndTimezones() {
		return new LocalesAndTimezonesWrapper(Arrays.asList(Locale.getAvailableLocales()),
				Arrays.asList(TimeZone.getAvailableIDs()));
	}

	@RequestMapping(path = "utils/i18n", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	String getLocales(@RequestBody @Valid final LocaleInfo localeInfo) {
		return messageSource.getMessage(localeInfo.getMessageKey(), new String[]{}, LocaleContextHolder.getLocale());
	}

}
