package com.projects.cinephiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
public class CinephilesApplication {
	public static void main(String[] args) {
		SpringApplication.run(CinephilesApplication.class, args);
	}
}

