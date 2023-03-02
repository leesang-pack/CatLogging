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
package com.catlogging.web.app;

import com.catlogging.app.CatLoggingHome;
import com.catlogging.app.ContextProvider;
//import com.catlogging.app.DataSourceAppConfig.DBInitIndicator;
import com.catlogging.h2.dsl.FlywayMigrationRepositorySupport;
import com.catlogging.model.LogInputStream;
import com.catlogging.model.LogRawAccess;
import com.catlogging.model.LogSource;
import com.catlogging.model.LogSourceProvider;
import com.catlogging.model.file.RollingLogsSource;
import com.catlogging.model.system.FlywayInfo;
import com.catlogging.reader.filter.FilteredLogEntryReader;
import com.catlogging.reader.log4j.Log4jTextReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers catloggings own logs as source.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class SniffMePopulator {

	@Autowired
	private CatLoggingHome home;

	@Autowired
	private LogSourceProvider sourceProvider;

	@Autowired
	private FlywayMigrationRepositorySupport flywayMigrationRepositorySupport;

	@Async("SyncTask")
	public void populate() {

		int maxtryCnt = 100;
		int retryCnt = 1;
		while (true){
			try {
				log.debug("[CHECK] Wait....... application Context Loading.. maxTryCnt:[" +maxtryCnt+"] retryCnt :["+ retryCnt +"]");
				if(ContextProvider.getContext() != null) {
					log.debug("[CHECK] Loaded application Context Done.");
					break;
				}

				if(maxtryCnt <= retryCnt ) {
					throw new Exception("[CHECK] Loaded application Context Check Over MaxRetryCnt: " + maxtryCnt);
				}

				Thread.sleep(1000);
				retryCnt += 1;
			} catch (Exception e) {
				break;
			}
		}

		String demoLogName="catlogging's server log";
		List<LogSource<LogRawAccess<? extends LogInputStream>>> logSourceList = sourceProvider.getSources();

		Page<FlywayInfo> flywayInfos = flywayMigrationRepositorySupport.findAllByInstalledOnAndInstallRank(
				new Date(),
				5L,
				-1L,
				PageRequest.of(0, Integer.MAX_VALUE));

		// 5분 이후에는 로그가 있던없던 데모 넣는부분 스킵.
		if(CollectionUtils.isEmpty(flywayInfos.getContent())
		// 5분 동안 로그 내용이 하나이상 존재하면 데모 넣는부분 스킵.
		|| (!CollectionUtils.isEmpty(logSourceList) && !CollectionUtils.isEmpty(flywayInfos.getContent()))) {
			log.info("Welcome for catlogging's server Start");
			return;
		}

		final RollingLogsSource myLogSource = new RollingLogsSource();
		myLogSource.setPattern(new File(home.getHomeDir(), "logs/catlogging.log").getPath());
		myLogSource.setName(demoLogName);
		final Log4jTextReader reader = new Log4jTextReader();
		reader.setFormatPattern("%d %-5p [%c] %m%n");
		final Map<String, String> specifiersFieldMapping = new HashMap<>();
		specifiersFieldMapping.put("d", "date");
		specifiersFieldMapping.put("p", "priority");
		specifiersFieldMapping.put("c", "category");
		specifiersFieldMapping.put("m", "message");
		reader.setSpecifiersFieldMapping(specifiersFieldMapping);
		myLogSource.setReader(new FilteredLogEntryReader(reader, null));
		sourceProvider.createSource(myLogSource);
		log.info("Created source for catlogging's server log: {}", myLogSource);
	}

}
