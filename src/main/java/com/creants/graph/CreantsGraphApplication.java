package com.creants.graph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author LamHa
 *
 */
@SpringBootApplication
public class CreantsGraphApplication {
	public static void main(String[] args) {
		System.setProperty("log4j.configurationFile", "config/log4j2.xml");
		SpringApplication.run(CreantsGraphApplication.class, args);
	}
}
