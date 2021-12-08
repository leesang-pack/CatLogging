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
package com.catlogging.web.controller.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.validation.Valid;

import com.catlogging.app.ElasticSearchConnect;
import com.catlogging.app.EsSettingsHolder;
import com.catlogging.model.es.EsSettings;
import com.catlogging.model.es.EsStatus;
import com.catlogging.model.es.EsStatusAndSettings;
import com.catlogging.web.controller.report.ElasticEventsController;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Settings REST source for elasticsearch.
 * 
 * @author Tester
 *
 */
@RestController
@Slf4j
public class ElasticSettingsResource {
	@Autowired
	private EsSettingsHolder settingsHolder;
	@Autowired
	private ElasticSearchConnect elasticSearchConnect;

	@RequestMapping(value = "/system/settings/elastic", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public EsStatusAndSettings EsStatusAndSettings() {
		return getStatus(settingsHolder.getSettings());
	}

	@RequestMapping(value = "/system/settings/elastic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public EsStatusAndSettings saveElasticSettings(@RequestBody @Valid final EsSettings settings) throws IOException {
		settingsHolder.storeSettings(settings);
		elasticSearchConnect.initElastic(true);
		return getStatus(settings);

	}

	private EsStatusAndSettings getStatus(final EsSettings settings) {
		final EsStatusAndSettings esas = new EsStatusAndSettings();
		esas.setSettings(settings);
		try {
			RestClient restClient = elasticSearchConnect.getClientConnection().getLowLevelClient();
			Response response = restClient.performRequest("GET", "/_cluster/health");

			ClusterHealthStatus healthStatus;
			try (InputStream is = response.getEntity().getContent()) {
				Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
				healthStatus = ClusterHealthStatus.fromString((String) map.get("status"));
			}

			log.debug("STATUS!!!!!!! " + healthStatus);
			switch (healthStatus) {
				case GREEN:
				case YELLOW:
					esas.setStatus(EsStatus.GREEN);
					break;
				case RED:
					esas.setStatus(EsStatus.RED);
					break;
			}

		} catch (Exception e) {
			esas.setStatus(EsStatus.RED);
			esas.setStatusMessage(e.getMessage());
		}
		return esas;
	}

}
