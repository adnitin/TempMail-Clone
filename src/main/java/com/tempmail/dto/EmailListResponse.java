package com.tempmail.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.Data;

@Data
public class EmailListResponse {

	private String emailAddress;

	private Instant createdAt;

	private boolean isActive;

	private long minutesLeft;

	public EmailListResponse(String emailAddress, Instant createdAt, boolean isActive) {
		this.emailAddress = emailAddress;
		this.createdAt = createdAt;
		this.isActive = isActive;
		this.minutesLeft = ChronoUnit.MINUTES.between(Instant.now(), createdAt.plus(1, ChronoUnit.HOURS));;
	}
	
	
}
