package com.projects.cinephiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinephilesApplication {

	public static void main(String[] args) {

		SpringApplication.run(CinephilesApplication.class, args);
	}

}
