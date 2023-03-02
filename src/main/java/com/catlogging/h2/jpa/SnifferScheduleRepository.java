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

package com.catlogging.h2.jpa;

import com.catlogging.model.sniffer.ScheduleInfo;
import com.catlogging.model.sniffer.SnifferScheduleIdKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;



public interface SnifferScheduleRepository extends JpaRepository<ScheduleInfo, SnifferScheduleIdKey>
{

    ScheduleInfo save(ScheduleInfo s);

    // deleteById로 할시 상속되어 있는 Sniffer Table의 Id키가
    // Schedule Table에 반드시 등록되어 있어야하기 때문에 Empty관련 에러발생
    @Modifying(clearAutomatically=true)
    void deleteBySnifferScheduleIdKey(SnifferScheduleIdKey snifferScheduleIdKey);

}