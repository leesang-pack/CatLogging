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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.catlogging.web.nav.NavNode;
import com.catlogging.web.nav.support.NgPage;
import com.catlogging.web.nav.support.NgTemplate;

/**
 * Spring app config for navigation.
 * 
 * @author Tester
 * 
 */
@Configuration
public class NavigationAppConfig {
	public static final String NAV_NODE_SYSTEM = "systemNode";

	@Value(value = "${catlogging.enable.auth:false}")
	private boolean catloggingEnableAuth;
	/**
	 * 
	 * @return the settings root node exposed by {@link Qualifier}
	 *         {@value NavigationAppConfig#NAV_NODE_SYSTEM}.
	 */
	@Bean(name = NAV_NODE_SYSTEM)
	public NavNode systemNode() {

		final NavNode systemNode = new NavNode("catlogging.nav.system", "static/templates/system/settings/general/system");

		final NavNode general = new NavNode("catlogging.nav.generalSet", "general");
		systemNode.addSubNode(general);
		general.setPageContext(new NgPage("ng/system/settings/general/app.js",
				"SystemSettingsGeneralModule",
				"SystemSettingsGeneralController",
				"templates/system/settings/general/main"));

		final NavNode elasticsearch = new NavNode("catlogging.nav.elastic", "elastic");
		elasticsearch.setPageContext(new NgPage("ng/system/settings/elastic/app.js",
				"SystemSettingsElasticModule",
				"SystemSettingsElasticController",
				"templates/system/settings/elastic/main"));
		systemNode.addSubNode(elasticsearch);

		final NavNode notifications = new NavNode("catlogging.nav.notice", "notifications");
		notifications.setPageContext(new NgPage("ng/system/notifications/app.js",
				"SystemNotificationsModule",
				"SystemNotificationsController",
				"templates/system/notifications/main"));
		systemNode.addSubNode(notifications);

		final NavNode about = new NavNode("catlogging.nav.about", "about");
		about.setPageContext(new NgTemplate("templates/system/about"));
		systemNode.addSubNode(about);

		if(catloggingEnableAuth) {
			final NavNode auth = new NavNode("catlogging.nav.auth", "auth");
			auth.setPageContext(new NgPage("ng/system/auth/app.js",
					"SystemMemberModule",
					"SystemMemberController",
					"templates/system/auth/main"));
			systemNode.addSubNode(auth);
		}
		return systemNode;
	}
}
