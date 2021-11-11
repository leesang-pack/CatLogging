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

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Extension to delegate execution to a context bound job.
 * 
 * @author Tester
 * 
 */
public interface ContextAwareJob extends Job {
	/**
	 * Called in scope of a context bound job.
	 * 
	 * @param jobCtx
	 * @throws JobExecutionException
	 */
	void executeInContext(JobExecutionContext jobCtx,
			InterruptionStatus interruption) throws JobExecutionException;

	/**
	 * Context for interruption status of a job.
	 * 
	 * @author blank08
	 * 
	 */
	public static class InterruptionStatus {
		private boolean interrupted = false;

		public boolean isInterrupted() {
			return interrupted;
		}

		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
		}

	}

}
