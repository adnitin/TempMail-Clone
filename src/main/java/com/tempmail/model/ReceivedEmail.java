package com.tempmail.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "received_emails")
public class ReceivedEmail {

	@Id
	@GeneratedValue
	private UUID mailId;

	@ManyToOne
	@JoinColumn(name = "email_id")
	private TempEmail tempEmail;

	private String sender;
	private String subject;
	private String body;

	@CreationTimestamp
	private Instant receivedAt;

	private boolean isRead = false;
}
