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
package com.catlogging.aspect.sql;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.catlogging.aspect.AspectHost;
import com.catlogging.aspect.AspectProvider;

/**
 * JDBC selection aspect provider.
 * 
 * @author Tester
 * 
 * @param <T>
 * @param <AspectType>
 */
public interface QueryAdaptor<T extends AspectHost, AspectType> extends
		AspectProvider<T, AspectType> {
	public String getQuery(String innerQuery);

	public List<Object> getQueryArgs(List<Object> innerArgs);

	public RowMapper<T> getRowMapper(RowMapper<? extends T> innerMapper);
}
