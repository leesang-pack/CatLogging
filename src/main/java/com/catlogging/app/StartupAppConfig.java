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
package com.catlogging.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * App config for startup routines.
 * 
 * @author Tester
 * 
 */
@Configuration
@Slf4j
public class StartupAppConfig {

	@Value(value = "${catlogging.home}")
	private String catloggingHomeDir;

	@Value(value = "${catlogging.version}")
	private String version;

	/**
	 * Checks the home dir under ${catlogging.home} for write access, creates it
	 * if necessary and writes a template config.properties.
	 * 
	 * @return home directory representation
	 * @throws Exception
	 */
	@Bean
	public CatLoggingHome homeDir() throws Exception {
		File catloggingHomeDirFile = new File(catloggingHomeDir);
		log.info("Starting catlogging [{}] with home directory [{}]", version, catloggingHomeDirFile.getPath());
		if (!catloggingHomeDirFile.exists()) {
			log.info("Home directory is't present, going to create it");
			try {
				catloggingHomeDirFile.mkdirs();
			} catch (Exception e) {
				log.error(
						"Failed to create home directory \""
								+ catloggingHomeDirFile.getPath()
								+ "\". catlogging can't operate without a write enabled home directory. Please create the home directory manually and grant the user catlogging is running as the write access.",
						e);
				throw e;
			}
		} else if (!catloggingHomeDirFile.canWrite()) {
			log.error(
					"Configured home directory \"{}\" isn't write enabled. catlogging can't operate without a write enabled home directory. Please grant the user catlogging is running as the write access.",
					catloggingHomeDirFile.getPath());
			throw new SecurityException("Configured home directory \""
					+ catloggingHomeDirFile.getPath()
					+ "\" isn't write enabled.");
		}
		File homeConfigProps = new File(catloggingHomeDirFile,
				"config.properties");
		if (!homeConfigProps.exists()) {
			FileOutputStream fo = new FileOutputStream(homeConfigProps);
			try {
				new Properties().store(fo, "Place here catlogging settings");
			} finally {
				fo.close();
			}
		}
		
		return new CatLoggingHome() {
			@Override
			public File getHomeDir() {
				return new File(catloggingHomeDir);
			}
		};
	}
}
