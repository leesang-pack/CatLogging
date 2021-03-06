/*******************************************************************************
 * catlogging, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2021 xzpluszone, www.catlogging.com
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
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
package com.catlogging.message.h2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * H2 based source provider.
 * 
 * @author Tester
 * 
 */
@Slf4j
@Component
public class H2MessageProvider{

	private class SourceRowMapper implements RowMapper<LanguageEntity> {
		private static final String SQL_PROJECTION = "SELECT ID, LOCALE, MESSAGEKEY, MESSAGECONTENT FROM LANGUAGES";

		@Override
		public LanguageEntity mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final int id = rs.getInt("ID");
			final String LOCALE = rs.getString("LOCALE");
			final String MESSAGEKEY = rs.getString("MESSAGEKEY");
			final String MESSAGECONTENT = rs.getString("MESSAGECONTENT");
			final LanguageEntity source = new LanguageEntity();

			source.setId(id);
			source.setLocale(LOCALE);
			source.setMessagekey(MESSAGEKEY);
			source.setMessagecontent(MESSAGECONTENT);

			return source;
		}
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;


	public LanguageEntity findByKeyAndLocale(String key, String locale) {
		final List<LanguageEntity> sources = jdbcTemplate.query(SourceRowMapper.SQL_PROJECTION + " WHERE MESSAGEKEY=? AND LOCALE=? ", new Object[] { key, locale }, new SourceRowMapper());
		return sources.size() > 0 ? sources.get(0) : null;
	}

}
