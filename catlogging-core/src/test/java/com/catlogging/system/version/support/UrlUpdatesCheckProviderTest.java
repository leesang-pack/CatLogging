package com.catlogging.system.version.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.catlogging.app.ConfigValueAppConfig;
import com.catlogging.app.CoreAppConfig;
import com.catlogging.settings.http.HttpSettings;
import com.catlogging.system.version.UpdatesInfoProvider.UpdatesInfoContext;
import com.catlogging.system.version.VersionInfo;

/**
 * Test for {@link UrlUpdatesCheckProvider}.
 * 
 * @author Tester
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { UrlUpdatesCheckProviderTest.HelperAppConfig.class, CoreAppConfig.class,
		ConfigValueAppConfig.class })
public class UrlUpdatesCheckProviderTest {
	/**
	 * Helper app config.
	 * 
	 * @author Tester
	 *
	 */
	@Configuration
	public static class HelperAppConfig {

		@Bean
		public HttpSettings httpSettings() {
			return new HttpSettings();
		}

		@Bean
		public UrlUpdatesCheckProvider provider() {
			return new UrlUpdatesCheckProvider();
		}

	}

	private static int port = 8098;

	static {
		ServerSocket s = null;
		try {
			s = new ServerSocket(0);
			port = s.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(port);

	@Autowired
	private UrlUpdatesCheckProvider provider;

	@Autowired
	@Qualifier(CoreAppConfig.BEAN_catlogging_PROPS)
	private Properties catloggingProperties;

	@PostConstruct
	public void setUpUpdatesCheckUrl() {
		catloggingProperties.setProperty(UrlUpdatesCheckProvider.PROP_catlogging_UPDATES_CHECK_URL,
				"http://localhost:" + port + "/versionCheck.php?currentVersion={0}");
	}

	@Test
	public void testValid() throws IOException {
		stubFor(get(urlEqualTo("/versionCheck.php?currentVersion=1.2.3"))
				.willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json").withBody(
						"{ stable: { version: \"1.5.3\", features: true, bugfixes: true, security: false } }")));
		VersionInfo version = provider.getLatestStableVersion(new UpdatesInfoContext() {
			@Override
			public String getCurrentVersion() {
				return "1.2.3";
			}
		});
		Assert.assertEquals("1.5.3", version.getName());
		Assert.assertTrue(version.isBugfixes());
		Assert.assertTrue(version.isFeatures());
		Assert.assertFalse(version.isSecurity());
	}

	@Test(expected = IOException.class)
	public void testIOE() throws IOException {
		provider.getLatestStableVersion(Mockito.mock(UpdatesInfoContext.class));
	}
}
