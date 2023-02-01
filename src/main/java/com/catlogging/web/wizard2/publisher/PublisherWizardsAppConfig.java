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
package com.catlogging.web.wizard2.publisher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.event.publisher.MailPublisher;
import com.catlogging.event.publisher.http.HttpPublisher;
import com.catlogging.web.wizard2.ConfigBeanWizard;
import com.catlogging.web.wizard2.SimpleBeanWizard;

/**
 * Exposes standard wizards for publishers.
 * 
 * @author Tester
 * 
 */
@Configuration
public class PublisherWizardsAppConfig {
	@Bean
	public ConfigBeanWizard<MailPublisher> mailPublisherWizard() {
		return new SimpleBeanWizard<MailPublisher>(
				"catlogging.wizard.publisher.mail", "templates/wizards/publisher/mail",
				MailPublisher.class, new MailPublisher());
	}

	@Bean
	public ConfigBeanWizard<HttpPublisher> httpPublisherWizard() {
		return new SimpleBeanWizard<HttpPublisher>(
				"catlogging.wizard.publisher.http",
				"/ng/wizards/publisher/httpPublisher.html",
				HttpPublisher.class, new HttpPublisher());
	}

	/**
	 * @Bean public ConfigBeanWizard<ShellCommandPublisher>
	 *       shellCommandPublisherWizard() { return new
	 *       SimpleBeanWizard<ShellCommandPublisher>(
	 *       "catlogging.wizard.publisher.shellCommand",
	 *       "/ng/wizards/publisher/shellCommandPublisher.html",
	 *       ShellCommandPublisher.class, new ShellCommandPublisher()); }
	 **/
}
