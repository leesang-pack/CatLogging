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
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(
		name="sniffers_schedule_info",
		indexes = {
				@Index(name = "CONSTRAINT_INDEX_F", columnList = "sniffer, scheduleId", unique = true)
		}

)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@ToString
public class ScheduleInfo {

	@EmbeddedId //합성
	private SnifferScheduleIdKey snifferScheduleIdKey; //inner join 조건 nullable = false //<---|
                                                                                              //|
	//삭제 시 관련 부모및 하위모두 삭제됨.                                                        //|
//	@ManyToOne 의                                                                             //|
//		optional 속성이 true (default)                                                         //|
//		이거나 @JoinColumn 의 nullable 속성이 true(default) 인 경우                              //|
//			Sniffer이 없는 Scheduler도 조회될 것을 보장해야 하기 때문에 Outer Join 을 수행한다.     //|
//	@ManyToOne 의                                                                              //|
//		optional 속성                                                                          //|
//		또는 @JoinColumn 의 nullable 속성이 false 인 경우라면,                                    //|
//			Sniffer이 없는 Schduler는 없다는 제약조건이 생기므로 Inner Join을 수행할 수 있다.        //
	@ManyToOne(fetch = FetchType.EAGER, optional = false) // inner join 조건                    //|
	@JoinColumnsOrFormulas(value = { //SQL의 파생된 값(derived value)을 읽기 전용 상태로 표현      //|
		@JoinColumnOrFormula(column=                                                           //|
			@JoinColumn(                                                                       //|
				name = "sniffer", //컬럼이름 <--------------------------------------------------//|
				table = "sniffers_schedule_info",
				// ScheduleInfo	 조회시 영속성 컨텍스는 sniffer 데이터를 Sniffer 객체로 가지고 있었다.
				// 그런데 비즈 처리 중 sniffer가 null이 되었고, 영속성 컨텍스트 입장에서는 sniffer 프로퍼티가 Sniffer 객체에서 null로 변경된 것이다.
				// 그래서 변경점을 반영하려고 하는데, updatable = false 속성으로 인해 변경할 수가 없어서 경고가 발생한 것이다.
				insertable = false, //연관관계만 맺는 역할
				updatable = false,  //연관관계만 맺는 역할
				referencedColumnName = "id",
				foreignKey =@ForeignKey(value = ConstraintMode.NO_CONSTRAINT) //formulas에선 name이 안바뀜.
			)),
//		@JoinColumnOrFormula(formula = //정규식 또는 custom이므로 left outer join 기본
//			@JoinFormula(
//				value = "sniffer", //컬럼이름.
//				referencedColumnName = "id") //지정안할 시 자동으로 PK를 참조
//		)
	})
//	@org.hibernate.annotations.ForeignKey(name = "none") //deprecate 되었지만 fk 이름이 변경할 수 있음.
//	@NotFound(action= NotFoundAction.IGNORE) // foreignKey 설정을 하지 않는다. 완전 무시 하지만, inner join은 함.
	private Sniffer sniffer;

	private boolean scheduled;

	@Column(name = "LAST_FIRE")
	private Date lastFireTime;

	public ScheduleInfo(boolean _scheduled, Date _lastFireTime){
		this.scheduled = _scheduled;
		this.lastFireTime = _lastFireTime;
	}
}
