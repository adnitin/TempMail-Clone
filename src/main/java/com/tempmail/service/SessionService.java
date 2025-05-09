package com.tempmail.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tempmail.exception.InvalidSessionException;
import com.tempmail.model.User;
import com.tempmail.repository.UserRepository;

@Service
public class SessionService {
	private final UserRepository userRepository;
	private static Logger logger = LoggerFactory.getLogger(SessionService.class);

	public SessionService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String createSession() {
		User user = new User();
		user.setSessionToken(UUID.randomUUID().toString());
		User save = userRepository.save(user);
		logger.info("session token :" + save + "=:=" + user);
		return user.getSessionToken();
	}

	public void validateSessionToken(String sessionToken) {
		if (!userRepository.existsBySessionToken(sessionToken)) {
			throw new InvalidSessionException(sessionToken + "is not valid");
		}
	}
}
