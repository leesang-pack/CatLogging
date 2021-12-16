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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Source for timestamp rolled log files with a live file where the name is
 * static. This source supports exposing of multiple logs, because logs are
 * mainly referenced by the matching live files and combined with the
 * appropriated rolled over files.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class RollingLogsSource extends AbstractTimestampRollingLogsSource {

	@JsonProperty
	private String pastLogsSuffixPattern = ".*";

	/**
	 * @return the pastLogsSuffixPattern
	 */
	public String getPastLogsSuffixPattern() {
		return pastLogsSuffixPattern;
	}

	/**
	 * @param pastLogsSuffixPattern
	 *            the pastLogsSuffixPattern to set
	 */
	public void setPastLogsSuffixPattern(final String pastLogsSuffixPattern) {
		this.pastLogsSuffixPattern = pastLogsSuffixPattern;
	}

	@Override
	public List<Log> getLogs() throws IOException {
		final List<Log> logs = super.getLogs();
		final List<Log> rollingLogs = new ArrayList<Log>(logs.size());
		for (int i = 0; i < logs.size(); i++) {
			final Log liveLog = logs.get(i);
			log.debug("Adapting live log to rolling log: {}", liveLog);
			rollingLogs.add(
					new DailyRollingLog(liveLog.getName(), liveLog.getPath(), liveLog, getPastLogs(liveLog.getPath())));
		}
		return rollingLogs;
	}

	protected Log[] getPastLogs(final String liveLog) throws IOException {
		final File dir = new File(FilenameUtils.getFullPathNoEndSeparator(liveLog));
		final String pastPattern = FilenameUtils.getName(liveLog) + getPastLogsSuffixPattern();
		final FileFilter fileFilter = new WildcardFileFilter(pastPattern);
		final File[] files = dir.listFiles(fileFilter);
		final FileLog[] logs = new FileLog[files.length];
		Arrays.sort(files, getPastLogsType().getPastComparator());
		int i = 0;
		for (final File file : files) {
			// TODO Decouple direct file log association
			logs[i++] = new FileLog(file);
		}
		log.debug("Found {} past logs for {} with pattern {}", logs.length, liveLog, pastPattern);
		return logs;
	}

	@Override
	public Log getLog(final String path) throws IOException {
		final Log liveLog = super.getLog(path);
		if (liveLog != null) {
			log.debug("Adapting live log to rolling log: {}", liveLog);
			return new DailyRollingLog(liveLog.getName(), liveLog.getPath(), liveLog, getPastLogs(liveLog.getPath()));
		} else {
			return null;
		}
	}
}
