package com.catlogging.system.version;

import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.Notification.Level;
import com.catlogging.system.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.system.version.UpdatesInfoProvider.UpdatesInfoContext;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.Configured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * Checks for updates and creates a notification periodically.
 * 
 * @author Tester
 *
 */
@Slf4j
@Component
public class SystemUpdatesCheckTask {
	private static final int FREQUENCY = 1000 * 60 * 60 * 24;

	public static final String PROP_catlogging_UPDATES_CHECK_ENABLED = "catlogging.system.updatesCheckEnabled";

	@Configured(value = PROP_catlogging_UPDATES_CHECK_ENABLED, defaultValue = "true")
	private ConfigValue<Boolean> updatesCheckEnabled;

	@Autowired
	private UpdatesInfoProvider updatesProvider;

	@Autowired
	private NotificationProvider notificationProvider;

	@Autowired
	private ITemplateEngine templateEngine;

	@Value(value = "${catlogging.version}")
	private String currentVersion;

	@PostConstruct
	public void deleteNotificationForCurrentVersion() {
		try {
			notificationProvider.delete("updateAvailable/" + currentVersion);
		} catch (final Exception e) {
			log.warn("Failed to delete obsolete notification for current version", e);
		}
	}

	@Scheduled(fixedDelay = FREQUENCY, initialDelay = 60000)
	public void checkForUpdates() {
		if (!updatesCheckEnabled.get()) {
			log.debug("Updates check disabled by configuration");
			return;
		}
		try {
			final UpdatesInfoContext context = new UpdatesInfoContext() {
				@Override
				public String getCurrentVersion() {
					return currentVersion;
				}
			};
			log.debug("Checking for system updates, current version: [{}]", context.getCurrentVersion());
			final VersionInfo latestStableVersion = updatesProvider.getLatestStableVersion(context);
			if (latestStableVersion.compareTo(new VersionInfo(context.getCurrentVersion())) > 0) {
				log.debug("System update available: {}", latestStableVersion);

				final Context ctx = new Context(Locale.getDefault());
				ctx.setVariable("version", latestStableVersion);
				ctx.setVariable("context", context);
				String processTitle = templateEngine.process("templates/system/utils/systemUpdatesNotificationTitle", ctx);
				String processBody = templateEngine.process("templates/system/utils/systemUpdatesNotificationBody", ctx);

				final Notification n = new Notification();
				n.setId("updateAvailable/" + latestStableVersion.getName());
				n.setTitle(processTitle);
				n.setMessage(processBody);
				n.setLevel(Level.INFO);
				n.setType(Type.TOPIC);
				n.setExpirationDate(new Date(new Date().getTime() + FREQUENCY));
				notificationProvider.store(n, false);
			} else {
				log.debug("System is up to date, got latest stable version: {}", latestStableVersion);
			}
		} catch (final IOException e) {
			log.error("Failed to check for system updates CAUSE:{}", e.getMessage());
		}
	}

}
