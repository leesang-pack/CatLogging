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

import java.util.Locale;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.catlogging.event.Sniffer;
import com.catlogging.event.SnifferPersistence.AspectSniffer;
import com.catlogging.util.PageableResult;
import com.catlogging.web.ViewController;
import com.catlogging.web.controller.FlashMessage;
import com.catlogging.web.controller.FlashMessage.MessageType;
import com.catlogging.web.controller.exception.ActionViolationException;
import com.catlogging.web.controller.exception.ResourceNotFoundException;

/**
 * Controller to manage sniffers.
 * 
 * @author Tester
 * 
 */
@ViewController
public class SnifferManageController extends SniffersBaseController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private SniffersResourceController sniffersResourceController;

	@RequestMapping(value = "/sniffers", method = RequestMethod.GET)
	ModelAndView listSniffers() {
		final ModelAndView mv = new ModelAndView("sniffers/list");
		final PageableResult<AspectSniffer> result = snifferPersistence.getSnifferListBuilder()
				.withEventsCounter(eventPersistence.getEventsCounter())
				.withScheduleInfo(snifferScheduler.getScheduleInfoAspectAdaptor()).list();
		mv.addObject("result", result);
		mv.addObject("sniffers", result.getItems());
		return mv;
	}

	@RequestMapping(value = "/sniffers/new", method = RequestMethod.GET)
	String newSnifferForm() throws ResourceNotFoundException, SchedulerException {
		return "sniffers/new";
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.GET)
	String editSniffer(@PathVariable("snifferId") final long snifferId, final Model model)
			throws ResourceNotFoundException, SchedulerException {
		getAndBindActiveSniffer(model, snifferId);
		return "sniffers/edit";
	}

	@RequestMapping(value = "/sniffers/{snifferId}", method = RequestMethod.POST)
	String redirectAfterUpdate(@PathVariable("snifferId") final long snifferId,
			final RedirectAttributes redirectAttrs) {
		redirectAttrs.addFlashAttribute("message", "Changes applied successfully!");
		return "redirect:{snifferId}";
	}

	@RequestMapping(value = "/sniffers/{snifferId}/delete", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	String deleteSniffer(@PathVariable("snifferId") final long snifferId, final Locale locale,
			final RedirectAttributes redirectAttrs)
					throws ResourceNotFoundException, SchedulerException, ActionViolationException {
		// TODO Duplicate code
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		sniffersResourceController.deleteSniffer(snifferId);
		redirectAttrs.addFlashAttribute("message", new FlashMessage(MessageType.SUCCESS,
				messageSource.getMessage("catlogging.sniffers.deleted", new String[] { sniffer.getName() }, locale)));
		return "redirect:../../sniffers";
	}
}
