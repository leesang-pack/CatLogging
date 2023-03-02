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

package com.catlogging.h2.dsl;

import com.catlogging.model.system.FlywayInfo;
import com.catlogging.model.system.QFlywayInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.DatePart;
import com.querydsl.sql.SQLExpressions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Slf4j
public class FlywayMigrationRepositorySupport extends QuerydslRepositorySupport{
    @Autowired
    private JPAQueryFactory queryFactory;

    public FlywayMigrationRepositorySupport() {
        super(FlywayInfo.class);
    }

    public Page<FlywayInfo> findAllByInstalledOnAndInstallRank(   Date currentTime,
                                                                  Long minTerm,
                                                                  Long installRank,
                                                                  Pageable pageable) {

        QFlywayInfo qFlywayInfo = QFlywayInfo.flywayInfo;
        JPQLQuery<FlywayInfo> query = queryFactory
            .select(Projections.fields(FlywayInfo.class,
                    qFlywayInfo.installedRank,
                    qFlywayInfo.installedOn
            ))
            .from(qFlywayInfo)
            .where(
                    //lt <
                    //loe <=
                    //gt >
                    //goe >=
                SQLExpressions.datediff(DatePart.minute, currentTime, qFlywayInfo.installedOn).abs().lt(minTerm)
                .and(qFlywayInfo.installedRank.eq(installRank))
            );
        long totalCount = query.fetchCount();
        List<FlywayInfo> results = getQuerydsl()
                                    .applyPagination(pageable, query)
                                    .fetch();
        return new PageImpl<>(results, pageable, totalCount);
    }

}