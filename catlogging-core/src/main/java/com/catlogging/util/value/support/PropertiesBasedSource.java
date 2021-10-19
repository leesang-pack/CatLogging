/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 
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
package com.catlogging.util.value.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.app.CatLoggingHome;
import com.catlogging.util.value.ConfigValueStore;

/**
 * Retrieves properties from
 * {@link CoreAppConfig#catloggingProperties(org.springframework.context.ApplicationContext)}
 * . To fulfill the {@link ConfigValueStore} interface the properties are stored
 * to permanently to {@link CoreAppConfig#catlogging_PROPERTIES_FILE} and
 * replace in the bean
 * {@link CoreAppConfig#catloggingProperties(org.springframework.context.ApplicationContext)}
 * during current runtime.
 * 
 * @author Tester
 * 
 */
public class PropertiesBasedSource implements ConfigValueStore {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PropertiesBasedSource.class);
	@Autowired
	private CatLoggingHome homeDir;

	@Autowired
	@Qualifier(CoreAppConfig.BEAN_catlogging_PROPS)
	private Properties catloggingProperties;

	@Override
	public String getValue(final String key) {
		return catloggingProperties.getProperty(key);
	}

	@Override
	public void store(final String key, final String value) throws IOException {
		if (value != null) {
			catloggingProperties.setProperty(key, value);
		} else {
			catloggingProperties.remove(key);
		}
		File file = new File(homeDir.getHomeDir(),
				CoreAppConfig.catlogging_PROPERTIES_FILE);
		LOGGER.info("Saving config value for key '{}' to file: {}", key,
				file.getAbsolutePath());
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			LOGGER.warn(
					"Failed to load current properties from file, continue with empty properties: "
							+ file.getAbsolutePath(), e);
		}
		if (value != null) {
			properties.setProperty(key, value);
		} else {
			properties.remove(key);
		}
		properties.store(new FileOutputStream(file), null);
	}

}
