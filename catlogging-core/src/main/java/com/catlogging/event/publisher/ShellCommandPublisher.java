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
package com.catlogging.event.publisher;

import com.catlogging.config.BeanConfigFactoryManager;
import com.catlogging.config.BeanPostConstructor;
import com.catlogging.config.ConfigException;
import com.catlogging.config.PostConstructed;
import com.catlogging.event.Event;
import com.catlogging.event.Publisher;
import com.catlogging.event.publisher.ShellCommandPublisher.ShellPublisherConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@PostConstructed(constructor = ShellPublisherConstructor.class)
public class ShellCommandPublisher implements Publisher {
	@JsonIgnore
	private VelocityEventRenderer velocityRenderer;

	@NotNull
	@JsonProperty
	private String shellScript;

	@JsonProperty
	private String workingDir;

	@Component
	public static class ShellPublisherConstructor implements
			BeanPostConstructor<ShellCommandPublisher> {
		@Autowired
		private VelocityEventRenderer velocityRenderer;

		@Override
		public void postConstruct(final ShellCommandPublisher bean,
				final BeanConfigFactoryManager configManager)
				throws ConfigException {
			bean.velocityRenderer = velocityRenderer;
		}

	}

	@Override
	public void publish(final Event event) throws PublishException {
		VelocityContext velocityContext = velocityRenderer.getContext(event);

	}

	/**
	 * @return the shellScript
	 */
	public String getShellScript() {
		return shellScript;
	}

	/**
	 * @param shellScript
	 *            the shellScript to set
	 */
	public void setShellScript(final String shellScript) {
		this.shellScript = shellScript;
	}

	/**
	 * @return the workingDir
	 */
	public String getWorkingDir() {
		return workingDir;
	}

	/**
	 * @param workingDir
	 *            the workingDir to set
	 */
	public void setWorkingDir(final String workingDir) {
		this.workingDir = workingDir;
	}

}
