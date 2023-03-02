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
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(
		name="sniffers_scanner_idata",
		indexes = {
				@Index(name = "PRIMARY_KEY_F", columnList = "sniffer, source, log", unique = true)
		}

)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@ToString
public class ScannerIdataInfo {

	@EmbeddedId
	private SnifferIdKey snifferIdKey;

	@Column(name = "log")
	private String  log;

	@Column(name = "data")
	@Lob
	private String  data;

	@Column(name = "next_pointer")
	private String  next_pointer;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumnsOrFormulas(value = {
			@JoinColumnOrFormula(column=
			@JoinColumn(
					name = "sniffer",
					table = "sniffers_scanner_idata",
					insertable = false, //연관관계만 맺는 역할
					updatable = false,  //연관관계만 맺는 역할
					referencedColumnName = "id",
					foreignKey =@ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
			))
	})
	private Sniffer sniffer;

	//Todo : source도 foreignKey
}
