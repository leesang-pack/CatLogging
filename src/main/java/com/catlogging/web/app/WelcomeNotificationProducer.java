package com.catlogging.web.app;

import com.catlogging.model.notification.Notification;
import com.catlogging.model.notification.Notification.Level;
import com.catlogging.model.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Date;
import java.util.Locale;

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
	private ITemplateEngine templateEngine;

	@PostConstruct
	public void storeNotification() throws IOException {
		Notification likeMe = Notification.builder()
				.id("welcome")
				.title("Welcome and thank you for trying out catlogging!")
				.creationDate(new Date())
				.build();
		final Context ctx = new Context(Locale.getDefault());
		// 앞에 '/' 주의
		// static/.. 로 찾기 때문에
		// static// 되면 에러발생.
		String process = templateEngine.process("templates/system/utils/contactsShareTeaser", ctx);

		likeMe.setMessage(process);
		likeMe.setLevel(Level.INFO);
		likeMe.setType(Type.TOPIC);
		provider.store(likeMe, false);
	}

}
