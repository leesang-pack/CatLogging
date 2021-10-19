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
package com.catlogging.reader;

import java.io.IOException;

/**
 * Reflects an exception in case of format errors during reading log entries.
 * 
 * @author Tester
 * 
 */
public class FormatException extends IOException {
	private static final long serialVersionUID = -7865748029157326999L;

	public FormatException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public FormatException(final String arg0) {
		super(arg0);
	}

}
