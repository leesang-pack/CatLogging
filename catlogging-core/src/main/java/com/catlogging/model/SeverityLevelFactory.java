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
package com.catlogging.model;

/**
 * Creates corresponding severity levels identified by
 * {@link SeverityLevel#getOrdinalNumber()}.
 * 
 * @author Tester
 * 
 */
public interface SeverityLevelFactory {
	/**
	 * Resolves for a given ordinal number the corresponding severity level
	 * object. If no severity level is defined for given number, a pseudo
	 * instance is returned with name "UNKNOWN" and empty classification.
	 * 
	 * @param ordinalNumber
	 *            the severity ordinal number
	 * @return corresponding severity level object
	 */
	SeverityLevel resolveFor(int ordinalNumber);
}
