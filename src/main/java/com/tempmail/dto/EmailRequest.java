package com.tempmail.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequest {
	@NotBlank(message = "Session Token is requied")
	private String sessionToken;
}
