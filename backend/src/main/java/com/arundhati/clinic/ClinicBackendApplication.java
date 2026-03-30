package com.arundhati.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClinicBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicBackendApplication.class, args);
	}

}
