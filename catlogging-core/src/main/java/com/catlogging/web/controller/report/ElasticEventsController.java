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
package com.catlogging.web.controller.report;

import com.catlogging.app.ElasticSearchConnect;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Provides searching for events persisted in the Elasticsearch index.
 * 
 * @author Tester
 * 
 */
@Slf4j
@RestController
public class ElasticEventsController {
	@Value(value = "${catlogging.es.indexName}")
	private String indexName;

	@Autowired
	private ElasticSearchConnect elasticSearchConnect;

	@RequestMapping(value = "/reports/eventSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void eventSearch(final HttpEntity<String> httpEntity,
			final HttpServletResponse response) throws IOException {
		long start = System.currentTimeMillis();
		String jsonRequest = httpEntity.getBody() ;
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		try (XContentParser parser = XContentFactory
				.xContent(XContentType.JSON)
				.createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, jsonRequest))
		{
			searchSourceBuilder.parseXContent(parser);
		}
		SearchRequest searchRequest = new SearchRequest()
				.indices(indexName)
				.indicesOptions(IndicesOptions.lenientExpandOpen())
				.types("event")
				.source(searchSourceBuilder);

		SearchResponse searchResponse = elasticSearchConnect.getClientConnection().search(searchRequest);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		OutputStream responseStream = response.getOutputStream();

		XContentBuilder builder = XContentFactory.jsonBuilder(responseStream);
		builder.startObject();
		searchResponse.toXContent(builder, ToXContent.EMPTY_PARAMS);
		builder.endObject();
		builder.close();
		responseStream.close();

		log.debug("Executed search in {}ms: {}", System.currentTimeMillis() - start, jsonRequest);
	}
}
