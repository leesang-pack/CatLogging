package com.catlogging.util;

import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.Notification.Level;
import com.catlogging.system.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Produces a {@link Notification} for liking catlogging.
 * 
 * @author Tester
 *
 */
@Slf4j
@Component
public class WelcomeNotificationProducer {

	@Autowired
	private NotificationProvider provider;

	@Autowired
	private VelocityEngine velocityEngine;

	@PostConstruct
	public void storeNotification() {
		try {
			Notification likeMe = new Notification();
			likeMe.setId("/static/snippets/system/welcome");
			likeMe.setTitle("Welcome and thank you for trying out catlogging!");

			final VelocityContext vcontext = new VelocityContext();
			final StringWriter bodyWriter = new StringWriter();
			velocityEngine.evaluate(vcontext, bodyWriter, "TemplateName",
					new InputStreamReader(
							getClass().getResourceAsStream("/static/snippets/system/contactsShareTeaser.html"),
							"UTF-8"));

			likeMe.setMessage(bodyWriter.toString());
			likeMe.setLevel(Level.INFO);
			likeMe.setType(Type.TOPIC);
			provider.store(likeMe, false);
		} catch (IOException e) {
			log.info("Failed to store welcome notification", e);
		}
	}

}
