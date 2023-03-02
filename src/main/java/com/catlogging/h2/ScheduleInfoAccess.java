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

import java.util.Optional;

import com.catlogging.h2.jpa.SnifferScheduleRepository;
import com.catlogging.model.sniffer.ScheduleInfo;
import com.catlogging.model.sniffer.SnifferScheduleIdKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Update {@link ScheduleInfo} information for sniffers.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class ScheduleInfoAccess {

	@Autowired
	private SnifferScheduleRepository snifferScheduleRepository;

	public void updateScheduleInfo(final long snifferId, final ScheduleInfo info) {
		SnifferScheduleIdKey s = SnifferScheduleIdKey.builder()
				.sniffer(snifferId)
				.scheduleId(snifferId).build();
		ScheduleInfo scheduleInfo = ScheduleInfo.builder()
				.snifferScheduleIdKey(
						s
				)
				.scheduled(info.isScheduled())
				.lastFireTime(info.getLastFireTime())
				.build();
		ScheduleInfo newS = snifferScheduleRepository.save(scheduleInfo);
	}

	public ScheduleInfo getScheduleInfo(final long snifferId) {
		//schedulerId가 아닌 snifferId로 inner join 함
		Optional<ScheduleInfo> info = snifferScheduleRepository.findById(SnifferScheduleIdKey.builder()
				.scheduleId(snifferId)
				.sniffer(snifferId).build());
		return info.orElse(
				ScheduleInfo.builder()
						.scheduled(false)
						.lastFireTime(null)
						.build()
		);
	}
}
