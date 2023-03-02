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

package com.catlogging.h2;

import com.catlogging.h2.dsl.NotificationRepositorySupport;
import com.catlogging.h2.jpa.NotificationAckRepository;
import com.catlogging.h2.jpa.NotificationRepository;
import com.catlogging.model.notification.Notification;
import com.catlogging.model.notification.Notification.Level;
import com.catlogging.model.notification.Notification.Type;
import com.catlogging.model.notification.NotificationAcks;
import com.catlogging.system.notification.NotificationProvider;
import com.catlogging.util.PageableResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Date;

/**
 * H2 implementation of {@link NotificationProvider}.
 * 
 * @author Tester
 *
 */
@Slf4j
@Component
public class H2NotificationProvider implements NotificationProvider {
	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationAckRepository notificationAckRepository;

	@Autowired
	private NotificationRepositorySupport notificationRepositorySupport;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean store(final Notification n, final boolean override) {
		Notification newNoti = notificationRepository.save(n);
		if(newNoti == null){
			return false;
		}
		return true;
	}

	@Override
	public PageableResult<Notification> getNotifications(final String userToken, final int from, final int limit) {
		final PageableResult<Notification> n = new PageableResult<>();
		n.setTotalCount(getSummary(userToken).getCount());

		Page<Notification> notificationPage = notificationRepositorySupport.findAllByTypeAndExpirationDate(
			userToken,
			Type.MESSAGE.ordinal(),
			new Date(),
			PageRequest.of(0, Integer.MAX_VALUE,
				// 주의 : Camel Case는 JAVA기본문법이라 잘되지만 _ 붙는 컬럼명 사용 시 (즉 snake Case)는 sort시 token에 변환에 의해 문제발생
				Sort.by(Sort.Order.desc("level"))
				.and(
					Sort.by(Sort.Order.desc("creationDate")))//Jpa 기본컬럼, QueryDSL일때는 VO 이름.. 자동으로 'X.'creation 이 붙는다.
			));
		n.setItems(notificationPage.getContent());
		return n;
	}

	@Override
	public NotificationSummary getSummary(final String userToken) {
		final long start = System.currentTimeMillis();
		Page<Notification> notificationPage = notificationRepositorySupport.findAllByTypeAndExpirationDate(
			userToken,
			Type.MESSAGE.ordinal(),
			new Date(),
			PageRequest.of(0, Integer.MAX_VALUE,
				Sort.by(Sort.Order.desc("level"))
					.and(
					Sort.by(Sort.Order.desc("creationDate")))
				));

		Notification notification = notificationPage.getContent().stream()
				.max(Comparator.comparing(Notification::getLevel))
				.orElse(null);

		Level worst = notification==null ? null : notification.getLevel();
		log.debug("Took {}ms for calculating notification summary CNT:[{}] MAX LEVEL:[{}]", System.currentTimeMillis() - start, notificationPage.getContent().size(), worst);
		return new NotificationSummary((int) notificationPage.getContent().size(), worst);
	}

	@Override
	@Transactional
	public void acknowledge(final String notificationId, final String userToken) {
		log.debug("Ack! notification: {}", notificationId);
		notificationAckRepository.save(NotificationAcks.builder()
										.id(notificationId)
										.systemUserName(userToken)
										.build());
	}

	@Override
	//	- 전파 옵션
	//    - **REQUIRED**(default): 부모 트랜잭션이 있으면 참여, 없으면 새로 생성
	@Transactional
	public void delete(final String notificationId) {
		log.debug("Deleting notification: {}", notificationId);
		notificationAckRepository.deleteById(notificationId);
		notificationRepository.deleteById(notificationId);
	}

}
