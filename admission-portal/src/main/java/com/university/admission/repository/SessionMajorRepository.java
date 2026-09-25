package com.university.admission.repository;

import com.university.admission.entity.SessionMajor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionMajorRepository extends JpaRepository<SessionMajor, Long> {
    List<SessionMajor> findBySessionId(Long sessionId);
    Optional<SessionMajor> findBySessionIdAndMajorId(Long sessionId, Long majorId);
}
