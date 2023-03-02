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

package com.catlogging.web.controller.sniffer;

import com.catlogging.event.IncrementData;
import com.catlogging.model.sniffer.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferScheduler;
import com.catlogging.model.*;
import com.catlogging.model.support.JsonLogPointer;
import com.catlogging.util.excption.ResourceNotFoundException;
import com.catlogging.web.controller.sniffer.SnifferStatusController.LogSniffingStatus;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for sniffer status methods.
 * 
 * @author Tester
 *
 */
@Slf4j
@RestController
public class SnifferStatusResourceController {

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private LogSourceProvider sourceProvider;

	@Autowired
	protected SnifferScheduler snifferScheduler;

	@RequestMapping(value = "/sniffers/{snifferId}/status/pointerOffset", method = RequestMethod.POST)
	@ResponseBody
	LogSniffingStatus updateStatusByPointer(@PathVariable("snifferId") final long snifferId,
			@RequestParam("log") final String logPath, @RequestBody final JsonLogPointer pointer)
			throws IOException, ResourceNotFoundException {
		final Sniffer sniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(sniffer);
		final Log log = source.getLog(logPath);
		if (log == null) {
			throw new ResourceNotFoundException(Log.class, logPath);
		}
		final LogRawAccess<? extends LogInputStream> logAccess = source.getLogAccess(log);
		final LogPointer targetPointer = logAccess.getFromJSON(pointer.getJson());
		final LogPointer refreshedPointer = logAccess.refresh(targetPointer).get();
		final long offset = logAccess.getDifference(null, refreshedPointer);
		return new LogSniffingStatus(log, refreshedPointer, offset, log.getSize());
	}

	@RequestMapping(value = "/sniffers/{snifferId}/status/summary", method = RequestMethod.GET)
	@ResponseBody
	Map<String, Object> getStatusSummary(@PathVariable("snifferId") final long snifferId)
			throws IOException, ResourceNotFoundException {
		final Sniffer sniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(sniffer);
		final List<LogSniffingStatus> logsStatus = new ArrayList<LogSniffingStatus>();
		final Map<Log, IncrementData> logsIncData = snifferPersistence.getIncrementDataByLog(sniffer, source);
		for (final Log log : logsIncData.keySet()) {
			final LogRawAccess<?> logAccess = source.getLogAccess(log);
			final IncrementData incData = logsIncData.get(log);
			LogPointer currentPointer = null;
			long currentOffset = 0;
			if (incData.getNextOffset() != null) {
				currentPointer = logAccess.refresh(logAccess.getFromJSON(incData.getNextOffset().getJson())).get();
			}
			if (currentPointer != null) {
				currentOffset = Math.abs(logAccess.getDifference(null, currentPointer));
			}
			logsStatus.add(new LogSniffingStatus(log, currentPointer, currentOffset, log.getSize()));
		}
		final HashMap<String, Object> summary = new HashMap<>();
		summary.put("scheduleInfo", snifferScheduler.getScheduleInfo(snifferId));
		summary.put("logsStatus", logsStatus);
		return summary;
	}

	private LogSource<?> getSource(final Sniffer sniffer) throws ResourceNotFoundException {
		final LogSource<?> source = sourceProvider.getSourceById(sniffer.getLogSourceId());
		if (source == null) {
			throw new ResourceNotFoundException(LogSource.class, sniffer.getLogSourceId());
		}
		return source;
	}

	private Sniffer getSniffer(final long snifferId) throws ResourceNotFoundException {
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId);
		}
		return sniffer;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/status/startFrom", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	void startFrom(@PathVariable("snifferId") final long snifferId, @RequestBody final StartFromBean[] startFromList)
			throws ResourceNotFoundException, IOException, SchedulerException {
		log.info("Starting sniffer {} from: {}", snifferId, startFromList);
		final Sniffer activeSniffer = getSniffer(snifferId);
		final LogSource<?> source = getSource(activeSniffer);
		for (final Log logg : source.getLogs()) {
			StartFromBean startFrom = null;
			for (final StartFromBean s : startFromList) {
				if (logg.getPath().equals(s.logPath)) {
					startFrom = s;
					break;
				}
			}
			if (startFrom != null) {
				final IncrementData incData = snifferPersistence.getIncrementData(activeSniffer, source, logg);
				if (startFrom.startFromHead) {
					incData.setNextOffset(null);
					log.debug("Setup sniffing {} from head", log);
				} else if (startFrom.startFromTail) {
					final LogRawAccess<?> logAccess = source.getLogAccess(logg);
					final LogPointer end = logAccess.end();
					incData.setNextOffset(end);
					log.debug("Setup sniffing {} from tail: {}", log, end);
				} else if (startFrom.startFromPointer != null && startFrom.startFromPointer.getJson() != null) {
					final LogRawAccess<?> logAccess = source.getLogAccess(logg);
					final LogPointer refreshedPointer = logAccess.getFromJSON(startFrom.startFromPointer.getJson());
					incData.setNextOffset(refreshedPointer);
					log.debug("Setup sniffing {} from pointer: {}", logg, refreshedPointer);
				} else {
					log.warn("Invalid request to setup sniffing {}: {}", logg, startFrom);
					continue;
				}
				snifferPersistence.storeIncrementalData(activeSniffer, source, logg, incData);
			} else {
				log.debug("No start-from information received for log, sniffing will be started from head: {}", logg);
			}
		}
		snifferScheduler.startSniffing(activeSniffer.getId());
		log.info("Started sniffer: {}", snifferId);
	}

	/**
	 * Bean for deserializing the start from request.
	 * 
	 * @author Tester
	 *
	 */
	public static class StartFromBean {
		private String logPath;
		private boolean startFromHead;
		private boolean startFromTail;
		private JsonLogPointer startFromPointer;

		/**
		 * @return the logPath
		 */
		public String getLogPath() {
			return logPath;
		}

		/**
		 * @param logPath
		 *            the logPath to set
		 */
		public void setLogPath(final String logPath) {
			this.logPath = logPath;
		}

		/**
		 * @return the startFromHead
		 */
		public boolean isStartFromHead() {
			return startFromHead;
		}

		/**
		 * @param startFromHead
		 *            the startFromHead to set
		 */
		public void setStartFromHead(final boolean startFromHead) {
			this.startFromHead = startFromHead;
		}

		/**
		 * @return the startFromTail
		 */
		public boolean isStartFromTail() {
			return startFromTail;
		}

		/**
		 * @param startFromTail
		 *            the startFromTail to set
		 */
		public void setStartFromTail(final boolean startFromTail) {
			this.startFromTail = startFromTail;
		}

		/**
		 * @return the startFromPointer
		 */
		public JsonLogPointer getStartFromPointer() {
			return startFromPointer;
		}

		/**
		 * @param startFromPointer
		 *            the startFromPointer to set
		 */
		public void setStartFromPointer(final JsonLogPointer startFromPointer) {
			this.startFromPointer = startFromPointer;
		}

		@Override
		public String toString() {
			return "StartFromBean [logPath=" + logPath + ", startFromHead=" + startFromHead + ", startFromTail="
					+ startFromTail + ", startFromPointer=" + startFromPointer + "]";
		}

	}
}
