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

package com.catlogging.model.profile;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(
        name="user_profile_settings",
        indexes = {
                @Index(name = "user_profile_settings_unique", columnList = "token, path", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@ToString
public class Profile {

    @Id
    private String token;

    private String path;

    @Lob
    private String data;
}