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
package com.catlogging.model.file;

import com.catlogging.model.Log;
import com.catlogging.model.support.DailyRollingLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source for timestamp rolled log files with a live file where the name is
 * dynamic. This source exposes only a single log, because the
 * {@link #getPattern()} is used to detect the live and the rolled over files.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class RollingLogsSourceDynamicLiveName extends AbstractTimestampRollingLogsSource {

	@Override
	public List<Log> getLogs() throws IOException {
		final List<Log> logs = super.getLogs();
		if (!logs.isEmpty()) {
			Collections.sort(logs, getPastLogsType().getPastComparatorForLogs());
			final Log liveLog = logs.get(0);
			final List<Log> pastLogs = logs.subList(1, logs.size());
			log.debug("Exposing rolling log with dynamic live file {} and rolled over files: {}", liveLog, pastLogs);
			final List<Log> rolledLog = new ArrayList<>();
			rolledLog.add(new DailyRollingLog(liveLog.getName(), getName(), liveLog, pastLogs));
			return rolledLog;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Log getLog(final String path) throws IOException {
		final List<Log> logs = getLogs();
		if (logs.isEmpty()) {
			return null;
		} else {
			return logs.get(0);
		}
	}
}
