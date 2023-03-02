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

package com.catlogging.model.notification;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Notification model bean.
 * 
 * @author Tester
 *
 */
@Entity
@Table(
		name="system_notifications",
		indexes = {
				@Index(name = "PRIMARY_KEY_7B", columnList = "system_id", unique = true)
		}
)
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
public class Notification extends Notice{
	/**
	 * Level for a notification.
	 * 
	 * @author Tester
	 *
	 */
	public static enum Level {
		INFO, WARN, ERROR
	}

	/**
	 * Notification type.
	 * 
	 * @author Tester
	 *
	 */
	public static enum Type {
		/**
		 * Means the notification will be addressed to all users until it isn't
		 * expired. Acknowledging a notification per user is possible only for
		 * this type.
		 */
		TOPIC,
		/**
		 * Means the notification is globally addressed to all users.
		 * Acknowledging isn't supported for this type, only deletion.
		 */
		MESSAGE
	}

	@Column(name="title")
	private String title;

	@Column(name="message")
	@Lob
	private String message;

	@Column(name="expiration")
	private Date expirationDate;

	@Column(name="creation")
	private Date creationDate;

	@Column(name="level")
	// EnumType.STRING 문자열 자체가 DB 에 저장
	// EnumType.ORDINAL는 숫자 저장
	@Enumerated(EnumType.ORDINAL)
	private Level level = Level.INFO;

	@Column(name="ntype")
	@Enumerated(EnumType.ORDINAL)
	private Type type = Type.MESSAGE;

}
