package com.catlogging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = {
		FlywayAutoConfiguration.class
})
@Slf4j
public class CatLoggingApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(CatLoggingApplication.class, args);
	}

}
