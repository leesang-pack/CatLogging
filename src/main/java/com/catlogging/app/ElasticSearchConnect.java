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
package com.catlogging.app;

import com.catlogging.model.es.EsOperatingType;
import com.catlogging.model.es.RemoteAddress;
import com.catlogging.util.HostInfoProp;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.ConfigValueStore;
import com.catlogging.util.value.Configured;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.JavaHomeOption;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;


@Slf4j
@Configuration
@Import(ConfigValueAppConfig.class)
public class ElasticSearchConnect {

	@Autowired
	CatLoggingHome catloggingHome;
	private static final String ELASTIC_VERSION = "6.8.23";
	static final int HTTP_PORT_VALUE = 9200;
	static final int TCP_PORT_VALUE = 9300;

	private RestHighLevelClient restHighLevelClient;

	private EmbeddedElastic embeddedElastic;

	public static final String PROP_ES_OPS_TYPE = "catlogging.es.operatingType";
	public static final String PROP_ES_REMOTE_ADDRESSES = "catlogging.es.remoteAddresses";

	@Configured(value = PROP_ES_OPS_TYPE, defaultValue = "EMBEDDED")
	private ConfigValue<EsOperatingType> operatingType;

	@Configured(value = PROP_ES_REMOTE_ADDRESSES)
	private ConfigValue<String> remoteAddresses;

	@Autowired
	private ConfigValueStore configValueStore;

	private final Object readLock = new Object();

	@PostConstruct
	public void init(){
		initElastic(false);
	}

	@PreDestroy
	public void destroy(){
		closeCurrentClientConnection();
	}

	///////////////////////////////
	// 최초 configure에서 읽어 온다.
	// 나중에 수정하게 되면 configure를 덮어쓴다.
	@Bean
	public EsSettingsHolder esSettingsHolder() {
		log.debug("ES SettingsHolder Address: [{}]", remoteAddresses.get());
		return new DefaultSettingsHolder(remoteAddresses, configValueStore, operatingType);
	}

	@Synchronized("readLock")
	public void initElastic(boolean isSkipTest){
		closeCurrentClientConnection();

		if (operatingType.get() == EsOperatingType.EMBEDDED)
			initEmbeddedElastic();

		initRestHighLevelClient(isSkipTest);
	}

	public void initEmbeddedElastic() {

		try {
			File esHomeDir = catloggingHome.getHomeDir();

			this.embeddedElastic = EmbeddedElastic.builder()
					.withElasticVersion(ELASTIC_VERSION)
					.withCleanInstallationDirectoryOnStop(false)
					.withSetting(PopularProperties.HTTP_PORT, HTTP_PORT_VALUE)
					.withSetting(PopularProperties.TRANSPORT_TCP_PORT, TCP_PORT_VALUE)
					.withSetting(PopularProperties.CLUSTER_NAME, "embedded")
					.withDownloadDirectory(esHomeDir)
					.withInstallationDirectory(esHomeDir)
					.withEsJavaOpts("-Xms1G -Xmx1G -Dlog4j2.formatMsgNoLookups=true")
					.withStartTimeout(1, MINUTES)
					.withJavaHome(JavaHomeOption.inheritTestSuite())
					.build();

			this.embeddedElastic.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public void initRestHighLevelClient(boolean isSkipTest) {
		try {
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null;}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(X509Certificate[] certs, String authType) { }}};

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, null);

			final List<RemoteAddress> addresses = HostInfoProp.parseHostInfo(remoteAddresses);
			List<HttpHost> hosts = new ArrayList<>();
			for (RemoteAddress remoteAddress : addresses) {
				hosts.add(new HttpHost(InetAddress.getByName(remoteAddress.getHost()), remoteAddress.getPort(), "http"));
			}

			// default conn 1000ms
			RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]))
					.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
							.setKeepAliveStrategy((response, context) -> TimeUnit.MINUTES.toMillis(3))
							.setMaxConnTotal(100)
							.setMaxConnPerRoute(10)
							.setSSLContext(sslContext)
							.setSSLHostnameVerifier((host, session) -> true))
					.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
							.setConnectTimeout(10000)
							.setSocketTimeout(30000));
			RestHighLevelClient client = new RestHighLevelClient(builder);

			restHighLevelClient = client;

			if(isSkipTest)
				return;

			Map<String, String> params = new HashMap<>();
			params.put("wait_for_status", "green");

			RestClient restClient = restHighLevelClient.getLowLevelClient();
			Response response = restClient.performRequest("GET", "/_cluster/health", params);

			ClusterHealthStatus healthStatus;
			try (InputStream is = response.getEntity().getContent()) {
				Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
				healthStatus = ClusterHealthStatus.fromString((String) map.get("status"));
			}

			while (true){
				log.debug("ES STATUS [{}] ",healthStatus);
				if (healthStatus == ClusterHealthStatus.GREEN) {
					break;
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public void closeCurrentClientConnection() {
		log.debug("ES Close Start.");
		try {
			if (restHighLevelClient != null)
				restHighLevelClient.close();
			if(embeddedElastic != null)
				embeddedElastic.stop();
		} catch (Exception e) {
			log.warn("Close...{}", ExceptionUtils.getRootCauseMessage(e));
		}
	}


	public RestHighLevelClient getClientConnection() {
		return this.restHighLevelClient;
	}

}
