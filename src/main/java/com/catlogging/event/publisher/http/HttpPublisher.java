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
package com.catlogging.event.publisher.http;

import com.catlogging.config.PostConstructed;
import com.catlogging.event.Event;
import com.catlogging.event.Publisher;
import com.catlogging.event.publisher.EventRenderer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.protocol.HttpContext;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpMethod;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PostConstructed(constructor = HttpPublisherConfigurer.class)
public class HttpPublisher implements Publisher {
	@JsonIgnore
	private EventRenderer renderer;

	@JsonIgnore
	private HttpClient httpClient;

	@JsonIgnore
	private HttpContext httpContext;

	@NotNull
	@JsonProperty
	private HttpMethod method = HttpMethod.GET;

	@NotNull
	@URL
	@JsonProperty
	private String url;

	@JsonProperty
	private String body;

	@JsonProperty
	private Map<String, String> headers = new HashMap<String, String>();

	@JsonProperty
	@NotNull
	private String bodyMimeType = ContentType.TEXT_PLAIN.getMimeType();

	@JsonProperty
	@Valid
	private HttpBasicAuth httpAuthentication;

	@Override
	public void publish(final Event event) throws PublishException {
		Context context = renderer.getContext(event);
		String eventUrl = renderer.render(url, context);
		HttpRequestBase request = null;
		switch (method) {
		case GET:
			request = new HttpGet(eventUrl);
			break;
		case POST:
			request = new HttpPost(eventUrl);
			addBody((HttpPost) request, context);
			break;
		case PUT:
			request = new HttpPut(eventUrl);
			addBody((HttpPut) request, context);
			break;
		case DELETE:
			request = new HttpDelete(eventUrl);
			break;
		case HEAD:
			request = new HttpHead(eventUrl);
			break;
		case OPTIONS:
			request = new HttpOptions(eventUrl);
			break;
		case PATCH:
			request = new HttpPatch(eventUrl);
			break;
		case TRACE:
			request = new HttpTrace(eventUrl);
			break;
		}
		httpAddons(request, event);
		try {
			log.debug("Publishing event {} via HTTP '{}'", event.getId(), request);
			HttpResponse response = httpClient.execute(request, httpContext);
			if (response.getStatusLine().getStatusCode() >= 200
					&& response.getStatusLine().getStatusCode() < 300) {
				log.debug("Published event {} successfuly via HTTP '{}' with status: {}",
						event.getId(), request, response.getStatusLine().getStatusCode());

			} else {
				log.warn(
						"Failed to publish event {} via HTTP '{}' due to status: {} - {}",
						event.getId(),
						request,
						response.getStatusLine().getStatusCode(),
						response.getStatusLine().getReasonPhrase());
				throw new PublishException(
						"Got errornuous HTTP status for pubslihed event: " + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			throw new PublishException("Failed to publish event " + event.getId() + " via HTTP", e);
		}
	}

	protected void addBody(final HttpEntityEnclosingRequestBase request, final Context vCtx) {
		request.setEntity(new StringEntity(
				body != null ?
						renderer.render(body, vCtx)
						:
						"",
				ContentType.create(bodyMimeType, "UTF-8")));
	}

	protected void httpAddons(final AbstractHttpMessage httpMessage, final Event event) {
		if (headers != null) {
			for (String headerKey : headers.keySet()) {
				httpMessage.addHeader(headerKey, headers.get(headerKey));
			}
		}
	}

	/**
	 * @return the method
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(final HttpMethod method) {
		this.method = method;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(final String body) {
		this.body = body;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers
	 *            the headers to set
	 */
	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * Init method for this publisher.
	 * 
	 * @param renderer
	 *            the renderer to set
	 * @param httpClient
	 *            http client
	 */
	protected void init(final EventRenderer renderer, final HttpClient httpClient) {
		this.renderer = renderer;
		this.httpClient = httpClient;
		if (getHttpAuthentication() != null) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
					AuthScope.ANY_PORT), new UsernamePasswordCredentials(
					getHttpAuthentication().getUsername(),
					getHttpAuthentication().getPassword()));
			// Add AuthCache to the execution context
			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
		}
	}

	/**
	 * @return the httpAuthentication
	 */
	public HttpBasicAuth getHttpAuthentication() {
		return httpAuthentication;
	}

	/**
	 * @param httpAuthentication
	 *            the httpAuthentication to set
	 */
	public void setHttpAuthentication(final HttpBasicAuth httpAuthentication) {
		this.httpAuthentication = httpAuthentication;
	}

	/**
	 * @return the bodyMimeType
	 */
	public String getBodyMimeType() {
		return bodyMimeType;
	}

	/**
	 * @param bodyMimeType
	 *            the bodyMimeType to set
	 */
	public void setBodyMimeType(final String bodyMimeType) {
		this.bodyMimeType = bodyMimeType;
	}

}
