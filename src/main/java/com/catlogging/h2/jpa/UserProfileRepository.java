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

import com.catlogging.model.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserProfileRepository extends JpaRepository<Profile, String> {

    //네이밍 형식이 있음 JPQL.
    List<Profile> findAllByTokenAndPath(String token, String path);

    // 연관성 쿼리를 날리지 않고 속도 저하없이 해당쿼리 실행.
    //@Modifying 어노테이션이 적용된 쿼리 메서드를 실행한 후,
    // 영속성 컨텍스트(Persistent Context)에 담겨있는 인스턴스를 clear 했다는 것입니다.
    @Modifying(clearAutomatically=true)
    @Query(value="" +
            "DELETE FROM " +
            "   user_profile_settings " +
            "WHERE " +
            "   TOKEN= :token " +
            "   AND PATH LIKE CONCAT(:path,'%') ", nativeQuery=true)
    void deleteAllByTokenAndPathParamsNative(@Param("token") String token, @Param("path") String path);

}