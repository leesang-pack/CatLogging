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

import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.catlogging.event.LogEntryReaderStrategy;
import com.catlogging.event.Publisher;
import com.catlogging.h2.jpa.converter.SnifferPublisherConverter;
import com.catlogging.h2.jpa.converter.SnifferReaderConverter;
import com.catlogging.h2.jpa.converter.SnifferScannerConverter;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.catlogging.event.filter.FilteredScanner;
import com.catlogging.validators.CronExprConstraint;
import com.catlogging.validators.NotDefaultPrimitiveValue;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;

/**
 * Sniffer for searching for log events.
 * 
 * @author Tester
 * 
 */
@Entity
@Table(
		name="sniffers"
)
// sniffer table 하나의 테이블에 다양한 VO 수용. 다형성
// AspectSniffer extends Sniffer가 들어갈 수 있고, dtype="AspectSniffer"
// ... extends Sniffer가 들어갈 수도 있다.
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) //누군가에 상속받아서 사용할때 단점: dtype 컬럼필요.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@SuperBuilder
@ToString
public class Sniffer {

	//	IDENTITY : 기본키를 null로 넣으면, DB가 알아서 생성해서 넣어준다. (ex: Mysql, PostgreSQL,,)
	//	SEQUENCE : 데이터베이스 시퀀스는 유일한 값을 순서대로 생성한다. (ex. Oracle, PostgreSQL, H2..)
	//	TABLE : 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
	//	AUTO : 데이터베이스 방언(dialect)마다 위 세가지 전략을 자동으로 지정한다.
	@Id
	@GeneratedValue
	@Column(name="ID")
	private Long id;

//	int id; // not null로 생성됨. primitive에는 null이 들어갈 수 없기 때문.
//	Integer id; // nullable true로 생성됨
//	@Column
//	int id; // @Column의 기본값인 nullable=true가 적용되서 nullable=true로 생성됨. 주의해야함

	@NotNull
	@Column(name="NAME", nullable = false)
	private String name;

	@NotNull
	@CronExprConstraint
	@Column(name="CRON_EXPR")
	private String scheduleCronExpression;

	@NotDefaultPrimitiveValue
	@Column(name="SOURCE")
	private Long logSourceId;

	//	@OneToOne(mappedBy = "sniffers_scanner_idata")
	// sniffer--- scanner
	@Valid
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	@Builder.Default							//vo에서 new로 만들었기 때문에
	@Column(name="SCANNER_CONFIG")
	@Convert(converter = SnifferScannerConverter.class)
	private FilteredScanner scanner = new FilteredScanner();

	//	@OneToOne(mappedBy = "") //따로 테이블 없음.
	// sniffer--- reader
	@Column(name="READER_STRATEGY_CONFIG")
	@Convert(converter = SnifferReaderConverter.class)
	private LogEntryReaderStrategy readerStrategy;

	//	@OneToMany(mappedBy = "") //따로 테이블은 없음.
	// sniffer--- publisher1
	//         |- publisher2
	@Valid
	@Column(name="PUBLISHERS_CONFIG")
	@Convert(converter = SnifferPublisherConverter.class)
	private List<Publisher> publishers;
}
