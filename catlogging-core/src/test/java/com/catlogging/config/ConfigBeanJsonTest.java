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
package com.catlogging.config;

import com.catlogging.app.CoreAppConfig;
import com.catlogging.model.LogSource;
import com.catlogging.model.file.WildcardLogsSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Test for serializing / deserializing {@link ConfiguredBean}s.
 * 
 * @author Tester
 * 
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CoreAppConfig.class })
@Configuration
public class ConfigBeanJsonTest {
	@Autowired
	private ObjectMapper mapper;

	@Test
	public void testSerializing() throws IOException {
		WildcardLogsSource source = new WildcardLogsSource();
		source.setName("Test");
		String json = mapper.writeValueAsString(source);
		log.info("Serialized bean: {}", json);

		// Deserialize
		LogSource source2 = mapper.readValue(json, LogSource.class);
		Assert.assertEquals(WildcardLogsSource.class, source2.getClass());
	}
}
