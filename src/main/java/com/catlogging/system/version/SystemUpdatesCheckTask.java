/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
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

package com.catlogging.system.version;

import com.catlogging.model.notification.Notification;
import com.catlogging.model.notification.Notification.Level;
import com.catlogging.model.notification.Notification.Type;
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
			log.warn("Failed to delete obsolete notification for current version : {}", e.getMessage());
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

				final Notification n = Notification.builder()
				.id("updateAvailable/" + latestStableVersion.getName())
				.title(processTitle)
				.message(processBody)
				.level(Level.INFO)
				.type(Type.TOPIC)
				.expirationDate(new Date(new Date().getTime() + FREQUENCY)).build();
				notificationProvider.store(n, false);
			} else {
				log.debug("System is up to date, got latest stable version: {}", latestStableVersion);
			}
		} catch (final IOException e) {
			log.error("Failed to check for system updates CAUSE:{}", e.getMessage());
		}
	}

}
