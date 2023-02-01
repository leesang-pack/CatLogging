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
package com.catlogging.web.wizard2.reader;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.reader.grok.GrokTextReader;
import com.catlogging.util.grok.Grok;
import com.catlogging.util.grok.GrokConsumerConstructor;
import com.catlogging.util.grok.GroksRegistry;
import com.catlogging.web.wizard2.ConfigBeanWizard;

/**
 * Wizard for {@link GrokTextReader}.
 * 
 * @author Tester
 * 
 */
@Component
public class GrokTextReaderWizard implements ConfigBeanWizard<GrokTextReader> {

	@Autowired
	private GrokConsumerConstructor grokTextReaderConstructor;

	@Autowired
	private BeanConfigFactoryManager configManager;

	@Autowired
	private GroksRegistry groksRegistry;

	private Map<String, Map<String, Grok>> grokGroups;

	@Override
	public String getWizardView() {
		return "templates/wizards/readers/grok";
	}

	@Override
	public String getNameKey() {
		return "catlogging.wizard.reader.grok";
	}

	@Override
	public Class<GrokTextReader> getBeanType() {
		return GrokTextReader.class;
	}

	@Override
	public GrokTextReader getTemplate() {
		GrokTextReader reader = new GrokTextReader();
//		grokTextReaderConstructor.postConstruct(reader, configManager);
		grokTextReaderConstructor.postConstruct(reader);
		return reader;
	}

	public Map<String, Map<String, Grok>> getGrokGroups() {
		if (grokGroups == null) {
			grokGroups = groksRegistry.getGrokGroups();
		}
		return grokGroups;
	}
}
