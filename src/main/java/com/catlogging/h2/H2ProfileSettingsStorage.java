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

import com.catlogging.fields.FieldsMap;
import com.catlogging.h2.jpa.UserProfileRepository;
import com.catlogging.model.profile.Profile;
import com.catlogging.user.profile.ProfileSettingsStorage;
import com.catlogging.util.excption.DataAccessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * H2 implementation for {@link ProfileSettingsStorage}. Recursive retrieval
 * isn't supported so far.
 * 
 * @author Tester
 *
 */
@Slf4j
@Component
public class H2ProfileSettingsStorage implements ProfileSettingsStorage {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserProfileRepository userProfileRepository;
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void storeSettings(final String token, final String settingsPath, final FieldsMap data) {
		try {
			Profile profile = Profile.builder()
					.token(token)
					.path(settingsPath)
					.data(objectMapper.writeValueAsString(data))
					.build(); 									// Transient 상태
			userProfileRepository.save(profile); 				// Persistent 상태

			// 참고:
			// update, delete 시 디비에는 반영되지만 캐쉬에서는 반영안됨 (옵션을 줘서 바로 clear)해야함.
			// find 시 // get by Persistence Context (NOT DB)에 의해 캐쉬 있는걸 갓다씀.

		} catch (final JsonProcessingException e) {
			throw new DataAccessException(
					"Failed to store settings for token=" + token + " and path '" + settingsPath + "'", e);
		}
	}

	@Override
	public void deleteSettings(final String token, final String path, final boolean recursive) {
		log.info("Deleting profile settings recursively={} for path: {}", recursive, path);

		Profile profile = Profile.builder()
				.token(token)
				.path(path)
				.build();
		if (recursive) {
			userProfileRepository.deleteAllByTokenAndPathParamsNative(profile.getToken(), profile.getPath());
		} else {
			userProfileRepository.delete(profile);
		}
	}

	@Override
	public FieldsMap getSettings(final String token, final String path, final boolean recursive) {
		if (recursive) {
			throw new NotImplementedException();
		}
		List<Profile> list = userProfileRepository.findAllByTokenAndPath(token, path);
		try {
			if (list.isEmpty()) {
				log.debug("No profile settings found for token={} and path={}", token, path);
				return null;
			} else {
				log.debug("Loaded profile settings for token={} and path={}", token, path);
				return objectMapper.readValue(list.get(0).getData(), FieldsMap.class);
			}
		} catch (final IOException e) {
			log.error("Failed to deserialize profile setting: " + list.get(0), e);
			return null;
		}
	}

}
