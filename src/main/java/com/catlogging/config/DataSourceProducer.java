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

package com.catlogging.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceProducer {

    private Server server;

    @Value(value = "${catlogging.h2.user}")
    private String user;

    @Value(value = "${catlogging.h2.password}")
    private String password;

    @Value(value = "${catlogging.h2.url}")
    private String url;

    @Value(value = "${catlogging.h2.remoteAccess:false}")
    private boolean isRemoteAccess;

    @PostConstruct
    public DataSource getDataSource() {
        log.info("[INIT] dataSource H2 setting... URL : {} USER : {} ISRemoteAccess : {}", url, user, isRemoteAccess);

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        if(isRemoteAccess)
            initServer();

        return dataSource;
    }

    public boolean initServer()  {

        // start the server
        try {
            server = Server.createTcpServer("-tcpAllowOthers").start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @PreDestroy
    public void disposeDataSource() {
        if(server != null)
            server.stop();
    }
}