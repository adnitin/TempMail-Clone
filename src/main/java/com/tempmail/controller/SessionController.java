package com.tempmail.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tempmail.security.JwtTokenUtil;
import com.tempmail.service.SessionService;

@RestController
@RequestMapping("/api/session")
public class SessionController {
	private static Logger logger = LoggerFactory.getLogger(SessionController.class);
	private final SessionService sessionService;
	private final JwtTokenUtil jwtTokenUtil;

	public SessionController(SessionService sessionService, JwtTokenUtil jwtTokenUtil) {
		this.sessionService = sessionService;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@PostMapping
	public Map<String, String> creatSession() {
		String sessionToken = sessionService.createSession();
		Map<String, String> of = Map.of("token", jwtTokenUtil.generateToken(sessionToken), "Session id is {}",
				sessionToken, "message", "Session created with sesion id and Token");		
		/*
		 * String rawSecret = jwtTokenUtil.getSecret(); // Add this getter temporarily
		 * logger.info("Using secret: {}", rawSecret);
		 */
		String token = jwtTokenUtil.generateToken(sessionToken);
//		logger.info("Generated token: {}", token);
		return of;
	}

}
