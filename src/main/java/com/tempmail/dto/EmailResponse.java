package com.tempmail.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class EmailResponse {
	private final Logger logger = LoggerFactory.getLogger(EmailResponse.class);
	private String emailAddress;
	private String message;
	private long expiresInMinutes = 60;

	public EmailResponse(String emailAddress) {
		this.emailAddress = emailAddress;
		this.message = "Temprory email is created and will expires in 60 minutes";
		logger.info(emailAddress + "this is email23+/");
	}

}
