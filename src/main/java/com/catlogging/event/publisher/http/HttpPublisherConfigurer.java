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
package com.catlogging.event.publisher.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.event.publisher.VelocityEventRenderer;
import com.catlogging.settings.http.HttpSettings;

@Component
public class HttpPublisherConfigurer implements BeanPostConstructor<HttpPublisher> {
	@Autowired
	private HttpSettings httpSettings;

	@Autowired
	private VelocityEventRenderer velocityRenderer;

//	final BeanConfigFactoryManager configManager)
	@Override
	public void postConstruct(final HttpPublisher bean) throws ConfigException {
		bean.init(velocityRenderer, httpSettings.createHttpClientBuilder().build());
	}

}
