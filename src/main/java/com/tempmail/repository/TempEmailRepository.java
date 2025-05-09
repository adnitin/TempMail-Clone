package com.tempmail.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tempmail.model.TempEmail;
import com.tempmail.model.User;

import jakarta.transaction.Transactional;

public interface TempEmailRepository extends JpaRepository<TempEmail, UUID> {

	List<TempEmail> findByUser(User user);

	boolean existsByEmailAddress(String emailAddress);

	@Modifying
	@Transactional
	@Query("UPDATE TempEmail e SET e.isActive = false WHERE e.createdAt < :cutoff AND e.isActive = true")
	void deactivateExpiredEmails(@Param("cutoff") Instant cutoff);

	TempEmail findByUserAndEmailAddress(User user, String emailAddress);

	Optional<TempEmail> findByEmailAddressAndUser(String emailAddress, User user);

	Optional<TempEmail> findByEmailAddress(String emailAddressOrId);
}
