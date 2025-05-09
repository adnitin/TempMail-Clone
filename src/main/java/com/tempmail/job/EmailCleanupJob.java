package com.tempmail.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tempmail.repository.TempEmailRepository;

@Component
public class EmailCleanupJob {

	private final TempEmailRepository tempEmailRepository;

	public EmailCleanupJob(TempEmailRepository tempEmailRepository) {
		this.tempEmailRepository = tempEmailRepository;
	}

	@Scheduled(fixedRate = 300000) // Runs every 5 minutes
	public void cleanUpExpiredEmails() {
		Instant cutoffTime = Instant.now().minus(1, ChronoUnit.HOURS);
		tempEmailRepository.deactivateExpiredEmails(cutoffTime);
	}

}
