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

import com.catlogging.h2.jpa.LanguagesRepository;
import com.catlogging.model.locale.LanguageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * H2 based source provider.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class H2MessageProvider{

	@Autowired
	private LanguagesRepository languagesRepository;

	public LanguageEntity findByKeyAndLocale(String key, String locale) {

		return languagesRepository.findByLocaleAndMessagekey(locale, key);
	}

}
