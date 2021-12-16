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
package com.catlogging.web.advice;

import java.security.KeyStore;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.system.notification.NotificationProvider.NotificationSummary;
import com.catlogging.user.UserTokenProvider;
import com.catlogging.web.ViewController;

/**
 * General advice for common model attributes.
 * 
 * @author Tester
 *
 */
@Slf4j
@ControllerAdvice(annotations = ViewController.class)
public class CommonControllerAdvice {
	@Autowired
	@Qualifier(CoreAppConfig.BEAN_catlogging_PROPS)
	private Properties catloggingProps;

	@Autowired
	private NotificationProvider notificationProvider;

	@Autowired
	private UserTokenProvider userTokenProvider;

	/**
	 * Exposes the CoreAppConfig#BEAN_catlogging_PROPS as general modell
	 * attribute under the name "catloggingProps".
	 * 
	 * @return the CoreAppConfig#BEAN_catlogging_PROPS properties object
	 */
	@ModelAttribute("catloggingProps")
	public Properties catloggingProps() {
		return catloggingProps;
	}

	@ModelAttribute("systemNotificationSummary")
	public NotificationSummary systemNotificationSummary(HttpServletRequest request, HttpServletResponse response) {
		return notificationProvider.getSummary(userTokenProvider.getToken(request, response));
	}
}
