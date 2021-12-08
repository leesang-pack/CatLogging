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

import com.catlogging.fields.FieldBaseTypes;
import com.catlogging.model.*;
import com.catlogging.model.support.BaseLogsSource;
import com.catlogging.util.json.Views;
import com.catlogging.web.controller.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * REST resource for sources.
 * 
 * @author Tester
 * 
 */
@Slf4j
@RestController
public class SourcesResourceController {

	@Autowired
	private LogSourceProvider logsSourceProvider;

	@RequestMapping(value = "/sources/{logSource}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	LogSource<?> getSource(@PathVariable("logSource") final long logSourceId) throws ResourceNotFoundException {
		return getActiveSource(logSourceId);
	}

	@RequestMapping(value = "/sources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@JsonView(Views.Info.class)
	List<LogSource<LogRawAccess<? extends LogInputStream>>> getAllSources() throws ResourceNotFoundException {
		return logsSourceProvider.getSources();
	}

	@RequestMapping(value = "/sources/{logSource}/reader/supportedSeverities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	List<SeverityLevel> getReaderSupportedSeverities(@PathVariable("logSource") final long logSourceId)
			throws ResourceNotFoundException {
		return getActiveSource(logSourceId).getReader().getSupportedSeverities();
	}

	@RequestMapping(value = "/sources", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	@ResponseBody
	long createSource(@Valid @RequestBody final LogSource<?> newSource, final RedirectAttributes redirectAttrs)
			throws ResourceNotFoundException, SchedulerException, ParseException {
		final long id = logsSourceProvider.createSource(newSource);
		log.info("Created new log source: {}", newSource);
		return id;
	}

	@RequestMapping(value = "/sources/{logSource}", method = RequestMethod.PUT)
	@Transactional(rollbackFor = Exception.class)
	@ResponseStatus(HttpStatus.OK)
	void updateSource(@PathVariable("logSource") final long logSourceId,
			@Valid @RequestBody final LogSource<?> newSource)
					throws ResourceNotFoundException, SchedulerException, ParseException {
		((BaseLogsSource<?>) newSource).setId(logSourceId);
		logsSourceProvider.updateSource(newSource);
	}

	protected LogSource<?> getActiveSource(final long logSourceId) throws ResourceNotFoundException {
		final LogSource<?> source = logsSourceProvider.getSourceById(logSourceId);
		if (source == null) {
			throw new ResourceNotFoundException(LogSource.class, logSourceId,
					"Log source not found for id: " + logSourceId);
		}
		return source;
	}

	@RequestMapping(value = "/sources/{logSource}/logs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	Log[] getSourceLogs(@PathVariable("logSource") final long logSourceId)
			throws ResourceNotFoundException, IOException {
		final LogSource<?> source = getActiveSource(logSourceId);
		return source.getLogs().toArray(new Log[0]);
	}

	@RequestMapping(value = "/sources/logs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	Log[] getSourceLogs(@Valid @RequestBody final LogSource<?> source) throws IOException {
		return source.getLogs().toArray(new Log[0]);
	}

	@RequestMapping(value = "/sources/potentialFields", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	LinkedHashMap<String, FieldBaseTypes> getPotentialFields(@Valid @RequestBody final LogSource<?> source)
			throws IOException {
		return source.getReader().getFieldTypes();
	}
}
