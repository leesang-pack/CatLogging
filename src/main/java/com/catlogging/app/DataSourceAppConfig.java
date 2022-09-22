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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
//import org.springframework.boot.autoconfigure.jdbc.JdbcOperationsDependsOnPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.sql.Connection;
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

	private boolean newSchema = false;

	/**
	 * Used to indicate if DB is initialized the first time. It helps to drive
	 * further DB initializations.
	 * 
	 * @author Tester
	 * 
	 */
	public static interface DBInitIndicator {
		/**
		 * Returns true if DB was initialized.
		 * 
		 */
		boolean isNewSchema();
	}

	/**
	 * @return H2 pooled data source
	 * @throws SQLException
	 */
	@Bean(destroyMethod = "dispose")
	public DataSource dataSource() throws SQLException {
		final JdbcConnectionPool pool = JdbcConnectionPool.create(url, user, password);
		pool.setMaxConnections(maxPoolConnections);
		Connection con = null;
		con = pool.getConnection();

		FluentConfiguration flyway = Flyway.configure()
				.locations("classpath:sql/migration")
				.dataSource(pool)
				.sqlMigrationPrefix("VOS-")
//				.schemas("PUBLIC")
//				.defaultSchema("PUBLIC")
				.encoding(Charset.forName("UTF-8"))
				.ignoreFutureMigrations(true);
//		final Flyway flyway = new Flyway();
//		flyway.setLocations("classpath:sql/migration");
//		flyway.setDataSource(pool);
//		flyway.setSqlMigrationPrefix("VOS-");
//		flyway.setIgnoreFutureMigrations(true);

		final JdbcTemplate tpl = new JdbcTemplate(pool);
		boolean isExist = false;
		try{
			isExist = tpl.queryForObject("select count(*) from information_schema.tables where table_name = 'LOG_SOURCES'", int.class) == 0 ? false : true ;
		}catch (Exception e){ }

		if (!isExist) {
			log.info("H2 database STATUS : [Not Found], creating new schema and populate with default data");
			flyway.baselineVersion(MigrationVersion.fromVersion(DB_SETUP_VERSION));
			flyway.baselineOnMigrate(true);

			try {
				final ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
//				dbPopulator.addScript(new ClassPathResource("/sql/quartz/tables_h2.sql_"));
				dbPopulator.addScript(new ClassPathResource("/sql/model/schema_h2.sql"));
				dbPopulator.addScript(new ClassPathResource("/sql/model/schema_h2_data.sql"));
				dbPopulator.populate(con);
				log.info("Established H2 connection pool with new database");
			} finally {
				if (con != null) {
					con.close();
				}
			}
		} else {
			log.info("H2 database STATUS : [Found] Established H2 connection pool with existing database");
			try {
				final ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
				dbPopulator.addScript(new ClassPathResource("/sql/model/schema_h2_data.sql"));
				dbPopulator.populate(con);
			} finally {
				if (con != null) {
					con.close();
				}
			}
			if (tpl.queryForObject("select count(*) from information_schema.tables where table_name = 'schema_version'", int.class) == 0) {
				log.info("Flyway's DB migration not setup in this version, set baseline version to 0.5.0");
				flyway.baselineVersion(MigrationVersion.fromVersion("0.5.0"));
				flyway.baselineOnMigrate(true);
			}
		}

		log.debug("Migrating database, base version is: {}", flyway.getBaselineVersion());
		flyway.load().migrate();
		log.debug("Database migrated from base version: {}", flyway.getBaselineVersion());

		if(!isExist)
			newSchema = true;

		return pool;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public DBInitIndicator dbInitPopulate() {
		return new DBInitIndicator() {

			@Override
			public boolean isNewSchema() {
				return newSchema;
			}
		};
	}
}
