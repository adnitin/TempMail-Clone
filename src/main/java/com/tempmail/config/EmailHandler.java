package com.tempmail.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

import com.tempmail.model.ReceivedEmail;
import com.tempmail.model.TempEmail;
import com.tempmail.repository.ReceivedEmailRepository;
import com.tempmail.repository.TempEmailRepository;

@Component
public class EmailHandler implements MessageHandlerFactory {

	private static final Logger logger = LoggerFactory.getLogger(EmailHandler.class);

	// Configuration via application.properties
	@Value("${email.max-size:1048576}") // 1MB default
	private long maxEmailSize;

	@Value("${email.body-truncate-length:50000}") // 50KB default
	private int maxBodyLength;

	private final TempEmailRepository tempEmailRepo;
	private final ReceivedEmailRepository receivedEmailRepo;

	public EmailHandler(TempEmailRepository tempEmailRepo, ReceivedEmailRepository receivedEmailRepo) {
		this.tempEmailRepo = tempEmailRepo;
		this.receivedEmailRepo = receivedEmailRepo;
	}

	@Override
	public MessageHandler create(MessageContext ctx) {
		return new MessageHandler() {
			private String sender;
			private String recipient;
			private long bytesReceived = 0;

			@Override
			public void from(String from) {
				this.sender = from;
				logger.info("Receiving email from: {}", from);
			}

			@Override
			public void recipient(String recipient) {
				this.recipient = recipient;
				logger.info("Intended recipient: {}", recipient);
				if (!tempEmailRepo.existsByEmailAddress(recipient)) {
					throw new RuntimeException("Rejected - Invalid recipient: " + recipient);
				}
			}

			@Override
			public void data(InputStream data) throws IOException {
				try {
					// 1. Verify recipient exists
					TempEmail tempEmail = tempEmailRepo.findByEmailAddress(recipient)
							.orElseThrow(() -> new RuntimeException("Temporary email not found"));

					// 2. Read with size limit
					byte[] emailData = readWithSizeLimit(data);

					// 3. Parse with JavaMail
					Session mailSession = Session.getDefaultInstance(new Properties());
					MimeMessage mimeMessage = new MimeMessage(mailSession, new ByteArrayInputStream(emailData));

					// 4. Extract and limit content
					String subject = safeGetSubject(mimeMessage);
					String bodyContent = extractAndLimitContent(mimeMessage);

					// 5. Save to database
					ReceivedEmail receivedEmail = new ReceivedEmail();
					receivedEmail.setTempEmail(tempEmail);
					receivedEmail.setSender(sender);
					receivedEmail.setSubject(subject);
					receivedEmail.setBody(bodyContent);
					receivedEmail.setReceivedAt(Instant.now());
					receivedEmail.setRead(false);

					receivedEmailRepo.save(receivedEmail);

					logger.info("Stored email from {} ({} bytes)", sender, bytesReceived);

				} catch (SizeLimitExceededException e) {
					logger.warn("Rejected oversized email from {} ({} > {} bytes)", sender, bytesReceived,
							maxEmailSize);
					throw new RuntimeException("Email too large - Maximum allowed: " + maxEmailSize + " bytes");
				} catch (Exception e) {
					logger.error("Failed to process email from {}", sender, e);
					throw new RuntimeException("Email processing failed");
				}
			}

			private byte[] readWithSizeLimit(InputStream data) throws IOException, SizeLimitExceededException {
				try {
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = data.read(buffer)) != -1) {
						bytesReceived += bytesRead;
						if (bytesReceived > maxEmailSize) {
							throw new SizeLimitExceededException();
						}
					}
					return IOUtils.toByteArray(new ByteArrayInputStream(buffer));
				} finally {
					data.close();
				}
			}

			private String safeGetSubject(MimeMessage msg) {
				try {
					String subject = msg.getSubject();
					return subject != null ? subject.substring(0, Math.min(200, subject.length())) : "No Subject";
				} catch (Exception e) {
					return "No Subject";
				}
			}

			private String extractAndLimitContent(Part part) throws Exception {
				String content = extractMessageContent(part);
				if (content.length() > maxBodyLength) {
					content = content.substring(0, maxBodyLength) + "\n\n[TRUNCATED - Original message was "
							+ content.length() + " bytes]";
				}
				return content;
			}

			private String extractMessageContent(Part part) throws Exception {
				if (part.isMimeType("text/*")) {
					return (String) part.getContent();
				}

				if (part.isMimeType("multipart/alternative")) {
					// Prefer text over HTML
					Multipart mp = (Multipart) part.getContent();
					for (int i = 0; i < mp.getCount(); i++) {
						BodyPart bodyPart = mp.getBodyPart(i);
						if (bodyPart.isMimeType("text/plain")) {
							return (String) bodyPart.getContent();
						}
					}
					// Fallback to HTML if no text version
					for (int i = 0; i < mp.getCount(); i++) {
						BodyPart bodyPart = mp.getBodyPart(i);
						if (bodyPart.isMimeType("text/html")) {
							return (String) bodyPart.getContent();
						}
					}
				}

				if (part.isMimeType("multipart/*")) {
					Multipart mp = (Multipart) part.getContent();
					for (int i = 0; i < mp.getCount(); i++) {
						String content = extractMessageContent(mp.getBodyPart(i));
						if (content != null && !content.isEmpty()) {
							return content;
						}
					}
				}

				return "Email content could not be displayed";
			}

			@Override
			public void done() {
				logger.debug("Finished processing message from {}", sender);
			}
		};
	}

	private static class SizeLimitExceededException extends Exception {

		private static final long serialVersionUID = 5874443360586272586L;

		public SizeLimitExceededException() {
			super("Email size exceeds maximum allowed");
		}
	}
}