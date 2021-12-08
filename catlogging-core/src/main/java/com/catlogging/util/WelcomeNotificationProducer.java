package com.catlogging.util;

import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.Notification.Level;
import com.catlogging.system.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
			log.info("Failed to store welcome notification", e);
		}
	}

}
