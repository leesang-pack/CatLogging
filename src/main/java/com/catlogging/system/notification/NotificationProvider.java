/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
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

package com.catlogging.system.notification;

import com.catlogging.model.notification.Notification;
import com.catlogging.model.notification.Notification.Level;
import com.catlogging.util.PageableResult;

/**
 * Manages the lifecycle of notifications.
 * 
 * @author Tester
 *
 */
public interface NotificationProvider {
	/**
	 * Notification summary.
	 * 
	 * @author Tester
	 *
	 */
	public static final class NotificationSummary {
		private final int count;
		private final Level worstLevel;

		public NotificationSummary(final int count, final Level worstLevel) {
			this.count = count;
			this.worstLevel = worstLevel;
		}

		/**
		 * @return count of not acknowledged notifications addressed to a user
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return the worst level of not acknowledged notifications addressed
		 *         to a user
		 */
		public Level getWorstLevel() {
			return worstLevel;
		}
	}

	/**
	 * Stores given notification in the case the id is unknown. If the
	 * notification exists and the override flag is true the message will be
	 * overridden. In the case the message exists and the expiration date is set
	 * this will be updated in any case.
	 * 
	 * @param n
	 *            notification
	 * @param override
	 *            if true and the notification exists already it will be
	 *            overriden
	 * @return true if notification was stored or overridden, false if
	 *         notification is already stored
	 */
	boolean store(Notification n, boolean override);

	/**
	 * Returns not acknowledged notifications addressed to the given user.
	 * 
	 * @param userToken
	 *            the user token
	 * @param from
	 *            offset
	 * @param limit
	 *            limit
	 * @return pageable notification result
	 */
	PageableResult<Notification> getNotifications(String userToken, int from, int limit);

	/**
	 * Returns a summary for not acknowledged notifications addressed to the
	 * given user.
	 * 
	 * @param userToken
	 *            the user token
	 * @return summary
	 */
	NotificationSummary getSummary(String userToken);

	/**
	 * Acknowledges a notification for given user.
	 * 
	 * @param notificationId
	 *            the notification id
	 * @param userToken
	 *            user token
	 */
	void acknowledge(String notificationId, String userToken);

	/**
	 * Deletes a notification physically, thus it won't be available for any
	 * user anymore.
	 * 
	 * @param notificationId
	 *            the id
	 */
	void delete(String notificationId);
}
