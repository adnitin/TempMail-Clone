package com.tempmail.controller;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempmail.dto.EmailListResponse;
import com.tempmail.dto.EmailMessageResponse;
import com.tempmail.dto.EmailRequest;
import com.tempmail.dto.EmailResponse;
import com.tempmail.service.EmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
	Logger logger = Logger.getLogger(EmailController.class.getName());
	private final EmailService emailService;

	public EmailController(EmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping
	public EmailResponse emailResponse(@Valid @RequestBody EmailRequest token, @RequestAttribute String sessionId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		logger.info("Current authentication: {}" + auth);
		if (auth == null || !auth.isAuthenticated()) {
			throw new AuthenticationCredentialsNotFoundException("No authentication found");
		}
		EmailResponse tempEmail = emailService.createTempEmail(token.getSessionToken());
		logger.info("EmailResponse:" + tempEmail);
		return tempEmail;
	}

	@GetMapping
	public List<EmailListResponse> getUserEmails(@RequestHeader("X-Session-Token") String sessionToken) {
		logger.info("Processing emails for session: {}" + sessionToken);		
		return emailService.getUserEmails(sessionToken);
	}

	@GetMapping("/{emailIdentifier}/messages")
	public List<EmailMessageResponse> getMessages(@RequestHeader("X-Session-Token") String sessionToken,
			@PathVariable String emailIdentifier) { // Changed parameter name
		return emailService.getEmailMessages(sessionToken, emailIdentifier);
	}

	@GetMapping("/message/{messageId}")
	public EmailMessageResponse getMessage(@RequestHeader("X-Session-Token") String sessionToken,
			@PathVariable UUID messageId) {
		return emailService.getSingleMessage(sessionToken, messageId);
	}

}
