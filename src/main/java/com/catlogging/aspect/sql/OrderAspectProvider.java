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
package com.catlogging.aspect.sql;

import java.util.List;

import com.catlogging.aspect.AspectHost;

/**
 * Order by aspect.
 * 
 * @author Tester
 * 
 * @param <T>
 * @param <AspectType>
 */
public class OrderAspectProvider<T extends AspectHost, AspectType> implements QueryAdaptor<T, AspectType> {


	public OrderAspectProvider() {
		super();
	}

	@Override
	public List<Object> getQueryArgs(final List<Object> innerArgs) {
		return innerArgs;
	}

	@Override
	public AspectType getApsect(final T host) {
		return null;
	}

}
