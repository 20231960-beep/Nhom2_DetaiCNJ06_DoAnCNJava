package com.university.admission.service;

import com.university.admission.entity.AdmissionSession;
import com.university.admission.entity.SessionMajor;

import java.util.List;

public interface AdmissionSessionService {
    List<AdmissionSession> findAll();
    List<AdmissionSession> findOpenSessions();
    AdmissionSession findById(Long id);
    AdmissionSession save(AdmissionSession session);
    void delete(Long id);

    // Quan ly chi tieu nganh trong 1 dot tuyen sinh
    List<SessionMajor> getSessionMajors(Long sessionId);
    SessionMajor addMajorToSession(Long sessionId, Long majorId, Integer quota);
    void removeSessionMajor(Long sessionMajorId);
}
