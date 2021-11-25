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
package com.catlogging.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.NodeBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.node.Node;
//import org.elasticsearch.node.NodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.ConfigValueStore;
import com.catlogging.util.value.Configured;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Elasticsearch app config.
 * 
 * @author Tester
 * 
 */

@Slf4j
@Configuration
@Import(ConfigValueAppConfig.class)
public class ElasticSearchAppConfig {
	private static final String ELASTIC_VERSION = "6.3.0";
	static final int HTTP_PORT_VALUE = 9999;
	static final int TCP_PORT_VALUE = 9998;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Bean
	public EmbeddedElastic initEmbeddedElastic() {

		try {
			File esHomeDir = new File("/home/oem/catlogging");

			EmbeddedElastic embeddedElastic = EmbeddedElastic.builder()
					.withElasticVersion(ELASTIC_VERSION)
					.withSetting(PopularProperties.HTTP_PORT, HTTP_PORT_VALUE)
					.withSetting(PopularProperties.TRANSPORT_TCP_PORT, TCP_PORT_VALUE)
					.withSetting(PopularProperties.CLUSTER_NAME, "embedded")
					.withInstallationDirectory(esHomeDir)
					.withEsJavaOpts("-Xms1G -Xmx1G")
					.withStartTimeout(1, MINUTES)
					.build();

			embeddedElastic.start();

			Thread.sleep(10000);

			return embeddedElastic;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	@Bean
//	public EsSettingsHolder esSettingsHolder() {
//		return new DefaultSettingsHolder();
//	}

	@Bean
	@Autowired
	public RestHighLevelClient initRestHighLevelClient(EmbeddedElastic embeddedElastic) {
		try {
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null;}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(X509Certificate[] certs, String authType) { }}};
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, null);

			// default conn 1000ms
			RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", HTTP_PORT_VALUE))
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

			Map<String, String> params = new HashMap<>();
			params.put("wait_for_status", "green");

			RestClient restClient = client.getLowLevelClient();
			Response response = restClient.performRequest("GET", "/_cluster/health", params);

			ClusterHealthStatus healthStatus;
			try (InputStream is = response.getEntity().getContent()) {
				Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
				healthStatus = ClusterHealthStatus.fromString((String) map.get("status"));
			}

			while (true){
				log.debug("STATUS!!!!!!! " + healthStatus);
				if (healthStatus == ClusterHealthStatus.GREEN) {
					break;
				}
				Thread.sleep(1000);
			}

			return client;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

//	@PreDestroy
//	public void closeCurrentClientConnection() {
//		try {
//			if (client != null)
//				client.close();
//			if(embeddedElastic != null)
//				embeddedElastic.stop();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

//	static EmbeddedElastic embeddedElastic;

	private static interface ClientConnection {

		public RestHighLevelClient getClient();

		public void close();
	}

	/**
	 * Indicates whether elasticsearch is operated locally as embedded instance
	 * or by connecting to a remote cluster.
	 * 
	 * @author Tester
	 *
	 */
	public enum EsOperatingType {
		EMBEDDED, REMOTE
	}

	/**
	 * Remote address representation.
	 * 
	 * @author Tester
	 *
	 */
	public static final class RemoteAddress {
		@NotEmpty
		private String host;
		@Min(1)
		private int port = 9300;

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

		@Override
		public String toString() {
			return host + ":" + port;
		}

	}

	/**
	 * Holds settings for elasticsearch.
	 * 
	 * @author Tester
	 *
	 */
//	public static interface EsSettingsHolder {
//		/**
//		 * Returns current es settings.
//		 *
//		 * @return current es settings
//		 */
//		public EsSettings getSettings();
//
//		/**
//		 * Stores new es settings and applies it to the elasticsearch
//		 * connection.
//		 *
//		 * @param settings
//		 * @throws IOException
//		 */
//		public void storeSettings(final EsSettings settings) throws IOException;
//	}

