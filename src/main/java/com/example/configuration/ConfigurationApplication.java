package com.example.configuration;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {

		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(Environment environment,
										@Value ("${HOME}") String userHome,
										@Value("${greeting-message: Default Hello: ${message-from-application-properties}}") String defaultValue) {

		return args -> {
		log.info("message from application.properties" + environment.getProperty("message-from-application-properties"));
		log.info("message from application.properties" + environment.getProperty("spring.datasource.url"));
		log.info("default value from application.properties" + defaultValue);
		log.info("user home from the environment variables " + userHome);
		};
	}

}




