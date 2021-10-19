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
package com.catlogging.model;

import java.io.IOException;

public interface LogRawAccessor<ACCESSTYPE extends LogRawAccess<? extends LogInputStream>, LOGTYPE extends Log> {
	/**
	 * Returns raw read access to the log associated with given path or null if
	 * log not found.
	 * 
	 * @param log
	 *            log path
	 * @return read access to associated log or null if log not found
	 * @throws IOException
	 *             in case of errors
	 */
	public ACCESSTYPE getLogAccess(LOGTYPE log) throws IOException;
}
