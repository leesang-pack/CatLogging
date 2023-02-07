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

import com.catlogging.event.Event;
import com.catlogging.event.EventPersistence;
import com.catlogging.event.EventPersistence.AspectEvent;
import com.catlogging.event.EventPersistence.EventQueryBuilder;
import com.catlogging.event.EventPersistence.NativeQueryBuilder;
import com.catlogging.util.PageableResult;
import com.catlogging.util.excption.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * REST controller for events.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Controller
public class SnifferEventsResourceController {

	@Autowired
	private EventPersistence eventPersistence;

	@RequestMapping(value = "/sniffers/{snifferId}/events", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	PageableResult<AspectEvent> getEvents(final Model model, @PathVariable("snifferId") final long snifferId,
			@RequestParam(value = "_offset", defaultValue = "0", required = false) final long offset,
			@RequestParam(value = "_size", defaultValue = "25", required = false) final int size,
			@RequestParam(value = "_from", defaultValue = "-1", required = false) final long occurrenceFrom,
			@RequestParam(value = "_to", defaultValue = "-1", required = false) final long occurrenceTo,
			@RequestParam(value = "_histogram", defaultValue = "true", required = false) final boolean withHistogram) {
		EventQueryBuilder qb = eventPersistence.getEventsQueryBuilder(snifferId, offset, size);
		if (withHistogram) {
			qb = qb.withEventCountTimeHistogram(60);
		}
		qb = qb.sortByEntryTimestamp(false);
		if (occurrenceFrom >= 0) {
			qb.withOccurrenceFrom(new Date(occurrenceFrom));
		}
		if (occurrenceTo >= 0) {
			qb.withOccurrenceTo(new Date(occurrenceTo));
		}
		final PageableResult<AspectEvent> events = qb.list();
		return events;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/nativeSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	PageableResult<AspectEvent> getEventsByNativeSearch(@PathVariable("snifferId") final long snifferId,
			@RequestParam(value = "_offset", defaultValue = "0", required = false) final long offset,
			@RequestParam(value = "_size", defaultValue = "25", required = false) final int size,
			@RequestParam(value = "_histogram", defaultValue = "true", required = false) final boolean withHistogram,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String jsonRequest = IOUtils.toString(request.getInputStream());
		NativeQueryBuilder qb = eventPersistence.getEventsNativeQueryBuilder(snifferId, offset, size);
		if (withHistogram) {
			qb = qb.withEventCountTimeHistogram(60);
		}
		qb.withNativeQuery(jsonRequest);
		final PageableResult<AspectEvent> events = qb.list();
		return events;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/{eventId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	Event showEvent(@PathVariable("snifferId") final long snifferId, @PathVariable("eventId") final String eventId)
			throws ResourceNotFoundException {
		final Event event = eventPersistence.getEvent(snifferId, eventId);
		if (event == null) {
			throw new ResourceNotFoundException(Event.class, eventId, "Event not found for id: " + eventId);
		}
		return event;
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/{eventId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	void deleteEvent(@PathVariable("snifferId") final long snifferId, @PathVariable("eventId") final String eventId)
			throws ResourceNotFoundException {
		log.debug("Deleting event {} for sniffer {}", eventId, snifferId);
		// Load event first to check existence
		showEvent(snifferId, eventId);
		// Delete now
		eventPersistence.delete(snifferId, new String[] { eventId });
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	void deleteAllEvents(@PathVariable("snifferId") final long snifferId) throws ResourceNotFoundException {
		log.info("Deleting all events of sniffer {}", snifferId);
		eventPersistence.deleteAll(snifferId);
	}
}
