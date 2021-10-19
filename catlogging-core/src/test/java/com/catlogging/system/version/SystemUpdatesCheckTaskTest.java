package com.catlogging.system.version;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catlogging.app.ConfigValueAppConfig;
import com.catlogging.app.CoreAppConfig;
import com.catlogging.system.notification.Notification;
import com.catlogging.system.notification.Notification.Type;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.system.version.UpdatesInfoProvider.UpdatesInfoContext;

/**
 * Test for {@link SystemUpdatesCheckTask}.
 * 
 * @author Tester
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SystemUpdatesCheckTaskTest.HelperAppConfig.class, CoreAppConfig.class,
		ConfigValueAppConfig.class })
public class SystemUpdatesCheckTaskTest {

	/**
	 * Helper app config.
	 * 
	 * @author Tester
	 *
	 */
	@Configuration
	@EnableScheduling
	public static class HelperAppConfig {
		@Bean
		NotificationProvider notificationProvider() {
			return Mockito.mock(NotificationProvider.class);
		}

		@Bean
		UpdatesInfoProvider updProvider() {
			return Mockito.mock(UpdatesInfoProvider.class);
		}

		@Bean
		SystemUpdatesCheckTask task() {
			return new SystemUpdatesCheckTask();
		}

		@Bean
		VelocityEngine vEngine() {
			return new VelocityEngine();
		}
	}

	@Autowired
	private NotificationProvider notificationProvider;

	@Autowired
	private UpdatesInfoProvider updProvider;

	@Autowired
	private SystemUpdatesCheckTask task;

	@Autowired
	@Qualifier(CoreAppConfig.BEAN_catlogging_PROPS)
	private Properties catloggingProperties;

	@Value(value = "${catlogging.version}")
	private String currentVersion;

	@Before
	public void verifyDeletionForCurrentVersion() {
		Mockito.verify(notificationProvider, Mockito.times(1)).delete("/system/updateAvailable/" + currentVersion);
	}

	@Test
	public void testUpdateCheck() throws IOException {
		Mockito.when(updProvider.getLatestStableVersion(Mockito.any(UpdatesInfoContext.class)))
				.thenReturn(new VersionInfo("1.2.3"));
		task.checkForUpdates();
		Mockito.verify(notificationProvider).store(Mockito.argThat(new BaseMatcher<Notification>() {
			@Override
			public boolean matches(final Object arg0) {
				final Notification n = (Notification) arg0;
				return n.getTitle().length() > 0 && n.getType() == Type.TOPIC;
			}

			@Override
			public void describeTo(final Description arg0) {
			}
		}), Mockito.eq(false));
	}

	@Test
	public void testDisabledCheck() throws IOException {
		catloggingProperties.put(SystemUpdatesCheckTask.PROP_catlogging_UPDATES_CHECK_ENABLED, "false");
		task.checkForUpdates();
		Mockito.verifyZeroInteractions(notificationProvider);
	}
}
