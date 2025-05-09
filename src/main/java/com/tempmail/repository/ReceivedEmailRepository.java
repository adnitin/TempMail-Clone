package com.tempmail.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempmail.model.ReceivedEmail;
import com.tempmail.model.TempEmail;

public interface ReceivedEmailRepository extends JpaRepository<ReceivedEmail, UUID> {

	List<ReceivedEmail> findByTempEmailOrderByReceivedAtDesc(TempEmail tempEmail);

	ReceivedEmail findByMailIdAndTempEmail(UUID mailId, TempEmail tempEmail);

}
