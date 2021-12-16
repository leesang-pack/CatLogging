package com.catlogging.system.version.support;

import com.catlogging.settings.http.HttpSettings;
import com.catlogging.system.version.UpdatesInfoProvider;
import com.catlogging.system.version.VersionInfo;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.Configured;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Retrieves version info from a HTTP URL with support for following JSON
 * response: { stable: { version: "0.5.3", features: true, bugfixes: true,
 * security: false } }
 * 
 * @author Tester
 *
 */
@Slf4j
@Component
public class UrlUpdatesCheckProvider implements UpdatesInfoProvider {
	public static final String PROP_catlogging_UPDATES_CHECK_URL = "catlogging.system.updatesCheckUrl";

	@Autowired
	private HttpSettings httpSettings;

	@Autowired
	private ObjectMapper objectMapper;

	@Configured(value = PROP_catlogging_UPDATES_CHECK_URL, defaultValue = "http://www.catlogging.com/versionCheck.php?version={0}")
	private ConfigValue<String> updatesCheckUrlValue;

	/**
	 * {@link VersionInfo} mapped for JSON input.
	 * @author Tester
	 *
	 */
	public static class VersionInfoInputMapped extends VersionInfo {
		@JsonProperty("version")
		@Override
		public String getName() {
			return super.getName();
		}

		@JsonProperty("features")
		@Override
		public boolean isFeatures() {
			return super.isFeatures();
		}

		@JsonProperty("bugfixes")
		@Override
		public boolean isBugfixes() {
			return super.isBugfixes();
		}

		@JsonProperty("security")
		@Override
		public boolean isSecurity() {
			return super.isSecurity();
		}
		
	}
	
	/**
	 * Wrapper for the HTTP JSON response.
	 * 
	 * @author Tester
	 *
	 */
	public static class JsonResponseWrapper {
		private VersionInfoInputMapped stable;

		/**
		 * @return the stable
		 */
		public VersionInfoInputMapped getStable() {
			return stable;
		}

		/**
		 * @param stable
		 *            the stable to set
		 */
		public void setStable(VersionInfoInputMapped stable) {
			this.stable = stable;
		}
	}

	@Override
	public VersionInfo getLatestStableVersion(UpdatesInfoContext context) throws IOException {
		HttpClient client = httpSettings.createHttpClientBuilder().build();
		HttpGet get = new HttpGet(MessageFormat.format(updatesCheckUrlValue.get(), context.getCurrentVersion()));
		log.debug("Calling '{}' to get updates info for: {}", get, context);
		HttpResponse response = client.execute(get);
		JsonResponseWrapper wrapper = objectMapper.readValue(response.getEntity().getContent(),
				JsonResponseWrapper.class);
		return wrapper.getStable();
	}

}
