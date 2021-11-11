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
package com.catlogging.web.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Unpacks the WAR to ${catlogging.home}/war
 * 
 * @author Tester
 *
 */
public class WebInfConfigurationHomeUnpacked extends WebInfConfigurationUnpackOverridable {

	@Override
	protected File getExtractedWebAppDir(final WebAppContext context, final String war)
			throws IOException, MalformedURLException {
		return new File(System.getProperty("catlogging.home"), "web");
	}

}
