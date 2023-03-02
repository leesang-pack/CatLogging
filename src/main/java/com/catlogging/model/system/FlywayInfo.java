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

package com.catlogging.model.system;

import com.catlogging.model.notification.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
		name="flyway_schema_history",
		indexes = {
				@Index(name = "PRIMARY_KEY_6", columnList = "installed_rank", unique = true)
		}
)
@NoArgsConstructor //java.lang.InstantiationException:  java.lang.NoSuchMethodException <init>()
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
public class FlywayInfo {

	@Id
	@Column(name="installed_rank")
	private Long installedRank;

	@Column(name="version")
	private String  version;

	@Column(name="description")
	private String description;

	@Column(name="type")
	private String type;

	@Column(name="script")
	private String script;

	@Column(name="checksum")
	private Long checksum;

	@Column(name="installed_by")
	private String installedBy;

	@Column(name="installed_on")
	private Date installedOn;

	@Column(name="execution_time")
	private Integer executionTime;

	@Column(name="success")
	private boolean success;

}
