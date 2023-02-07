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
package com.catlogging.event.publisher;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catlogging.app.ConfigValueAppConfig;
import com.catlogging.event.Event;
import com.catlogging.event.Publisher;
import com.catlogging.util.value.ConfigValue;
import com.catlogging.util.value.Configured;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Rendering helper for {@link Publisher}s based on texts.
 * 
 * @author Tester
 * 
 */
@Component
public class EventRenderer {

	@Autowired
	private ITemplateEngine templateEngine;

	@Configured(ConfigValueAppConfig.catlogging_BASE_URL)
	private ConfigValue<String> baseUrl;

	/**
	 * Prepares the context for given event.
	 * 
	 * @param event
	 *            the event
	 * @return context
	 */
	public Context getContext(final Event event) {
		final Context ctx = new Context(Locale.getDefault());
		ctx.setVariable("eventLink",
				baseUrl.get()
						+ "/c/sniffers/"
						+ event.getSnifferId()
						+ "/events/#/"
						+ event.getId());
		ctx.setVariable("event", event);
		return ctx;
	}

	/**
	 * Renders the given template based on the event context.
	 * 
	 * @param template
	 *            template
	 * @param context
	 *            context based on an event
	 * @return the rendered text
	 */
	public String render(final String template, final Context ctx) {
		return templateEngine.process(template, ctx);
	}
}
