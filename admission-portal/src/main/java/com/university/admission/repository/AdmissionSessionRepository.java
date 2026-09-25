package com.university.admission.repository;

import com.university.admission.entity.AdmissionSession;
import com.university.admission.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionSessionRepository extends JpaRepository<AdmissionSession, Long> {
    List<AdmissionSession> findByStatus(SessionStatus status);
}
