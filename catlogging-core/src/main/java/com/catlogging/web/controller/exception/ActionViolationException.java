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
package com.catlogging.web.controller.exception;

/**
 * Thrown in case of action violations.
 * 
 * @author Tester
 * 
 */
public class ActionViolationException extends Exception {

	private static final long serialVersionUID = -7078342804206150864L;

	public ActionViolationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionViolationException(final String message) {
		super(message);
	}

}
