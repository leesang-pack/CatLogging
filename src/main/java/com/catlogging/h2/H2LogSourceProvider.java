/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
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
package com.catlogging.h2;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.ConfigException;
import com.catlogging.h2.jpa.LogRepository;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSource.LogSourceWrapper;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.model.support.BaseLogsSource;
import com.catlogging.model.support.LogsSourceEntity;
import com.catlogging.util.excption.ReferenceIntegrityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * H2 based source provider.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class H2LogSourceProvider implements LogSourceProvider {
	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private LogRepository logRepository;

	public LogSourceWrapper convertLogSource(final LogsSourceEntity logsSourceEntity){
		if (logsSourceEntity == null){
			return null;
		}
		return new LogSourceWrapper() {
			@SuppressWarnings("unchecked")
			@Override
			public LogSource<LogRawAccess<? extends LogInputStream>> getWrapped() {
				try {
					final BaseLogsSource<LogRawAccess<? extends LogInputStream>> wrapped = (BaseLogsSource<LogRawAccess<? extends LogInputStream>>) configManager.createBeanFromJSON(LogSource.class, logsSourceEntity.getConfig());
					wrapped.setId(logsSourceEntity.getId());
					return wrapped;
				} catch (final ConfigException e) {
					log.error("Failed to deserialize log source: " + logsSourceEntity.getId(), e);
					return LogSource.NULL_SOURCE;
				}
			}
			@Override
			public long getId() {
				return logsSourceEntity.getId();
			}

			@Override
			public String getName() {
				return logsSourceEntity.getName();
			}
		};
	}
	@Override
	public long createSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) {
		LogsSourceEntity newLog = logRepository.save(LogsSourceEntity.builder()
				.name(source.getName())
				.config(configManager.saveBeanToJSON(source))
				.build()
		);
		return newLog.getId();
	}

	@Override
	public List<LogSource<LogRawAccess<? extends LogInputStream>>> getSources() {
		return logRepository.findAllByOrderByName().stream()
				.map(this::convertLogSource)
				.collect(Collectors.toList());
	}

	@Override
	public LogSource<LogRawAccess<? extends LogInputStream>> getSourceById(final long id) {
		return convertLogSource(logRepository.findById(id));
	}

	@Override
	public void updateSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) {
		logRepository.save(LogsSourceEntity.builder()
				.name(source.getName())
				.id(source.getId())
				.config(configManager.saveBeanToJSON(source))
				.build()
		);
	}

	@Override
	public void deleteSource(final LogSource<? extends LogRawAccess<? extends LogInputStream>> source) throws ReferenceIntegrityException {
		try {
			logRepository.deleteById(source.getId());
			log.info("Deleted source with id: {}", source.getId());
		} catch (final DataIntegrityViolationException e) {
			log.info("Deleting source with id {} failed due to references", source.getId());
			throw new ReferenceIntegrityException(LogSource.class, e);
		}
	}

}
