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
package com.catlogging.h2;

import com.catlogging.aspect.AspectProvider;
import com.catlogging.aspect.PostAspectProvider;
import com.catlogging.aspect.sql.QueryAdaptor;
import com.catlogging.event.*;
import com.catlogging.h2.jpa.SnifferEventRepository;
import com.catlogging.h2.jpa.SnifferRepository;
import com.catlogging.h2.jpa.SnifferScannerRepository;
import com.catlogging.h2.jpa.SnifferScheduleRepository;
import com.catlogging.model.Log;
import com.catlogging.model.LogSource;
import com.catlogging.model.sniffer.*;
import com.catlogging.model.support.JsonLogPointer;
import com.catlogging.util.PageableResult;
import com.catlogging.model.messages.Message;
import com.catlogging.model.messages.Message.MessageType;
import com.catlogging.util.excption.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * H2 persistence for sniffers.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class H2SnifferPersistence implements SnifferPersistence {

	@Autowired
	private ApplicationEventPublisher appEventPublisher;
	@Autowired
	private SnifferRepository snifferRepository;
	@Autowired
	private SnifferScannerRepository snifferScannerRepository;
	@Autowired
	private SnifferEventRepository snifferEventRepository;
	@Autowired
	private SnifferScheduleRepository snifferScheduleRepository;

	@Override
	public SnifferListBuilder getSnifferListBuilder() {
		return new SnifferListBuilder() {
			private AspectProvider<AspectSniffer, Integer> eventsCounter;
			private QueryAdaptor<AspectSniffer, ScheduleInfo> scheduleInfoAdaptor;

			@Override
			public PageableResult<AspectSniffer> list() {
				List<AspectSniffer> sniffers = (List<AspectSniffer>) snifferRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
				sniffers.stream().forEach(ff->{
					if (scheduleInfoAdaptor != null) {
						Optional<ScheduleInfo> snifferss =	snifferScheduleRepository.findById(SnifferScheduleIdKey.builder().scheduleId(ff.getId()).sniffer(ff.getId()).build());

						ff.setAspect("scheduleInfo",
								ScheduleInfo.builder()
										.scheduled(snifferss.orElseGet(()->ScheduleInfo.builder().build()).isScheduled())
										.lastFireTime(snifferss.orElseGet(()->ScheduleInfo.builder().build()).getLastFireTime())
										.build()
						);
					}
				});

				// sniffer에 추가적으로 aspect 셋팅
				// 참고 : view에서
				//	bean에서 json으로 읽어버림 aspects.scheduleInfo
				// 	bean으로 getAspect접근 aspects.eventsCounter
				final PageableResult<AspectSniffer> result = new PageableResult<SnifferPersistence.AspectSniffer>(-1, sniffers);
				try {
					if (eventsCounter instanceof PostAspectProvider) {
						((PostAspectProvider<AspectSniffer, Integer>) eventsCounter).injectAspect(sniffers);
					}
				} catch (final Exception e) {
					log.error("Failed to access event count", e);
					result.getMessages().add(new Message(MessageType.ERROR, "Failed to access event counts: " + e.getMessage()));
				}
				return result;
			}

			@Override
			public SnifferListBuilder withEventsCounter(final AspectProvider<AspectSniffer, Integer> eventsCounter) {
				this.eventsCounter = eventsCounter;
				return this;
			}

			@Override
			public SnifferListBuilder withScheduleInfo(final QueryAdaptor<AspectSniffer, ScheduleInfo> adaptor) {
				this.scheduleInfoAdaptor = adaptor;
				return this;
			}
		};
	}

	//View에서 보여줄때는 AspectSniffer로 Aspect를 포함한 내용을 보여줘야함.
	@Override
	public Sniffer getSniffer(final long id) {
		return snifferRepository.findByIdOrderByName(id);
	}

	@Override
	public IncrementData getIncrementData(final Sniffer sniffer, final LogSource<?> source, final Log logg) {

		Optional<ScannerIdataInfo> scannerIdataInfo = snifferScannerRepository.findScannerIdataInfoBySnifferIdKeyAndLog(
				SnifferIdKey.builder()
						.sniffer(sniffer.getId())
						.source(source.getId()).build(),
				logg.getPath());
		if(!scannerIdataInfo.isPresent()){
			log.debug("No increment data for sniffer={}, source={} and log={} found, create an empty one",
					sniffer, source, logg);
			return new IncrementData();
		}

		final IncrementData data = new IncrementData();
		data.setData(JSONObject.fromObject(scannerIdataInfo.get().getData()));
		final String jsonStr = scannerIdataInfo.get().getNext_pointer();
		if (StringUtils.isNotBlank(jsonStr)) {
			data.setNextOffset(new JsonLogPointer(jsonStr));
		}
		return data;

	}

	@Override
	public Map<Log, IncrementData> getIncrementDataByLog(final Sniffer sniffer,
														 final LogSource<?> source) throws IOException {
		final List<Log> logs = source.getLogs();
		if (CollectionUtils.isEmpty(logs)) {
			return Collections.emptyMap();
		}

		//db get
		List<String> sLogs= logs.stream().map(f->f.getPath()).collect(Collectors.toList());
		Optional<ScannerIdataInfo> scannerIdataInfo = snifferScannerRepository.findScannerIdataInfoBySnifferIdKeyAndLogIn(
				SnifferIdKey.builder()
						.sniffer(sniffer.getId())
						.source(source.getId()).build(),
				sLogs);

		// result
		final HashMap<Log, IncrementData> incs = new HashMap<Log, IncrementData>();
		final HashMap<String, Log> logMapping = new HashMap<String, Log>();
		for (final Log logg : logs) {
			logMapping.put(logg.getPath(), logg);
		}

		//re make
		final String logPath = scannerIdataInfo.get().getLog();
		final Log logg = logMapping.get(logPath);
		if (logg != null) {
			IncrementData data = new IncrementData();
			data.setData(JSONObject.fromObject(scannerIdataInfo.get().getData()));
			try {
				final String jsonStr = scannerIdataInfo.get().getNext_pointer();
				if (StringUtils.isNotBlank(jsonStr)) {
					data.setNextOffset(source.getLogAccess(logg).getFromJSON(jsonStr));
				}
				incs.put(logg, data);
			} catch (final Exception e) {
				throw new DataAccessException("Failed to construct pointer in log: " + logg, e);
			}
		} else{
			log.error("Didn't find log '{}' for selected incrementdata", logPath);
		}
		// Create empty entries for not yet persisted
		for (final Log log : logMapping.values()) {
			if (!incs.containsKey(log)) {
				incs.put(log, new IncrementData());
			}
		}
		return incs;
	}

	@Override
	public void storeIncrementalData(final Sniffer observer,
									 final LogSource<?> source,
									 final Log logg,
									 final IncrementData data) {
		ScannerIdataInfo scannerIdataInfo = ScannerIdataInfo.builder()
				.next_pointer(data.getNextOffset() != null ? data.getNextOffset().getJson() : "")
				.data(data.getData().toString())
				.log(logg.getPath())
				.snifferIdKey(SnifferIdKey.builder()
					.source(source.getId())
					.sniffer(observer.getId())
					.build())
				.build();

		log.debug("Storing inc data for sniffer={}, source={} and log={} with next offset: {}",
				observer, source, logg, data.getNextOffset());
		snifferScannerRepository.save(scannerIdataInfo);
	}

	// 저장할때는 AspectSniffer로 마춰줘야함.
	// 저장과 보여줄때 AspectSniffer 상속관계의 타입라벨을 마춰줘야기 때문에 (dtype)
	@Override
	public long createSniffer(final Sniffer sniffer) {

		AspectSniffer newSniffer = (AspectSniffer) snifferRepository.save(
				AspectSniffer.builder()
				.scheduleCronExpression(sniffer.getScheduleCronExpression())
				.scanner(sniffer.getScanner())
				.publishers(sniffer.getPublishers())
				.readerStrategy(sniffer.getReaderStrategy())
				.name(sniffer.getName())
				.logSourceId(sniffer.getLogSourceId())
				.id(sniffer.getId()).build());

		final long snifferId = newSniffer.getId();
		sniffer.setId(snifferId);
		log.debug("Persisted new sniffer with id {}", snifferId);
		appEventPublisher.publishEvent(new SnifferChangedEvent(sniffer));
		return snifferId;
	}

	@Override
	public void updateSniffer(final Sniffer sniffer) {
		snifferRepository.save(
				AspectSniffer.builder()
				.scheduleCronExpression(sniffer.getScheduleCronExpression())
				.scanner(sniffer.getScanner())
				.publishers(sniffer.getPublishers())
				.readerStrategy(sniffer.getReaderStrategy())
				.name(sniffer.getName())
				.logSourceId(sniffer.getLogSourceId())
				.id(sniffer.getId()).build());
		log.debug("Updated sniffer {}", sniffer.getId());
		appEventPublisher.publishEvent(new SnifferChangedEvent(sniffer));
	}

	@Override
	@Transactional
	public void deleteSniffer(final Sniffer sniffer) {

		snifferScannerRepository.deleteBySnifferIdKey(
				SnifferIdKey.builder()
						.sniffer(sniffer.getId())
						.source(sniffer.getLogSourceId())
						.build() );
		snifferEventRepository.deleteBySnifferId(sniffer.getId());
		snifferScheduleRepository.deleteBySnifferScheduleIdKey(SnifferScheduleIdKey.builder().sniffer(sniffer.getId()).scheduleId(sniffer.getId()).build());
		snifferRepository.deleteById(sniffer.getId());
		log.info("Deleted sniffer for id: {}", sniffer.getId());
	}

}
