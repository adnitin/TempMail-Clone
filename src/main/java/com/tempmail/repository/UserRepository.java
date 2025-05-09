package com.tempmail.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tempmail.model.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findBySessionToken(String sessionToken);

	@Transactional()
	@Query("SELECT COUNT(u) > 0 FROM User u WHERE u.sessionToken = :sessionToken")
	boolean existsBySessionToken(@Param("sessionToken") String sessionToken);

}
