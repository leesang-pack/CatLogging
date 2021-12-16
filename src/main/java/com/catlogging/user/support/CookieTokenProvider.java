package com.catlogging.user.support;

import com.catlogging.user.UserTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Manages generated token using browser cookies.
 * 
 * @author Tester
 *
 */
@Component
@Slf4j
public class CookieTokenProvider implements UserTokenProvider {
	private final static String COOKIE_KEY = "profileKey";

	@Override
	public String getToken(final HttpServletRequest request, final HttpServletResponse response) {
		final Cookie tokenCookie = WebUtils.getCookie(request, COOKIE_KEY);
		if (tokenCookie != null && tokenCookie.getValue() != null) {
			log.debug("Detected profile token from cookie: {}", tokenCookie.getValue());
			return tokenCookie.getValue();
		}
		final String token = UUID.randomUUID().toString();
		final CookieGenerator g = new CookieGenerator();
		g.setCookieMaxAge(Integer.MAX_VALUE);
		g.setCookiePath("/");
		g.setCookieName(COOKIE_KEY);
		g.addCookie(response, token);
		log.debug("Generated a new token: {}", token);
		return token;
	}

}
