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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.catlogging.event.Scanner;
import com.catlogging.model.Log;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.user.UserTokenProvider;
import com.catlogging.user.profile.ProfileSettingsStorage;
import com.catlogging.web.ViewController;
import com.catlogging.util.excption.ResourceNotFoundException;
import com.catlogging.web.wizard.WizardInfo;
import com.catlogging.web.controller.wizard.WizardInfoController;

@ViewController
public class SourcesController {

	public static final int DEFAULT_ENTRIES_COUNT = 100;

	public static final String PROFILE_SETTINGS_VIEWER_FIELDS = "/logSource/{0}/viewerFields";

	@Autowired
	private LogSourceProvider logsSourceProvider;

	@Autowired
	private WizardInfoController wizardController;

	@Autowired
	private UserTokenProvider tokenProvider;

	@Autowired
	private ProfileSettingsStorage profileSettingsStorage;

	@ModelAttribute("filterScannerWizards")
	public List<WizardInfo> filterScannerWizards(final Locale locale) {
		return wizardController.getWizardsInfo(Scanner.class, locale);
	}

	private LogSource<?> getAndFillActiveLogSource(final ModelAndView mv, final long logSourceId) throws ResourceNotFoundException {
		final LogSource<?> activeLogSource = logsSourceProvider.getSourceById(logSourceId);
		if (activeLogSource != null) {
			mv.addObject("activeSource", activeLogSource);
			return activeLogSource;
		} else {
			throw new ResourceNotFoundException(LogSource.class, logSourceId, "Log source not found for id: " + logSourceId);
		}
	}

	private Log getAndFillLog(final ModelAndView mv, final LogSource<?> activeLogSource, final String logPath) throws IOException, ResourceNotFoundException {
		if (logPath == null) {
			return null;
		}
		final Log log = activeLogSource.getLog(logPath);
		if (log != null) {
			mv.addObject("activeLog", log);
			return log;
		} else {
			throw new ResourceNotFoundException(Log.class, logPath, "Log not found in source " + activeLogSource + ": " + logPath);
		}
	}

	@RequestMapping(value = "/sources", method = RequestMethod.GET)
	ModelAndView listSources() {
		final ModelAndView mv = new ModelAndView("templates/sources/list");
		final List<LogSource<LogRawAccess<? extends LogInputStream>>> logSources = logsSourceProvider.getSources();
		mv.addObject("logSources", logSources);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSourceId}/logs", method = RequestMethod.GET)
	ModelAndView listSourceLogs(@PathVariable("logSourceId") final long logSourceId) throws IOException, ResourceNotFoundException {
		final ModelAndView mv = new ModelAndView("templates/sources/logs");
		final LogSource<?> activeSource = getAndFillActiveLogSource(mv, logSourceId);
		final List<LogSource<LogRawAccess<? extends LogInputStream>>> logSources = logsSourceProvider.getSources();
		mv.addObject("logSources", logSources);
		mv.addObject("logs", activeSource.getLogs());
		mv.addObject("defaultCount", DEFAULT_ENTRIES_COUNT);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSource}/info", method = RequestMethod.GET)
	ModelAndView info(@PathVariable("logSource") final long logSource, @RequestParam("log") final String logPath) throws IOException, ResourceNotFoundException {
		final ModelAndView mv = new ModelAndView("templates/sources/info");
		getAndFillLog(mv, getAndFillActiveLogSource(mv, logSource), logPath);
		return mv;
	}

	@RequestMapping(value = "/sources/{logSource}/show", method = RequestMethod.GET)
	ModelAndView showStart(@PathVariable("logSource") final long logSourceId, @RequestParam("log") final String logPath, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ResourceNotFoundException {
		final ModelAndView mv = new ModelAndView("templates/sources/show");
		final LogSource<?> logSource = getAndFillActiveLogSource(mv, logSourceId);
		final Log log = getAndFillLog(mv, logSource, logPath);
		mv.addObject("defaultCount", DEFAULT_ENTRIES_COUNT);
		mv.addObject("pointerTpl", logSource.getLogAccess(log).start());
		mv.addObject("userProfileViewerFields", profileSettingsStorage.getSettings(
				tokenProvider.getToken(request, response),
				MessageFormat.format(PROFILE_SETTINGS_VIEWER_FIELDS, logSourceId),
				false));
		return mv;
	}

}