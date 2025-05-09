package com.tempmail.dto;

import lombok.Data;

@Data
public class SessionResponse {

	private String sessionToken;

	private String message;

	public SessionResponse(String sessionToken) {	
		this.sessionToken = sessionToken;
		this.message = "Session created successfully. Use this token for subsequent requests.";
	}

}
