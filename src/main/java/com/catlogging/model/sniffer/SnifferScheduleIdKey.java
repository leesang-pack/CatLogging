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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Data
public class SnifferScheduleIdKey implements Serializable {

    @Column(name = "scheduleId", nullable = false)
    private Long  scheduleId;

    @Column(name = "sniffer", nullable = false)
    private Long sniffer;

    @Override
    public boolean equals(Object o) {
        return (
                (o instanceof SnifferScheduleIdKey)
                        && sniffer == ((SnifferScheduleIdKey)o).getSniffer()
                        && scheduleId == ((SnifferScheduleIdKey) o).getScheduleId()
        );
    }

    @Override
    public int hashCode() {
        return (int)(sniffer ^ scheduleId);
    }
}