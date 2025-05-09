package com.tempmail.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "temp_emails")
public class TempEmail {

	@Id
	@GeneratedValue
	private UUID emailId;

	@ManyToOne
	@JoinColumn(name = "user_Id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true)
	private String emailAddress;

	@CreationTimestamp
	private Instant createdAt;

	private Boolean isActive = true;
}
