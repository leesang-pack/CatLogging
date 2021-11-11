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
package com.catlogging.web.ide;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebInfConfiguration;

import com.catlogging.web.util.JettyLauncher;
import com.catlogging.web.util.WebContextWithExtraConfigurations;

/**
 * Launcher for IDE with exploded sources.
 * 
 * @author Tester
 * 
 */
public class JettyIDELauncher extends JettyLauncher {

	@Override
	protected void configureWebAppContext(final WebContextWithExtraConfigurations context) throws Exception {
		super.configureWebAppContext(context);
		WebContextWithExtraConfigurations webAppContext = context;
		webAppContext.replaceConfiguration(MetaInfConfiguration.class, MetaInfFolderConfiguration.class);
		// webAppContext.replaceConfiguration(FragmentConfiguration.class,
		// FragmentFolderConfiguration.class);
		webAppContext.replaceConfiguration(WebInfConfiguration.class, WebInfFolderExtendedConfiguration.class);

		// TODO Review - this will make EVERYTHING on the classpath be
		// scanned for META-INF/resources and web-fragment.xml - great for dev!
		// NOTE: Several patterns can be listed, separate by comma
		webAppContext.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*\\.jar");
		webAppContext.setAttribute(WebInfConfiguration.WEBINF_JAR_PATTERN, ".*\\.jar");
	}

	/**
	 * Starts Jetty from IDE in expanded source code mode.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		JettyIDELauncher launcher = new JettyIDELauncher();
		URL location = new File("src/main/webapp").toURI().toURL();
		launcher.start(args, location);
	}

}
