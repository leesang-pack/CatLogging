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
package com.catlogging.app;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.sql.SQLException;

/**
 * Configured the live datasource.
 * 
 * @author Tester
 * 
 */
@Configuration
@Slf4j
public class DataSourceAppConfig {
	private static final String DB_SETUP_VERSION = "0.5.5";

	@Value(value = "${catlogging.h2.user}")
	private String user;

	@Value(value = "${catlogging.h2.password}")
	private String password;

	@Value(value = "${catlogging.h2.url}")
	private String url;

	@Value(value = "${catlogging.h2.maxPoolConnections:5}")
	private final int maxPoolConnections = 5;

	/**
	 * @return H2 pooled data source
	 * @throws SQLException
	 */
	@Bean(destroyMethod = "dispose")
	public DataSource dataSource() {
		final JdbcConnectionPool pool = JdbcConnectionPool.create(url, user, password);
		pool.setMaxConnections(maxPoolConnections);

		FluentConfiguration flyway = Flyway.configure()
				.locations("classpath:sql/migration")
				.dataSource(pool)
				.sqlMigrationPrefix("VOS-")
				.encoding(Charset.forName("UTF-8"))
				.ignoreFutureMigrations(true);

			flyway.baselineVersion(MigrationVersion.fromVersion(DB_SETUP_VERSION));
			flyway.baselineOnMigrate(true);

		log.debug("[INIT] Migrating database, base version is: {}", flyway.getBaselineVersion());
		flyway.load().migrate();
		log.debug("Database migrated from base version: {}", flyway.getBaselineVersion());

		return pool;
	}

}
