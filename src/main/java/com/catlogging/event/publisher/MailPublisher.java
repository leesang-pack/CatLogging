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
package com.catlogging.event.publisher;

import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.config.PostConstructed;
import com.catlogging.event.Event;
import com.catlogging.event.Publisher;
import com.catlogging.model.LogEntry;
import com.catlogging.validators.MailListConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import javax.validation.constraints.NotNull;

/**
 * Publishes an event by sending a mail. The fields subject and text will thy
 *
 * @author Tester
 * 
 */
@Slf4j
@Component
@PostConstructed(constructor = MailPublisher.class)
public class MailPublisher implements Publisher, BeanPostConstructor<MailPublisher> {

	@Autowired
	@JsonIgnore
	private MailSender mailSender;

	@Autowired
	@JsonIgnore
	private EventRenderer renderer;

	@NotNull
	@MailListConstraint
	@JsonProperty
	private String to;

	@NotNull
	@JsonProperty
	private String subject;

	@NotNull
	@org.hibernate.validator.constraints.Email
	@JsonProperty
	private String from;

	@NotNull
	@JsonProperty
	private String textMessage = "Event link: $eventLink\n\nLog entries:\n" + "#foreach( $entry in $event['"
			+ Event.FIELD_ENTRIES + "'] )" + "\n  $entry['" + LogEntry.FIELD_RAW_CONTENT + "']\n" + "#end";

	@Override
	public void publish(final Event event) throws PublishException {
		try {
			final Context context = renderer.getContext(event);
			final SimpleMailMessage email = new SimpleMailMessage();
			email.setFrom(getFrom());
			email.setSubject(renderer.render(getSubject(), context));
			email.setText(renderer.render(getTextMessage(), context) + " ");
			final String to2 = getTo();
			email.setTo(to2.split(",|\\s"));
			mailSender.send(email);
			log.info("Sent event notification to: {}", to2);
		} catch (final MailException e) {
			throw new PublishException("Failed to send event notification to mail: " + getTo(), e);
		}
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to
	 *            the to to set
	 */
	public void setTo(final String to) {
		this.to = to;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 *            the from to set
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * @return the textMessage
	 */
	public String getTextMessage() {
		return textMessage;
	}

	/**
	 * @param textMessage
	 *            the textMessage to set
	 */
	public void setTextMessage(final String textMessage) {
		this.textMessage = textMessage;
	}

//	public void postConstruct(final MailPublisher bean, final BeanConfigFactoryManager configManager)
	@Override
	public void postConstruct(final MailPublisher bean) throws ConfigException {
		bean.mailSender = mailSender;
		bean.renderer = renderer;
	}

}
