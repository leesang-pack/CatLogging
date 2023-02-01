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
package com.catlogging.web.controller.sniffer;

import com.catlogging.event.IncrementData;
import com.catlogging.event.Sniffer;
import com.catlogging.model.Log;
import com.catlogging.model.LogPointer;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.web.ViewController;
import com.catlogging.web.controller.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Controller for sniffer state and scheduling issues.
 * 
 * @author Tester
 * 
 */
@Slf4j
@ViewController
public class SnifferStatusController extends SniffersBaseController {

	public static class LogSniffingStatus {
		private final long currentOffset;
		private final long logSize;
		private final Log log;
		private final LogPointer currentPointer;

		public LogSniffingStatus(final Log log, final LogPointer currentPointer, final long currentOffset,
				final long logSize) {
			super();
			this.logSize = logSize;
			this.currentPointer = currentPointer;
			this.log = log;
			this.currentOffset = currentOffset;
		}

		/**
		 * @return the logSize
		 */
		public long getLogSize() {
			return logSize;
		}

		/**
		 * @return the log
		 */
		public Log getLog() {
			return log;
		}

		/**
		 * @return the currentOffset
		 */
		public long getCurrentOffset() {
			return currentOffset;
		}

		/**
		 * @return the currentPointer
		 */
		public LogPointer getCurrentPointer() {
			return currentPointer;
		}

	}

	@RequestMapping(value = "/sniffers/{snifferId}/status", method = RequestMethod.GET)
	String showState(@PathVariable("snifferId") final long snifferId, final Model model)
			throws ResourceNotFoundException, SchedulerException, IOException, InterruptedException,
			ExecutionException {
		final Sniffer activeSniffer = getAndBindActiveSniffer(model, snifferId);
		final LogSource<?> logSource = getLogSource(activeSniffer.getLogSourceId());
		final Map<Log, IncrementData> logsIncData = snifferPersistence.getIncrementDataByLog(activeSniffer, logSource);
		final List<LogSniffingStatus> logsStatus = new ArrayList<LogSniffingStatus>();
		for (final Log log : logsIncData.keySet()) {
			final LogRawAccess<?> logAccess = logSource.getLogAccess(log);
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
		model.addAttribute("scheduleInfo", snifferScheduler.getScheduleInfo(snifferId));
		model.addAttribute("logsStatus", logsStatus);
		model.addAttribute("source", logSource);
		return "templates/sniffers/status";
	}

	@RequestMapping(value = "/sniffers/{snifferId}/stopForm", method = RequestMethod.POST)
	String stop(@PathVariable("snifferId") final long snifferId, final Model model,
			final RedirectAttributes redirectAttrs) throws ResourceNotFoundException, SchedulerException {
		log.info("Stopping sniffer: {}", snifferId);
		final Sniffer activeSniffer = getAndBindActiveSniffer(model, snifferId);
		snifferScheduler.stopSniffing(activeSniffer.getId());
		log.info("Stopped sniffer: {}", snifferId);
		redirectAttrs.addFlashAttribute("stopped", true);
		return "redirect:status";
	}
}
