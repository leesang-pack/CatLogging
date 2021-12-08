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
package com.catlogging.web.controller.system;

import com.catlogging.app.CatLoggingHome;
import com.catlogging.app.ConfigValueAppConfig;
import com.catlogging.app.MailAppConfig;
import com.catlogging.app.MailAppConfig.MailSettings;
import com.catlogging.settings.http.HttpProxy;
import com.catlogging.settings.http.HttpSettings;
import com.catlogging.system.version.SystemUpdatesCheckTask;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.ConfigValueStore;
import com.catlogging.util.value.Configured;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Slf4j
@RestController
public class GeneralSettingsResourceController {

	public static class MailSettingsBean {
		private String user;
		private String password;
		@NotNull
		private String host;

		@Min(1)
		private int port;

		/**
		 * @return the port
		 */
		public int getPort() {
			return port;
		}

		/**
		 * @param port
		 *            the port to set
		 */
		public void setPort(final int port) {
			this.port = port;
		}

		/**
		 * @return the host
		 */
		public String getHost() {
			return host;
		}

		/**
		 * @param host
		 *            the host to set
		 */
		public void setHost(final String host) {
			this.host = host;
		}

		/**
		 * @return the user
		 */
		public String getUser() {
			return user;
		}

		/**
		 * @param user
		 *            the user to set
		 */
		public void setUser(final String user) {
			this.user = user;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(final String password) {
			this.password = password;
		}
	}

	@Slf4j
	public static class GeneralSettings {
		private String homeDir;
		@NotNull
		@URL
		private String baseUrl;

		private String validationPath;

		@Valid
		private MailSettingsBean mailSettings;

		@Valid
		private HttpProxy httpProxy;

		private boolean systemUpdateCheckEnabled;

		/**
		 * @return the httpProxy
		 */
		public HttpProxy getHttpProxy() {
			return httpProxy;
		}

		/**
		 * @param httpProxy
		 *            the httpProxy to set
		 */
		public void setHttpProxy(final HttpProxy httpProxy) {
			this.httpProxy = httpProxy;
		}

		/**
		 * @return the mailSettings
		 */
		public MailSettingsBean getMailSettings() {
			return mailSettings;
		}

		/**
		 * @param mailSettings
		 *            the mailSettings to set
		 */
		public void setMailSettings(final MailSettingsBean mailSettings) {
			this.mailSettings = mailSettings;
		}

		/**
		 * @return the homeDir
		 */
		public String getHomeDir() {
			return homeDir;
		}

		/**
		 * @param homeDir
		 *            the homeDir to set
		 */
		private void setHomeDir(final String homeDir) {
			this.homeDir = homeDir;
		}

		/**
		 * @return the baseUrl
		 */
		public String getBaseUrl() {
			return baseUrl;
		}

		/**
		 * @param baseUrl
		 *            the baseUrl to set
		 */
		public void setBaseUrl(final String baseUrl) {
			this.baseUrl = baseUrl;
		}

		/**
		 * @return the systemUpdateCheckEnabled
		 */
		public boolean isSystemUpdateCheckEnabled() {
			return systemUpdateCheckEnabled;
		}

		/**
		 * @param systemUpdateCheckEnabled
		 *            the systemUpdateCheckEnabled to set
		 */
		public void setSystemUpdateCheckEnabled(boolean systemUpdateCheckEnabled) {
			this.systemUpdateCheckEnabled = systemUpdateCheckEnabled;
		}

		public String getValidationPath() {
			return validationPath;
		}

		public void setValidationPath(String validationPath) {
			this.validationPath = validationPath;
		}
	}


	@Autowired
	private CatLoggingHome home;

	@Autowired
	private ConfigValueStore configStore;

	@Autowired
	private MailAppConfig mailAppConfig;

	@Autowired
	private MailSettings mailSettings;

	@Autowired
	private HttpSettings httpSettings;

	@Configured(ConfigValueAppConfig.catlogging_BASE_URL)
	private ConfigValue<String> baseUrl;

	@Configured(ConfigValueAppConfig.catlogging_VALIDATION_PATH)
	private ConfigValue<String> validationPath;

	@Configured(value = SystemUpdatesCheckTask.PROP_catlogging_UPDATES_CHECK_ENABLED, defaultValue = "true")
	private ConfigValue<Boolean> updatesCheckEnabled;

	@RequestMapping(value = "/system/settings/general", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public GeneralSettings getGeneralSettings() {
		GeneralSettings settings = new GeneralSettings();
		settings.setHomeDir(home.getHomeDir().getAbsolutePath());
		settings.setBaseUrl(baseUrl.get());
		settings.setValidationPath(validationPath.get());
		settings.setSystemUpdateCheckEnabled(updatesCheckEnabled.get());

		MailSettingsBean mailSettingsBean = new MailSettingsBean();
		mailSettingsBean.setHost(mailSettings.getMailHost().get());
		mailSettingsBean.setPort(mailSettings.getMailPort().get());
		mailSettingsBean.setUser(mailSettings.getMailUser().get());
		mailSettingsBean.setPassword(mailSettings.getMailPassword().get());
		settings.setMailSettings(mailSettingsBean);

		settings.setHttpProxy(httpSettings.getHttpProxy());
		return settings;
	}

	@RequestMapping(value = "/system/settings/general", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public void saveGeneralSettings(@RequestBody @Valid final GeneralSettings settings) throws IOException {
		configStore.store(ConfigValueAppConfig.catlogging_BASE_URL, settings.getBaseUrl());
		configStore.store(ConfigValueAppConfig.catlogging_VALIDATION_PATH, settings.getValidationPath());
		configStore.store(SystemUpdatesCheckTask.PROP_catlogging_UPDATES_CHECK_ENABLED, Boolean.toString(settings.isSystemUpdateCheckEnabled()));

		log.info("Propagate validationPath {}", settings.getValidationPath());

		// Propagate changes to mail sender
		configStore.store(MailSettings.PROP_catlogging_MAIL_HOST, settings.getMailSettings().getHost());
		configStore.store(MailSettings.PROP_catlogging_MAIL_PORT, settings.getMailSettings().getPort() + "");
		configStore.store(MailSettings.PROP_catlogging_MAIL_USER, settings.getMailSettings().getUser());
		configStore.store(MailSettings.PROP_catlogging_MAIL_PASSWORD, settings.getMailSettings().getPassword());
		log.info("Propagate mail settings to mail sender");
		mailAppConfig.refreshMailSenderConfiguration();

		// Propagate http proxy settings
		configStore.store(HttpSettings.PROP_HTTP_PROXY_HOST,
				settings.getHttpProxy() != null ? settings.getHttpProxy().getHost() : null);
		configStore.store(HttpSettings.PROP_HTTP_PROXY_PORT,
				settings.getHttpProxy() != null ? settings.getHttpProxy().getPort() + "" : null);
		configStore.store(HttpSettings.PROP_HTTP_PROXY_USER,
				settings.getHttpProxy() != null && StringUtils.isNotBlank(settings.getHttpProxy().getUser())
						? settings.getHttpProxy().getUser() : null);
		configStore.store(HttpSettings.PROP_HTTP_PROXY_PASSWORD,
				settings.getHttpProxy() != null && StringUtils.isNotBlank(settings.getHttpProxy().getPassword())
						? settings.getHttpProxy().getPassword() : null);
		log.info("Propagate HTTP proxy settings");
		httpSettings.refreshProxySettings();
	}
}
