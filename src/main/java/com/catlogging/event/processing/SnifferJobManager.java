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
package com.catlogging.event.processing;

import com.catlogging.aspect.sql.QueryAdaptor;
import com.catlogging.model.sniffer.Sniffer;
import com.catlogging.event.SnifferPersistence;
import com.catlogging.event.SnifferPersistence.AspectSniffer;
import com.catlogging.event.SnifferScheduler;
import com.catlogging.h2.ScheduleInfoAccess;
import com.catlogging.model.sniffer.ScheduleInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.MutableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;

/**
 * Manages sniffer jobs.
 * 
 * @author Tester
 * 
 */
@Component
@Slf4j
public class SnifferJobManager implements SnifferScheduler {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private SnifferPersistence snifferPersistence;

	@Autowired
	private ScheduleInfoAccess scheduleInfoAccess;

	@Override
	@Transactional(rollbackFor = { SchedulerException.class, ParseException.class })
	public void startSniffing(final long snifferId) throws SchedulerException {
		log.debug("Starting cron job for sniffer: {}", snifferId);
		final Sniffer sniffer = snifferPersistence.getSniffer(snifferId);
		if (sniffer == null) {
			throw new SchedulerException("Sniffer not found: " + snifferId);
		}
		stopAndDeleteAllSnifferJobs(sniffer.getId());
		MutableTrigger trigger;
		try {
			trigger = CronScheduleBuilder.cronScheduleNonvalidatedExpression(sniffer.getScheduleCronExpression()).withMisfireHandlingInstructionDoNothing().build();
		} catch (final ParseException e) {
			throw new SchedulerException("Failed to parse cron expression", e);
		}
		trigger.setKey(getKey(sniffer, sniffer.getLogSourceId()));
		final JobDetail jobDetail = JobBuilder
				.newJob(SnifferJob.class)
				.requestRecovery()
				.withIdentity(getJobKey(sniffer, sniffer.getLogSourceId()))
				.build();
		scheduler.scheduleJob(jobDetail, trigger);
		final ScheduleInfo scheduleInfo = scheduleInfoAccess.getScheduleInfo(snifferId);
		scheduleInfo.setScheduled(true);
		scheduleInfoAccess.updateScheduleInfo(snifferId, scheduleInfo);
		log.info("Scheduled cron job for sniffer {} and log source {} with trigger {}", sniffer, sniffer.getLogSourceId(), trigger);
	}

	protected TriggerKey getKey(final Sniffer sniffer, final long logSourceId) {
		return TriggerKey.triggerKey(sniffer.getId() + ":" + logSourceId, "SNIFFER:" + sniffer.getId());
	}

	protected JobKey getJobKey(final Sniffer sniffer, final long logSourceId) {
		return JobKey.jobKey(sniffer.getId() + ":" + logSourceId, "SNIFFER:" + sniffer.getId());
	}

	protected static JobKey getJobKey(final long snifferId, final long logSourceId) {
		return JobKey.jobKey(snifferId + ":" + logSourceId, "SNIFFER:" + snifferId);
	}

	protected static long getSnifferId(final JobKey key) {
		return Long.parseLong(key.getName().split(":")[0]);
	}

	protected static long getLogSourceId(final JobKey key) {
		return Long.parseLong(key.getName().split(":")[1]);
	}

	protected void stopAndDeleteAllSnifferJobs(final long snifferId) throws SchedulerException {
		for (final JobKey job : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("SNIFFER:" + snifferId))) {
			log.info("Deleting scheduled job for sniffer={} and log source={}", snifferId, getLogSourceId(job));
			scheduler.deleteJob(job);
			log.info("Interrupting job for sniffer={} and log source={}", snifferId, getLogSourceId(job));
			scheduler.interrupt(job);
		}
	}

	@Transactional(rollbackFor = { SchedulerException.class })
	@Override
	public void stopSniffing(final long snifferId) throws SchedulerException {
		log.debug("Stopping scheduled cron jobs for sniffer: {}", snifferId);
		stopAndDeleteAllSnifferJobs(snifferId);
		final ScheduleInfo scheduleInfo = scheduleInfoAccess.getScheduleInfo(snifferId);
		scheduleInfo.setScheduled(false);
		scheduleInfoAccess.updateScheduleInfo(snifferId, scheduleInfo);
	}

	@Override
	public boolean isScheduled(final long snifferId) throws SchedulerException {
		return scheduleInfoAccess.getScheduleInfo(snifferId).isScheduled();
	}

	@Override
	public QueryAdaptor<AspectSniffer, ScheduleInfo> getScheduleInfoAspectAdaptor() {
		return SCHEDULE_INFO_ADAPTOR;
	}

	private static final QueryAdaptor<SnifferPersistence.AspectSniffer, ScheduleInfo> SCHEDULE_INFO_ADAPTOR = new QueryAdaptor<SnifferPersistence.AspectSniffer, ScheduleInfo>() {

		@Override
		public ScheduleInfo getApsect(final AspectSniffer host) {
			return host.getAspect("scheduleInfo", ScheduleInfo.class);
		}

		@Override
		public List<Object> getQueryArgs(final List<Object> innerArgs) {
			return innerArgs;
		}

	};

	@Override
	public ScheduleInfo getScheduleInfo(final long snifferId) {
		return scheduleInfoAccess.getScheduleInfo(snifferId);
	}
}
