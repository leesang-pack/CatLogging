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
package com.catlogging.web.controller;

import com.catlogging.util.messages.Message;

/**
 * Message flashed by redirection. Should be replace by {@link Message}.
 * 
 * @author Tester
 * 
 */
@Deprecated
public class FlashMessage {
	/**
	 * Message type.
	 * 
	 * @author Tester
	 * 
	 */
	public enum MessageType {
		SUCCESS, ERROR, WARNING, INFO
	}

	private final MessageType type;
	private final String text;

	public FlashMessage(final MessageType type, final String text) {
		super();
		this.type = type;
		this.text = text;
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

}
