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
package com.catlogging.web.controller.source;

import java.util.List;
import java.util.Locale;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.util.ReferenceIntegrityException;
import com.catlogging.web.ViewController;
import com.catlogging.web.controller.FlashMessage;
import com.catlogging.web.controller.FlashMessage.MessageType;
import com.catlogging.web.controller.exception.ResourceNotFoundException;

@ViewController
public class SourcesManageController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private LogSourceProvider logsSourceProvider;

	@ModelAttribute("logSources")
	public List<LogSource<LogRawAccess<? extends LogInputStream>>> getLogSources() {
		return logsSourceProvider.getSources();
	}

	@RequestMapping(value = "/sources/new", method = RequestMethod.GET)
	String newSnifferForm(final Model model) throws ResourceNotFoundException, SchedulerException {
		return "sources/new";
	}

	@RequestMapping(value = "/sources/{logSource}", method = RequestMethod.GET)
	String editSourceMvc(@PathVariable("logSource") final long logSourceId,
			@RequestParam(value = "created", defaultValue = "false") final boolean created, final Model model)
					throws ResourceNotFoundException, SchedulerException {
		model.addAttribute("created", created);
		getAndBindActiveSource(logSourceId, model);
		return "sources/edit";
	}

	@RequestMapping(value = "/sources/{logSource}", method = RequestMethod.POST)
	String redirectAfterUpdate(@PathVariable("logSource") final long logSourceId,
			final RedirectAttributes redirectAttrs) {
		redirectAttrs.addFlashAttribute("message", "Changes applied successfully!");
		return "redirect:{logSource}";
	}

	@RequestMapping(value = "/sources/{logSource}/delete", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	String deleteSource(@PathVariable("logSource") final long logSourceId, final Model model, final Locale locale,
			final RedirectAttributes redirectAttrs) throws ResourceNotFoundException, ReferenceIntegrityException {
		final LogSource<?> source = getAndBindActiveSource(logSourceId, model);
		logsSourceProvider.deleteSource(source);
		redirectAttrs.addFlashAttribute("message", new FlashMessage(MessageType.SUCCESS,
				messageSource.getMessage("catlogging.source.deleted", new String[] { source.getName() }, locale)));
		return "redirect:../../sources";
	}

	protected LogSource<?> getAndBindActiveSource(final long logSourceId, final Model model)
			throws ResourceNotFoundException {
		final LogSource<?> source = logsSourceProvider.getSourceById(logSourceId);
		if (source == null) {
			throw new ResourceNotFoundException(LogSource.class, logSourceId,
					"Log source not found for id: " + logSourceId);
		}
		if (model != null) {
			model.addAttribute("activeSource", source);
		}
		return source;
	}

}
