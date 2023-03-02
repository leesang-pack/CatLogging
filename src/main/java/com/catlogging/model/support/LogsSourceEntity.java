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
package com.catlogging.model.support;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

/**
 * Base log source.
 * 
 * @author Tester
 * 
 */
@Entity
@Table(
		name="log_sources",
		indexes = {
				@Index(name = "PRIMARY_KEY_7", columnList = "id", unique = true)
		}
)
@NoArgsConstructor
@Data
@SuperBuilder
public class LogsSourceEntity {

	@Id
	@GeneratedValue
	@Column(name="id")
	private long id;

	@Column(name="name")
	private String name;

	@Column(name="config")
	@Lob
	private String config;

}
