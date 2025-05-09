package com.tempmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TempMailApplication {

	public static void main(String[] args) {
		SpringApplication.run(TempMailApplication.class, args);
	}

}
