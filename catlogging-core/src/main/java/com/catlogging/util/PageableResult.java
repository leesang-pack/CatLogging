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
package com.catlogging.util;

import java.util.ArrayList;
import java.util.List;

import com.catlogging.util.messages.Message;

/**
 * Result wrapper for pageable result items.
 * 
 * @author Tester
 * 
 * @param <ItemType>
 *            the result item type
 */
public class PageableResult<ItemType> {
	private long totalCount;
	private List<ItemType> items;
	private final List<Message> messages = new ArrayList<>();

	public PageableResult(final long totalCount, final List<ItemType> items) {
		super();
		this.totalCount = totalCount;
		this.items = items;
	}

	public PageableResult() {
		super();
	}

	/**
	 * @return the totalCount
	 */
	public long getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount
	 *            the totalCount to set
	 */
	public void setTotalCount(final long totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return the items
	 */
	public List<ItemType> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(final List<ItemType> items) {
		this.items = items;
	}

	/**
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return messages;
	}

}