	/**
	 * Default settings holder for elasticsearch.
	 * 
	 * @author Tester
	 *
	 */
//	private final class DefaultSettingsHolder implements EsSettingsHolder {
//		public static final String PROP_ES_OPS_TYPE = "catlogging.es.operatingType";
//		public static final String PROP_ES_REMOTE_ADDRESSES = "catlogging.es.remoteAddresses";
//
//		@Configured(value = PROP_ES_OPS_TYPE, defaultValue = "EMBEDDED")
//		private ConfigValue<EsOperatingType> operatingType;
//
//		private final Pattern ADDRESS_PATTERN = Pattern.compile("\\s*([^:]+):(\\d+)\\s*,?");
//		@Configured(value = PROP_ES_REMOTE_ADDRESSES)
//		private ConfigValue<String> remoteAddresses;
//
//		@Autowired
//		private ConfigValueStore configValueStore;
//
//		private EsSettings settings;
//
//		@Override
//		public EsSettings getSettings() {
//			if (settings == null) {
//				settings = new EsSettings();
//				settings.setOperatingType(operatingType.get());
//				final List<RemoteAddress> addresses = new ArrayList<>();
//				if (settings.getOperatingType() == EsOperatingType.REMOTE) {
//					log.info("Building remote addresses from config: {}", remoteAddresses.get());
//					final Matcher m = ADDRESS_PATTERN.matcher(remoteAddresses.get());
//					while (m.find()) {
//						final RemoteAddress ra = new RemoteAddress();
//						ra.setHost(m.group(1));
//						ra.setPort(Integer.parseInt(m.group(2)));
//						addresses.add(ra);
//					}
//					log.info("Built remote addresses from config: {}", addresses);
//					settings.setRemoteAddresses(addresses);
//				}
//			}
//			return settings;
//		}
//
//		@Override
//		public synchronized void storeSettings(final EsSettings settings) throws IOException {
//			configValueStore.store(PROP_ES_OPS_TYPE, settings.getOperatingType().toString());
//			if (settings.getOperatingType() == EsOperatingType.REMOTE) {
//				final StringBuilder addresses = new StringBuilder();
//				for (final RemoteAddress a : settings.getRemoteAddresses()) {
//					if (addresses.length() > 0) {
//						addresses.append(",");
//					}
//					addresses.append(a.getHost() + ":" + a.getPort());
//				}
//				configValueStore.store(PROP_ES_REMOTE_ADDRESSES, addresses.toString());
//			}
//			this.settings = settings;
//			closeCurrentClientConnection();
//		}
//	}

	/**
	 * Bean for elasticsearch settings.
	 * 
	 * @author Tester
	 *
	 */
	public static final class EsSettings {
		@NotNull
		private EsOperatingType operatingType = EsOperatingType.EMBEDDED;

		@Valid
		private List<RemoteAddress> remoteAddresses;

		/**
		 * @return the operatingType
		 */
		public EsOperatingType getOperatingType() {
			return operatingType;
		}

		/**
		 * @param operatingType
		 *            the operatingType to set
		 */
		public void setOperatingType(final EsOperatingType operatingType) {
			this.operatingType = operatingType;
		}

		/**
		 * @return the remoteAddresses
		 */
		public List<RemoteAddress> getRemoteAddresses() {
			return remoteAddresses;
		}

		/**
		 * @param remoteAddresses
		 *            the remoteAddresses to set
		 */
		public void setRemoteAddresses(final List<RemoteAddress> remoteAddresses) {
			this.remoteAddresses = remoteAddresses;
		}

	}

//	public static interface EsClientBuilder {
//		Client buildFromSettings(EsSettings settings);
//	}

	/**
	 * Client callback.
	 * 
	 * @author Tester
	 * 
	 * @param <T>
	 *            return type
	 */
//	public static interface ClientCallback<T> {
//		/**
//		 * Executes callback code using an acquired client, which is closed
//		 * safely after the callback.
//		 *
//		 * @param client
//		 *            acquired client to use
//		 * @return return value
//		 */
//		public T execute(RestHighLevelClient client);
//	}

