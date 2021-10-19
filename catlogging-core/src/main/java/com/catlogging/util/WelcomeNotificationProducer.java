package com.catlogging.util;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.system.notification.Notification.Level;
import com.catlogging.system.notification.Notification.Type;

/**
 * Produces a {@link Notification} for liking catlogging.
 * 
 * @author Tester
 *
 */
@Component
public class WelcomeNotificationProducer {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private NotificationProvider provider;

	@PostConstruct
	public void storeNotification() {
		try {
			Notification likeMe = new Notification();
			likeMe.setId("/system/welcome");
			likeMe.setTitle("Welcome and thank you for trying out catlogging!");
			likeMe.setMessage(IOUtils.toString(getClass().getClassLoader()
					.getResourceAsStream("META-INF/resources/snippets/system/contactsShareTeaser.html")));
			likeMe.setLevel(Level.INFO);
			likeMe.setType(Type.TOPIC);
			provider.store(likeMe, false);
		} catch (IOException e) {
			logger.info("Failed to store welcome notification", e);
		}
	}

}
