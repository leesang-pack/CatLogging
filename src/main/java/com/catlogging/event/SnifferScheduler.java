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

import java.util.Date;

import com.catlogging.model.sniffer.ScheduleInfo;
import org.quartz.SchedulerException;

import com.catlogging.aspect.sql.QueryAdaptor;
import com.catlogging.event.SnifferPersistence.AspectSniffer;


/**
 * Scheduler for sniffers.
 * 
 * @author Tester
 * 
 */
public interface SnifferScheduler {
	/**
	 * Schedule info aspect related to a {@link AspectSniffer}.
	 * 
	 * @author Tester
	 * 
	 */

	public void startSniffing(long snifferId) throws SchedulerException;

	public void stopSniffing(long snifferId) throws SchedulerException;

	public boolean isScheduled(long snifferId) throws SchedulerException;

	/**
	 * Returns null-safe schedule info for given sniffer.
	 * 
	 * @param snifferId
	 *            sniffer id
	 * @return null-safe schedule info for given sniffer
	 */
	public ScheduleInfo getScheduleInfo(long snifferId);

	/**
	 * 
	 * @return returns aspect adaptor for accessing schedule info
	 * @deprecated aspects no longer on the road map in that way
	 */
	@Deprecated
	public QueryAdaptor<AspectSniffer, ScheduleInfo> getScheduleInfoAspectAdaptor();
}
