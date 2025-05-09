package com.tempmail.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Data
public class EmailMessageResponse {

	private UUID mailId;
	private String sender;
	private String subject;
	private String bodyPreview; // First 100 chars
	private Instant receivedAt;
	private boolean isRead;


	public EmailMessageResponse(UUID mailId, String sender, String subject, String body, Instant receivedAt,
			boolean isRead) {

		this.mailId = mailId;
		this.sender = sender;
		this.subject = subject;
		this.bodyPreview = body.length() > 100 ? body.substring(0, 100) + "...." : body;
		this.receivedAt = receivedAt;
		this.isRead = isRead;
	}

}
