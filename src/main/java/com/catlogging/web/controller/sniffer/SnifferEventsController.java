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

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.catlogging.event.Event;
import com.catlogging.event.EventPersistence;
import com.catlogging.web.ViewController;
import com.catlogging.util.excption.ResourceNotFoundException;

/**
 * Events controller for a sniffer.
 * 
 * @author Tester
 * 
 */
@ViewController
public class SnifferEventsController extends SniffersBaseController {

	@Autowired
	private EventPersistence eventPersistence;

	@RequestMapping(value = "/sniffers/{snifferId}/events", method = RequestMethod.GET)
	String listEvents(final Model model,
			@PathVariable("snifferId") final long snifferId)
			throws ResourceNotFoundException, SchedulerException {
		getAndBindActiveSniffer(model, snifferId);
		return "templates/sniffers/events";
	}

	@RequestMapping(value = "/sniffers/{snifferId}/events/{eventId}", method = RequestMethod.GET)
	String showEvent(final Model model,
			@PathVariable("snifferId") final long snifferId,
			@PathVariable("eventId") final String eventId)
			throws ResourceNotFoundException, SchedulerException {
		getAndBindActiveSniffer(model, snifferId);
		Event event = eventPersistence.getEvent(snifferId, eventId);
		if (event == null) {
			throw new ResourceNotFoundException(Event.class, eventId,
					"Event not found for id: " + eventId);
		}
		model.addAttribute("event", event);
		return "sniffers/event";
	}
}
