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
package com.catlogging.util.grok;

/**
 * Predicate constraint in addition to basic pattern matching.
 * 
 * @author Tester
 * 
 */
public interface GrokPredicate {
	/**
	 * Checks if the matched group values matches this predicate constraint too.
	 * 
	 * @param groupValue
	 *            the group value matching the basic pattern.
	 * @return true if constraint is valid for the value.
	 */
	public boolean matches(String groupValue);
}
