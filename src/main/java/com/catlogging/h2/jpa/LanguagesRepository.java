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

import com.catlogging.model.locale.LanguageEntity;
import com.catlogging.model.sniffer.Sniffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface LanguagesRepository extends JpaRepository<LanguageEntity, Long> {

    LanguageEntity findByLocaleAndMessagekey(String locale, String messagekey);

}