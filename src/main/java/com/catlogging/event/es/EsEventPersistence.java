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
package com.catlogging.event.es;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import javax.annotation.PostConstruct;

import com.catlogging.app.ElasticSearchConnect;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.catlogging.aspect.AspectProvider;
import com.catlogging.aspect.PostAspectProvider;
import com.catlogging.event.Event;
import com.catlogging.event.EventPersistence;
import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferPersistence.AspectSniffer;
import com.catlogging.event.SnifferPersistence.SnifferChangedEvent;
import com.catlogging.event.es.EsEventPersistence.AspectEventImpl.AspectEventImplTypeSafeDeserializer;
import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.fields.FieldsMap;
import com.catlogging.model.LogEntry;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.model.support.JsonLogPointer;
import com.catlogging.reader.FormatException;
import com.catlogging.util.DataAccessException;

import net.sf.json.util.JSONBuilder;

/**
 * Elastic search event persistence.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
@Primary
public class EsEventPersistence implements EventPersistence {
	public static final String EVENTS_COUNT = "eventsCount";

	@Autowired
	private LogSourceProvider logSourceProvider;

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private ElasticSearchConnect elasticSearchConnect;

	@Autowired
	private IndexNamingStrategy indexNamingStrategy;

	private ObjectMapper jsonMapper;

	@PostConstruct
	private void initJsonMapper() {
		jsonMapper = new ObjectMapper();
		jsonMapper.configure(MapperFeature.USE_STATIC_TYPING, true);
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.registerSubtypes(LogEntry.class);
		final SimpleModule esModule = new SimpleModule();
		esModule.addSerializer(LogPointer.class, new EsLogPointerSerializer());
		esModule.addDeserializer(LogPointer.class, new EsLogPointerDeserializer());
		esModule.addDeserializer(JsonLogPointer.class, new EsLogPointerDeserializer());
		jsonMapper.registerModule(esModule);
	}

	/**
	 * Returns the type for events.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return type
	 */
	public static String getType(final long snifferId) {
		return "event";
	}

	@Override
	public void refreshIndex() {
		try {
			RefreshRequest refreshRequest =  new RefreshRequest();
			RefreshResponse refreshResponse = elasticSearchConnect.getClientConnection().indices().refresh(refreshRequest, RequestOptions.DEFAULT);
			log.debug("refresh response: {}", refreshResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String persist(final Event event) {
		String evStr = null;
		try {
			evStr = jsonMapper.writeValueAsString(event);

			IndexRequest indexRequest = new IndexRequest()
					.index(indexNamingStrategy.buildActiveName(event.getSnifferId()))
					.type(getType(event.getSnifferId()))
					.source(evStr, XContentType.JSON);

			IndexResponse indexResponse =elasticSearchConnect.getClientConnection().index(indexRequest, RequestOptions.DEFAULT);
			String eventId = indexResponse.getId();
			log.debug("Persisted event with id: {} eventBody:{}", eventId, indexResponse.toString());

			return eventId;
		} catch (final Exception e) {
			throw new DataAccessException("Failed to persiste event: " + evStr, e);
		}
	}

	@Override
	public void delete(final long snifferId, final String[] eventIds) {
		try {
			BulkRequest deletes = new BulkRequest().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
			for (final String id : eventIds) {
				for (final String index : indexNamingStrategy.getRetrievalNames(snifferId)) {
					deletes.add(new DeleteRequest(index, getType(snifferId), id));
				}
			}
			elasticSearchConnect.getClientConnection().bulk(deletes, RequestOptions.DEFAULT);
			log.info("Deleted events: {}", (Object[]) eventIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteAll(final long snifferId) {
		for (final String index : indexNamingStrategy.getRetrievalNames(snifferId)) {
			log.debug("Going to delete all events for sniffer {} by deleting the index(es): {}", snifferId, index);

			//////////////////////////////
			// 인덱스 삭제
			try {
				DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest()
						.indices(index)
						.indicesOptions(IndicesOptions.lenientExpandOpen());

				AcknowledgedResponse deleteIndexResponse = elasticSearchConnect.getClientConnection().indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

				log.info("index delete all:{}",deleteIndexResponse.isAcknowledged());
			} catch (Exception e) {
				log.error("Catched IndexNotFoundException when deleting all events of sniffer: {} ", snifferId);
				e.printStackTrace();
			}

			log.info("Deleted all events for sniffer: {}", snifferId);
		}

		///////////////////////////////////
		// index 재생성
		prepareMapping(snifferId);
	}

	private abstract class EsBaseEventsNativeQueryBuilder<BuilderType extends BaseEventQueryBuilder<?>>
			implements BaseEventQueryBuilder<BuilderType> {
		private int maxHistogramIntervalSlots = -1;
		protected final long snifferId;
		private final int offset, size;

		/**
		 * @param snifferId
		 */
		public EsBaseEventsNativeQueryBuilder(final long snifferId, final int offset, final int size) {
			super();
			this.snifferId = snifferId;
			this.offset = offset;
			this.size = size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public BuilderType withEventCountTimeHistogram(final int maxHistogramIntervalSlots) {
			this.maxHistogramIntervalSlots = maxHistogramIntervalSlots;
			return (BuilderType) this;
		}

		@Override
		public EventsResult list() {
			return list("");
		}

		protected SearchSourceBuilder getBaseRequestBuilder() {
			try {
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
						.from(offset)
						.size(size)
						.sort(SortBuilders.fieldSort(Event.FIELD_TIMESTAMP).order(SortOrder.ASC).unmappedType("date"));

				return searchSourceBuilder;

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected abstract SearchSourceBuilder adaptRequestBuilder(final SearchSourceBuilder requestBuilder);

		public EventsResult list(String test) {
			try {
				final long start = System.currentTimeMillis();
				SearchSourceBuilder searchSourceBuilder = getBaseRequestBuilder();
				searchSourceBuilder = adaptRequestBuilder(searchSourceBuilder);
				SearchRequest searchRequest = new SearchRequest()
						.indices(indexNamingStrategy.getRetrievalNames(snifferId))
						.indicesOptions(IndicesOptions.lenientExpandOpen())
						.types(getType(snifferId))
						.source(searchSourceBuilder);


				EventsCountHistogram histogram = null;
				if (maxHistogramIntervalSlots > 0) {
					AggregationBuilder aggregation = AggregationBuilders.stats("timeRange").field(Event.FIELD_TIMESTAMP);

					final SearchSourceBuilder timeRangeQuery = adaptRequestBuilder(getBaseRequestBuilder().size(0).aggregation(aggregation));
					try {
						SearchRequest timeRangeQuerySearchRequest = new SearchRequest()
								.indices(indexNamingStrategy.getRetrievalNames(snifferId))
								.indicesOptions(IndicesOptions.lenientExpandOpen())
								.types(getType(snifferId))
								.source(timeRangeQuery);
						SearchResponse searchResponse = elasticSearchConnect.getClientConnection().search(timeRangeQuerySearchRequest, RequestOptions.DEFAULT);

						final Aggregations aggregations = searchResponse.getAggregations();
						if (aggregations != null) {
							final Stats timeRangeStats = aggregations.get("timeRange");
							final long timeRange = (long) (timeRangeStats.getMax() - timeRangeStats.getMin());
							log.debug("Time range query: {}", timeRangeQuery);
							log.debug("Retrieved time range for events of sniffer={} in {}ms: {}", snifferId, System.currentTimeMillis() - start, timeRange);
							histogram = new EventsCountHistogram();
							final DateHistogramInterval interval = getInterval(timeRange, maxHistogramIntervalSlots, histogram);
							searchSourceBuilder.aggregation(AggregationBuilders
									.dateHistogram("eventsCount")
									.dateHistogramInterval(interval)
									.field(Event.FIELD_TIMESTAMP)
									.order(BucketOrder.key(true)));

						}
					} catch (final SearchPhaseExecutionException e) {
						log.warn("Events histogram disabled because of exceptions (probably no events?)", e);
					}
				}

				final List<EventPersistence.AspectEvent> events = new ArrayList<>();
				SearchHit[] searchHits = new SearchHit[0];
				SearchResponse searchResponse = elasticSearchConnect.getClientConnection().search(searchRequest, RequestOptions.DEFAULT);
				searchHits = searchResponse.getHits().getHits();
				for (SearchHit searchHit : searchHits) {
					final AspectEventImpl event = jsonMapper.readValue(searchHit.getSourceAsString(), AspectEventImpl.class);
					event.setId(searchHit.getId());
					events.add(event);
				}
				if (histogram != null) {
					histogram.setEntries(new ArrayList<EventPersistence.HistogramEntry>());
					if (searchResponse.getAggregations() != null) {
						for (final Bucket e : ((Histogram) searchResponse.getAggregations().get("eventsCount")).getBuckets()) {
							final DateTime key = (DateTime) e.getKey();
							histogram.getEntries().add(new HistogramEntry(key.getMillis(), e.getDocCount()));
						}
					}
				}

				log.debug("Retrieved events for sniffer={} in {}ms with query: {}", snifferId, System.currentTimeMillis() - start, searchSourceBuilder);
				return new EventsResult(searchResponse.getHits().getTotalHits(), events, histogram);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

	private class EsEventsNativeQueryBuilder extends EsBaseEventsNativeQueryBuilder<NativeQueryBuilder>
			implements NativeQueryBuilder {
		private String nativeQuery;

		public EsEventsNativeQueryBuilder(final long snifferId, final int offset, final int size) {
			super(snifferId, offset, size);
		}

		@Override
		public NativeQueryBuilder withNativeQuery(final String nativeQuery) {
			this.nativeQuery = nativeQuery;
			return this;
		}

		@Override
		protected SearchSourceBuilder adaptRequestBuilder(final SearchSourceBuilder requestBuilder) {
//			return requestBuilder.setExtraSource(nativeQuery);
			return requestBuilder;
		}
	}

	private class EsEventQueryBuilder extends EsBaseEventsNativeQueryBuilder<EventQueryBuilder> implements EventQueryBuilder {

		public EsEventQueryBuilder(final long snifferId, final int offset, final int size) {
			super(snifferId, offset, size);
		}

		private Date from;
		private Date to;

		@Override
		protected SearchSourceBuilder adaptRequestBuilder(final SearchSourceBuilder requestBuilder) {
			QueryBuilder filter = null;
			if (from != null || to != null) {
				final RangeQueryBuilder occRange = QueryBuilders.rangeQuery(Event.FIELD_TIMESTAMP);
				if (from != null) {
					occRange.gte(from.getTime());
				}
				if (to != null) {
					occRange.lte(to.getTime());
				}
				filter = occRange;
			}
			if (filter != null) {
				requestBuilder.query(filter);
			}
			return requestBuilder;
		}

		@Override
		public EventQueryBuilder withOccurrenceFrom(final Date from) {
			this.from = from;
			return this;
		}

		@Override
		public EventQueryBuilder withOccurrenceTo(final Date to) {
			this.to = to;
			return this;
		}

		@Override
		public EventQueryBuilder sortByEntryTimestamp(final boolean desc) {
			// TODO Auto-generated method stub
			return this;
		}
	}

	@Override
	public EventQueryBuilder getEventsQueryBuilder(final long snifferId, final long offset, final int limit) {
		return new EsEventQueryBuilder(snifferId, (int) offset, limit);
	}

	@Override
	public NativeQueryBuilder getEventsNativeQueryBuilder(final long snifferId, final long offset, final int limit) {
		return new EsEventsNativeQueryBuilder(snifferId, (int) offset, limit);
	}

	protected DateHistogramInterval getInterval(final long timeRange, final int maxSlotsCount,
			final EventsCountHistogram histogram) {
		// year, quarter, month, week, day, hour, minute, second
		long dif = timeRange / maxSlotsCount / 1000;
		if (dif <= 0) {
			histogram.setInterval(HistogramInterval.SECOND);
			return DateHistogramInterval.SECOND;
		} else if (dif < 60) {
			histogram.setInterval(HistogramInterval.MINUTE);
			return DateHistogramInterval.MINUTE;
		} else if ((dif = dif / 60) < 60) {
			histogram.setInterval(HistogramInterval.HOUR);
			return DateHistogramInterval.HOUR;
		} else if ((dif = dif / 60) < 24) {
			histogram.setInterval(HistogramInterval.DAY);
			return DateHistogramInterval.DAY;
		} else if ((dif = dif / 24) < 7) {
			histogram.setInterval(HistogramInterval.WEEK);
			return DateHistogramInterval.WEEK;
		} else if ((dif = dif / 7) < 4) {
			histogram.setInterval(HistogramInterval.MONTH);
			return DateHistogramInterval.MONTH;
		}
		histogram.setInterval(HistogramInterval.YEAR);
		return DateHistogramInterval.YEAR;
	}

	@Override
	public Event getEvent(final long snifferId, final String eventId) {
		try {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
					.query(QueryBuilders.idsQuery()
							.addIds(eventId));

			SearchRequest searchRequest = new SearchRequest()
					.indices(indexNamingStrategy.getRetrievalNames(snifferId))
					.indicesOptions(IndicesOptions.lenientExpandOpen())
					.types(getType(snifferId))
					.source(searchSourceBuilder);

			SearchHit[] searchHits = elasticSearchConnect.getClientConnection().search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
			if (searchHits != null && searchHits.length > 0) {
				final SearchHit hit = searchHits[0];
				final Event event = jsonMapper.readValue(hit.getSourceAsString(), Event.class);
				event.setId(hit.getId());
				return event;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@JsonDeserialize(using = AspectEventImplTypeSafeDeserializer.class)
	public static class AspectEventImpl extends Event implements EventPersistence.AspectEvent {
		private static final long serialVersionUID = 255582842708979089L;
		@JsonIgnore
		private final HashMap<String, Object> aspects = new HashMap<String, Object>();

		@Override
		public <AspectType> void setAspect(final String aspectKey, final AspectType aspect) {
			aspects.put(aspectKey, aspect);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <AspectType> AspectType getAspect(final String aspectKey, final Class<AspectType> aspectType) {
			return (AspectType) aspects.get(aspectKey);
		}

		/**
		 * Type safe deserializer for {@link AspectEventImpl}s.
		 * 
		 * @author Tester
		 *
		 */
		public static class AspectEventImplTypeSafeDeserializer extends FieldsMapTypeSafeDeserializer {

			@Override
			protected FieldsMap create() {
				return new AspectEventImpl();
			}

		}
	}

	@Override
	public AspectProvider<AspectSniffer, Integer> getEventsCounter() {
		return new PostAspectProvider<SnifferPersistence.AspectSniffer, Integer>() {

			@Override
			public Integer getApsect(final AspectSniffer host) {
				return host.getAspect(EVENTS_COUNT, Integer.class);
			}

			@Override
			public void injectAspect(final List<AspectSniffer> hosts) {
				try {
					final long start = System.currentTimeMillis();
					final HashMap<Long, AspectSniffer> mapHosts = new HashMap<>();
					final long[] hostIds = new long[hosts.size()];
					int i = 0;
					for (final AspectSniffer s : hosts) {
						hostIds[i++] = s.getId();
						mapHosts.put(s.getId(), s);
						s.setAspect(EVENTS_COUNT, 0);
					}

					AggregationBuilder terms = null;

					terms = AggregationBuilders.terms("eventsCounter")
							.field(Event.FIELD_SNIFFER_ID)
							.includeExclude(new IncludeExclude(hostIds, null));
					if(hostIds.length > 0) {
						terms = AggregationBuilders.terms("eventsCounter")
								.field(Event.FIELD_SNIFFER_ID)
								.includeExclude(new IncludeExclude(hostIds, null))
								.size(hostIds.length);
					}

					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
							.size(0)
							.aggregation(terms);
					SearchRequest searchRequest = new SearchRequest()
							.source(searchSourceBuilder);
					SearchResponse searchResponse = elasticSearchConnect.getClientConnection().search(searchRequest, RequestOptions.DEFAULT);
					log.debug("Performed events counting search {} in {}ms", searchSourceBuilder, System.currentTimeMillis() - start);
					final Terms eventsCounterAgg = searchResponse.getAggregations() != null ? (Terms) searchResponse.getAggregations().get("eventsCounter") : null;
					if (eventsCounterAgg != null) {
						for (final Terms.Bucket entry : eventsCounterAgg.getBuckets()) {
							final long snifferId = entry.getKeyAsNumber().longValue();
							if (mapHosts.containsKey(snifferId)) {
								mapHosts.get(snifferId).setAspect(EVENTS_COUNT, entry.getDocCount());
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	@EventListener
	public void handleOrderCreatedEvent(final SnifferChangedEvent event) {
		prepareMapping(event.getSniffer().getId());
	}

	private void prepareMapping(final long snifferId) {
		log.info("Rebuilding mapping for sniffer {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			log.info("Skip rebuilding mapping due to no more existing sniffer: {}", snifferId);
			return;
		}
		final LinkedHashMap<String, FieldBaseTypes> snifferTypes = new LinkedHashMap<>();

		final LogSource<?> source = logSourceProvider.getSourceById(sniffer.getLogSourceId());
		final LinkedHashMap<String, FieldBaseTypes> entriesTypes = new LinkedHashMap<>();
		try {

			entriesTypes.putAll(source.getReader().getFieldTypes());

		} catch (final FormatException e) {
			log.warn("Failed to access entries fields, these won't be considered", e);
		}

		try {

			final StringWriter jsonMapping = new StringWriter();
			final JSONBuilder mappingBuilder = new JSONBuilder(jsonMapping).object();
			final JSONBuilder props = mappingBuilder.key(getType(snifferId)).object().key("properties").object();

			// TODO: Map sniffer fields dynamically
			props.key(Event.FIELD_TIMESTAMP).object().key("type").value("long").endObject();
			props.key(Event.FIELD_PUBLISHED).object().key("type").value("long").endObject();

			for (final String key : entriesTypes.keySet()) {
				mapField(props, Event.FIELD_ENTRIES + "." + key, entriesTypes.get(key));
			}
			mappingBuilder.endObject().endObject().endObject();

			log.info("Creating mapping for sniffer {}: {}", snifferId, jsonMapping);
			PutMappingRequest request = new PutMappingRequest()
					.source(jsonMapping.toString(),XContentType.JSON)
					.type(getType(snifferId))
					.indices(indexNamingStrategy.buildActiveName(snifferId));

			elasticSearchConnect.getClientConnection().indices()
					.putMappingAsync(
							request,
							RequestOptions.DEFAULT,
							new ActionListener<AcknowledgedResponse>() {

								@Override
								public void onResponse(AcknowledgedResponse acknowledgedResponse) {

								}

								@Override
								public void onFailure(Exception e) {
									// TODO handle failure here
								}
							});

		} catch (Exception e) {
			log.warn("Failed to update mapping for sniffer " + snifferId + ", try to delete all events", e);
		}

		return;
	}

	private void mapField(final JSONBuilder props, final String path, final FieldBaseTypes type) {
		if (type == FieldBaseTypes.DATE) {
			props.key(path).object().key("type").value("long").endObject();
		}
	}
}
