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
package com.catlogging.event;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.catlogging.event.filter.FilteredScanner;
import com.catlogging.validators.CronExprConstraint;
import com.catlogging.validators.NotDefaultPrimitiveValue;

/**
 * Sniffer for searching for log events.
 * 
 * @author Tester
 * 
 */
public class Sniffer {
	private LogEntryReaderStrategy readerStrategy;
	@NotDefaultPrimitiveValue
	private long logSourceId;

	@Valid
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	private FilteredScanner scanner = new FilteredScanner();

	@Valid
	private List<Publisher> publishers;
	@NotNull
	private String name;
	@NotNull
	@CronExprConstraint
	private String scheduleCronExpression;
	private long id;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * @return the logSourceId
	 */
	public long getLogSourceId() {
		return logSourceId;
	}

	/**
	 * @param logSource
	 *            the logSource to set
	 */
	public void setLogSourceId(final long logSourceId) {
		this.logSourceId = logSourceId;
	}

	/**
	 * @return the scanner
	 */
	public FilteredScanner getScanner() {
		return scanner;
	}

	/**
	 * @param scanner
	 *            the scanner to set
	 */
	public void setScanner(final FilteredScanner scanner) {
		this.scanner = scanner;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the scheduleCronExpression
	 */
	public String getScheduleCronExpression() {
		return scheduleCronExpression;
	}

	/**
	 * @param scheduleCronExpression
	 *            the scheduleCronExpression to set
	 */
	public void setScheduleCronExpression(final String scheduleCronExpression) {
		this.scheduleCronExpression = scheduleCronExpression;
	}

	/**
	 * @return the publishers
	 */
	public List<Publisher> getPublishers() {
		return publishers;
	}

	/**
	 * @param publishers
	 *            the publishers to set
	 */
	public void setPublishers(final List<Publisher> publishers) {
		this.publishers = publishers;
	}

	/**
	 * @return the readerStrategy
	 */
	public LogEntryReaderStrategy getReaderStrategy() {
		return readerStrategy;
	}

	/**
	 * @param readerStrategy
	 *            the readerStrategy to set
	 */
	public void setReaderStrategy(final LogEntryReaderStrategy readerStrategy) {
		this.readerStrategy = readerStrategy;
	}

}