	/**
	 * Template helper class to centralize node/client requests.
	 * 
	 * @author Tester
	 * 
	 */
//	public interface ElasticClientTemplate {
//		public <T> T executeWithClient(final ClientCallback<T> callback);
//	}

//	private synchronized RestHighLevelClient getClientConnection(final EsSettings settings) {
//		if (clientConnection == null) {
//			if (settings.getOperatingType() == EsOperatingType.EMBEDDED) {
//				buildLocalEmbeddedNode();

//				final Node localEmbeddedNode = buildLocalEmbeddedNode();
//				final RestHighLevelClient client = localEmbeddedNode.client();
//				clientConnection = new ClientConnection() {
//					@Override
//					public RestHighLevelClient getClient() {
//						return client;
//					}
//
//					@Override
//					public void close() {
//						log.info("Closing local embedded elasticsearch node");
//						try {
//							client.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						localEmbeddedNode.close();
//					}
//				};
//			} else {
//				log.info("Establishing remote elasticsearch connection to: {}", settings.getRemoteAddresses());
//				List<HttpHost> hosts = new ArrayList<>();
//				for (final RemoteAddress a : settings.getRemoteAddresses()) {
//					try {
//						hosts.add(new HttpHost(InetAddress.getByName(a.getHost()), a.getPort(), "http"));
//					} catch (final UnknownHostException e) {
//						log.warn("Failed to resolve ES host, it'll be ignored: " + a.getHost(), e);
//					}
//				}
//				client = new RestHighLevelClient(RestClient.builder(hosts.toArray(new HttpHost[hosts.size()])));
//				clientConnection = new ClientConnection() {
//
//					@Override
//					public RestHighLevelClient getClient() {
//						return client;
//					}
//
//					@Override
//					public void close() {
//						try {
//							client.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						log.info("Closing remote elasticsearch connection to: {}", settings.getRemoteAddresses());
//					}
//				};
//			}

//			final RestHighLevelClient client = clientConnection.getClient();
//			Client client client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();


			// if (!client.admin().indices().exists(new
			// IndicesExistsRequest(indexName)).actionGet().isExists()) {
			// log.info("Created elasticsearch index: {}", indexName);
			// client.admin().indices().create(new
			// CreateIndexRequest(indexName)).actionGet();
			// }

//		}
//		return clientConnection;
//		return client;
//	}



	/**
	 * Exposes the elasticsearch settings holder.
	 * 
	 * @return elasticsearch settings holder
	 */

//	@Bean
//	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//	@Autowired
//	public ElasticClientTemplate clientTemplate(final EsSettingsHolder settingsHolder) {
//		return new ElasticClientTemplate() {
//			@Override
//			public <T> T executeWithClient(final ClientCallback<T> callback) {
//				final RestHighLevelClient c = getClientConnection(settingsHolder.getSettings());
//				return callback.execute(c);
//			}
//		};
//	}
//
//	public String getIndexName(){
//		return indexName;
//	}
//
//	public RestHighLevelClient getClient(){
//		return client;
//	}
//
//	public EmbeddedElastic getEmbeddedElastic(EsSettingsHolder esSettingsHolder){
//		getClientConnection(esSettingsHolder.getSettings());
//		return embeddedElastic;
//	}

	private Stream<String> searchForDocuments(Optional<String> indexMaybe, Optional<String> routing, RestHighLevelClient restHighLevelClient) {
		String searchCommand = prepareQuery(indexMaybe, routing);
		String body = fetchDocuments(searchCommand, restHighLevelClient);
		return parseDocuments(body);
	}

	private String prepareQuery(Optional<String> indexMaybe, Optional<String> routing) {

		String routingQueryParam = routing
				.map(r -> "?routing=" + r)
				.orElse("");

		return indexMaybe
				.map(index -> "/" + index + "/_search" + routingQueryParam)
				.orElse("/_search");
	}

	private String fetchDocuments(String searchCommand, RestHighLevelClient restHighLevelClient) {
//		HttpGet request = new HttpGet(url(searchCommand));
//		return restHighLevelClient.execute(request, response -> {
//			assertOk(response, "Error during search (" + searchCommand + ")");
//			return readBodySafely(response);
//		})

		try {
			RestClient restClient = restHighLevelClient.getLowLevelClient();
			Response response = restClient.performRequest("GET", searchCommand);

			return IOUtils.toString(response.getEntity().getContent(), UTF_8);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private Stream<String> parseDocuments(String body) {
		try {
			JsonNode jsonNode = OBJECT_MAPPER.readTree(body);
			return StreamSupport.stream(jsonNode.get("hits").get("hits").spliterator(), false)
					.map(hitNode -> hitNode.get("_source"))
					.map(JsonNode::toString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
