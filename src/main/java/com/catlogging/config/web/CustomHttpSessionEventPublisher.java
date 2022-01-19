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
package com.catlogging.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomHttpSessionEventPublisher extends HttpSessionEventPublisher {


    @Value("#{'${server.servlet.session.timeout}' < '3600' ? '3600' : '${server.servlet.session.timeout}'}")
    private int sessionTimeSec;

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        log.debug("================= sessionCreated =================");
        event.getSession().setMaxInactiveInterval(sessionTimeSec);
        log.debug("================= Set Session Timeout(s): {} ", event.getSession().getMaxInactiveInterval());
        super.sessionCreated(event);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.debug("================= sessionDestroyed =================");


        HttpSession session = event.getSession();

        /*
            접속 로그아웃 로그 등록
         */
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("session_id", session.getId());

        log.debug("===================> Session INFO : {}" ,paramMap);

        try {
            //TODO : DB
        } catch (Exception ex) {
            log.error("ACCESS LOG UPDATE ERROR : {}", ex);
        }

        super.sessionDestroyed(event);
    }
}
