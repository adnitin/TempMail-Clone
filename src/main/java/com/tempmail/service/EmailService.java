package com.tempmail.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tempmail.dto.EmailListResponse;
import com.tempmail.dto.EmailMessageResponse;
import com.tempmail.dto.EmailResponse;
import com.tempmail.exception.AccessDeniedException;
import com.tempmail.exception.InvalidSessionException;
import com.tempmail.exception.ResourceNotFoundException;
import com.tempmail.model.ReceivedEmail;
import com.tempmail.model.TempEmail;
import com.tempmail.model.User;
import com.tempmail.repository.ReceivedEmailRepository;
import com.tempmail.repository.TempEmailRepository;
import com.tempmail.repository.UserRepository;

@Service
public class EmailService {
	private static Logger logger = LoggerFactory.getLogger(EmailService.class);
	private final TempEmailRepository tempEmailRepository;
	private final UserRepository userRepository;

	private final ReceivedEmailRepository receivedEmailRepository;

	public EmailService(TempEmailRepository tempEmailRepository, UserRepository userRepository,
			ReceivedEmailRepository receivedEmailRepository) {

		this.tempEmailRepository = tempEmailRepository;
		this.userRepository = userRepository;
		this.receivedEmailRepository = receivedEmailRepository;
	}

	public User validateSession(String sessionToken) {
		User user = userRepository.findBySessionToken(sessionToken)
				.orElseThrow(() -> new InvalidSessionException("Invalid Session Token"));
		return user;
	}

	public EmailResponse createTempEmail(String sessionToken) {
//		User user = userRepository.findBySessionToken(sessionToken)
//				.orElseThrow(() -> new InvalidSessionException("Invalid Session Token"));
		User user = validateSession(sessionToken);

		String randomLocalPart = UUID.randomUUID().toString().substring(0, 8);
		String emailAddress = randomLocalPart + "@tempdomain.com";

		TempEmail tempEmail = new TempEmail();
		tempEmail.setUser(user);
		tempEmail.setEmailAddress(emailAddress);
		tempEmailRepository.save(tempEmail);

		return new EmailResponse(emailAddress);
	}

	public List<EmailListResponse> getUserEmails(String sessionToken) {
		// User user = userRepository.findBySessionToken(sessionToken)
		// .orElseThrow(() -> new InvalidSessionException("Invalid session token"));
		User user = validateSession(sessionToken);
		logger.info("User for current requesst is:" + user);
		List<EmailListResponse> emailsInfo = tempEmailRepository.findByUser(user).stream()
				.map(email -> new EmailListResponse(email.getEmailAddress(), email.getCreatedAt(), email.getIsActive()))
				.collect(Collectors.toList());

		return emailsInfo;

	}

	public List<EmailMessageResponse> getEmailMessages(String sessionToken, String emailAddressOrId) {
		User user = userRepository.findBySessionToken(sessionToken)
				.orElseThrow(() -> new InvalidSessionException("Invalid session token"));// session validation

		/*
		 * TempEmail tempEmail =
		 * tempEmailRepository.findByEmailAddressAndUser(emailAddress, user)
		 * .orElseThrow(() -> new ResourceNotFoundException("Email not found"));
		 * 
		 * return
		 * receivedEmailRepository.findByTempEmailOrderByReceivedAtDesc(tempEmail).
		 * stream() .map(email -> new EmailMessageResponse(email.getMailId(),
		 * email.getSender(), email.getSubject(), email.getBody(),
		 * email.getReceivedAt(), email.isRead())) .collect(Collectors.toList());
		 */
		// Handle both UUID (ID) and email address formats
		TempEmail tempEmail;
		try {
			UUID emailId = UUID.fromString(emailAddressOrId);

			tempEmail = tempEmailRepository.findById(emailId)
					.orElseThrow(() -> new ResourceNotFoundException("Email not found by ID"));
		} catch (IllegalArgumentException e) {
			// If not UUID, treat as email address
			tempEmail = tempEmailRepository.findByEmailAddress(emailAddressOrId)
					.orElseThrow(() -> new ResourceNotFoundException("Email not found by address"));
		}

		// Verify email belongs to user
		if (!tempEmail.getUser().getUserId().equals(user.getUserId())) {
			throw new AccessDeniedException("Not authorized");
		}

		return receivedEmailRepository.findByTempEmailOrderByReceivedAtDesc(tempEmail).stream().map(this::convertToDto)
				.collect(Collectors.toList());
	}

	private EmailMessageResponse convertToDto(ReceivedEmail email) {
		return new EmailMessageResponse(email.getMailId(), email.getSender(), email.getSubject(), email.getBody(),
				email.getReceivedAt(), email.isRead());
	}

	public EmailMessageResponse getSingleMessage(String sessionToken, UUID messageId) {
		User user = userRepository.findBySessionToken(sessionToken)
				.orElseThrow(() -> new InvalidSessionException("Invalid session token"));
//		logger.info("getSingleMessage :: user :" + user);
		ReceivedEmail email = receivedEmailRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message not found"));
//		logger.info("getSingleMessage :: email :" + email);
		// Verify email belongs to user's temp email
		if (!email.getTempEmail().getUser().equals(user)) {
			throw new SecurityException("Not authorized to access this message");

		}

		// Mark as read
		if (!email.isRead()) {
			email.setRead(true);
			receivedEmailRepository.save(email);
		}

		return new EmailMessageResponse(email.getMailId(), email.getSender(), email.getSubject(), email.getBody(),
				email.getReceivedAt(), true);
	}
}
