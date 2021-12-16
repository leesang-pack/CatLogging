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

import com.catlogging.event.EventPersistence;
import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferScheduler;
import com.catlogging.web.controller.exception.ActionViolationException;
import com.catlogging.web.controller.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;

/**
 * REST resource for sniffers.
 * 
 * @author Tester
 * 
 */
@Slf4j
@RestController
public class SniffersResourceController {

	@Autowired
	protected SnifferPersistence snifferPersistence;

	@Autowired
	private EventPersistence eventPersistence;

	@Autowired
	protected SnifferScheduler snifferScheduler;

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	protected Sniffer getSniffer(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException {
		final Sniffer activeSniffer = snifferPersistence.getSniffer(snifferId);
		if (activeSniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId, "Sniffer not found for id: " + snifferId);
		}
		return activeSniffer;
	}

	@RequestMapping(value = "/sniffers", method = RequestMethod.POST)
	@ResponseBody
	long createSniffer(@Valid @RequestBody final Sniffer newSniffer)
			throws ResourceNotFoundException, SchedulerException {
		final long snifferId = snifferPersistence.createSniffer(newSniffer);
		log.info("Created new Sniffer with id: {}", snifferId);
		return snifferId;
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.PUT)
	@Transactional(rollbackFor = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	void updateSniffer(@PathVariable("snifferId") final long snifferId, @Valid @RequestBody final Sniffer sniffer)
			throws ResourceNotFoundException, SchedulerException {
		snifferPersistence.updateSniffer(sniffer);
		log.info("Updated sniffer with id: {}", snifferId);
	}

	@Transactional(rollbackFor = Exception.class)
	@RequestMapping(value = "/sniffers/{snifferId}/start", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void start(@PathVariable("snifferId") final long snifferId)
			throws ResourceNotFoundException, SchedulerException, ParseException {
		log.info("Starting sniffer: {}", snifferId);
		snifferScheduler.startSniffing(snifferId);
		log.info("Started sniffer: {}", snifferId);
	}

	@RequestMapping(value = "/sniffers/{snifferId}/stop", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	void stop(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException, SchedulerException {
		log.info("Stopping sniffer: {}", snifferId);
		snifferScheduler.stopSniffing(snifferId);
		log.info("Stopped sniffer: {}", snifferId);
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@Transactional(rollbackFor = Exception.class)
	void deleteSniffer(@PathVariable("snifferId") final long snifferId)
			throws ResourceNotFoundException, SchedulerException, ActionViolationException {
		log.info("Deleting sniffer: {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new ResourceNotFoundException(Sniffer.class, snifferId, "Sniffer not found for id: " + snifferId);
		} else if (snifferScheduler.isScheduled(snifferId)) {
			throw new ActionViolationException("Can't delete a running sniffer");
		}
		snifferPersistence.deleteSniffer(sniffer);
		log.info("Deleting all sniffer events: {}", snifferId);
		try {
			eventPersistence.deleteAll(snifferId);
		} catch (final Exception e) {
			log.error("Failed to delete events of sniffer: " + snifferId, e);
		}
		log.info("Deleted sniffer: {}", snifferId);

	}
}
