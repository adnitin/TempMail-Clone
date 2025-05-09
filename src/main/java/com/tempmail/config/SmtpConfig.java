package com.tempmail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

@Configuration
public class SmtpConfig {
private static 
	@Bean
	SMTPServer smtpServer(MessageHandlerFactory handlerFactory) {
		SMTPServer server = new SMTPServer(handlerFactory); // Correct constructor
		server.setPort(2525); // Set non-root port
		server.start();
		return server;
	}
}