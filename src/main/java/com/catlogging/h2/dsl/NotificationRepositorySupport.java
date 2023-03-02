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

import com.catlogging.h2.jpa.NotificationRepositoryCustom;
import com.catlogging.model.notification.Notification;
import com.catlogging.model.notification.QNotification;
import com.catlogging.model.notification.QNotificationAcks;
import com.querydsl.core.types.*;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
public class NotificationRepositorySupport extends QuerydslRepositorySupport implements NotificationRepositoryCustom {
    @Autowired
    private JPAQueryFactory queryFactory;

    public NotificationRepositorySupport() {
        super(Notification.class);
    }

    @Override
    public Page<Notification> findAllByTypeAndExpirationDate(
                                              String systemUserName,
                                              int type,
                                              Date expirationDate,
                                              Pageable pageable) {

        QNotification qNotificationEntity = QNotification.notification;
        QNotificationAcks qNotificationAcks = QNotificationAcks.notificationAcks;
        JPQLQuery<Notification> query = queryFactory
            .select(Projections.fields(Notification.class,
                    qNotificationEntity.id,
                    qNotificationEntity.creationDate,
                    qNotificationEntity.expirationDate,
                    qNotificationEntity.level,
                    qNotificationEntity.message,
                    qNotificationEntity.title,
                    qNotificationEntity.type
            ))
            .from(qNotificationEntity)
            .leftJoin(qNotificationAcks).on(
                qNotificationAcks.id.eq(qNotificationEntity.id)
                .and(
                    qNotificationAcks.systemUserName.eq(systemUserName)
                )
            ).fetchJoin()
            .where(
                    //Enum -> String, target int -> String
//                qNotificationEntity.type.stringValue().eq(String.valueOf(type))
                    //Enum -> int, target int
                    qNotificationEntity.type.castToNum(Integer.class).eq(type)
                .or(
                    qNotificationAcks.id.isNull()
                    .and(
                        qNotificationEntity.expirationDate.isNull()
                        .or(
                            qNotificationEntity.expirationDate.goe(expirationDate)
                        )
                    )
                )
            );
        long totalCount = query.fetchCount();
        List<Notification> results = getQuerydsl()
                                    .applyPagination(pageable, query)
                                    .fetch();
        return new PageImpl<>(results, pageable, totalCount);
    }

}