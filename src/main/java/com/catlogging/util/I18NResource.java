package com.catlogging.util;

import com.catlogging.model.LocaleInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.*;

/**
 * REST resource for I18N stuff.
 * 
 * @author Tester
 *
 */
@Slf4j
@RestController
public class I18NResource {
	@Autowired
	MessageSource messageSource;

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
	Map<String,String> getLocales(@RequestBody @Valid final LocaleInfo localeInfo) {
		Map<String,String> data =new HashMap<>();
		data.put("messageValue", messageSource.getMessage(localeInfo.getMessageKey(), new String[]{}, LocaleContextHolder.getLocale()));
		return data;
	}

}
