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
//	상속관계 매핑, 엔티티가 아님.
//	테이블과 매핑되지 않음
//	부모 클래스는 상속받는 자식 클래스에게 매핑 정보만 제공합니다.
//	조회, 검색이 불가능합니다.
//	직접 생성해서 사용할 일이 없으므로 추상 클래스로 사용하는 것을 권장합니다
//	테이블과 관계 없이, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할입니다.
//	주로 등록일, 수정자, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용합니다.
//  서브 @Entity 클래스는 메인 엔티티나 @MappedSuperclass로 지정한 클래스만 상속이 가능합니다.
@MappedSuperclass //코드 재사용
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class Notice {

	@Id
	@Column(name="system_id")
	private String id;
}
