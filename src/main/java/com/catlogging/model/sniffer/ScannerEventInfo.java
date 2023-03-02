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

package com.catlogging.model.sniffer;

import lombok.*;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(
		name="sniffers_events"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@ToString
public class ScannerEventInfo {


	@GeneratedValue
	@Id
	@Column(name = "id")
	private Long  id;

	@Column(name = "sniffer")
	private Long  snifferId;

	@Column(name = "source")
	private Long  source;

	@Column(name = "log")
	private String  log;

	@Column(name = "data")
	@Lob
	private String  data;

	@Column(name = "published")
	private Date published;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumnsOrFormulas(value = {
			@JoinColumnOrFormula(column=
			@JoinColumn(
					name = "sniffer",
					table = "sniffers_events",
					insertable = false, //연관관계만 맺는 역할
					updatable = false,  //연관관계만 맺는 역할
					referencedColumnName = "id",
					foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
			))
	})
	private Sniffer sniffer;

	//Todo : source도 foreignKey
}
