package com.catlogging.system.version;

import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.Notification.Level;
import com.catlogging.system.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.system.version.UpdatesInfoProvider.UpdatesInfoContext;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.Configured;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Date;

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
	private VelocityEngine velocityEngine;

	@Value(value = "${catlogging.version}")
	private String currentVersion;

	@PostConstruct
	public void deleteNotificationForCurrentVersion() {
		try {
			notificationProvider.delete("/static/snippets/system/updateAvailable/" + currentVersion);
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
				final VelocityContext vcontext = new VelocityContext();
				vcontext.put("version", latestStableVersion);
				vcontext.put("context", context);
				final StringWriter titleWriter = new StringWriter();
				velocityEngine.evaluate(vcontext, titleWriter, "TemplateName",
						new InputStreamReader(
								getClass().getResourceAsStream("/static/snippets/system/systemUpdatesNotificationTitle.html"),
								"UTF-8"));
				final StringWriter bodyWriter = new StringWriter();
				velocityEngine.evaluate(vcontext, bodyWriter, "TemplateName",
						new InputStreamReader(
								getClass().getResourceAsStream("/static/snippets/system/systemUpdatesNotificationBody.html"),
								"UTF-8"));
				final Notification n = new Notification();
				n.setId("/static/snippets/system/updateAvailable/" + latestStableVersion.getName());
				n.setTitle(titleWriter.toString());
				n.setMessage(bodyWriter.toString());
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
