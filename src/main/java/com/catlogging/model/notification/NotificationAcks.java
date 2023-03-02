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
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;

import javax.persistence.*;

/**
 * Notification model bean.
 * 
 * @author Tester
 *
 */
@Entity
@Table(
		name="system_notifications_acks",
		indexes = {
				@Index(name = "CONSTRAINT_7A", columnList = "system_id", unique = true)
		}
)
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper=false)
public class NotificationAcks extends Notice{

	@Column(name="system_user_name")
	private String systemUserName;

	@ManyToOne(fetch = FetchType.EAGER, optional = false) // inner join 조건
	@JoinColumnsOrFormulas(value = { //SQL의 파생된 값(derived value)을 읽기 전용 상태로 표현
		@JoinColumnOrFormula(column=
			@JoinColumn(
				name = "system_id", //컬럼이름
				// ScheduleInfo	 조회시 영속성 컨텍스는 sniffer 데이터를 Sniffer 객체로 가지고 있었다.
				// 그런데 비즈 처리 중 sniffer가 null이 되었고, 영속성 컨텍스트 입장에서는 sniffer 프로퍼티가 Sniffer 객체에서 null로 변경된 것이다.
				// 그래서 변경점을 반영하려고 하는데, updatable = false 속성으로 인해 변경할 수가 없어서 경고가 발생한 것이다.
				insertable = false, //연관관계만 맺는 역할
				updatable = false,  //연관관계만 맺는 역할
				referencedColumnName = "system_id",
				foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
			)
		)
	})
	private Notification notification;
}
