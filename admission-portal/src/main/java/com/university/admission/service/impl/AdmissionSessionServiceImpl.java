package com.university.admission.service.impl;

import com.university.admission.entity.AdmissionSession;
import com.university.admission.entity.Major;
import com.university.admission.entity.SessionMajor;
import com.university.admission.enums.SessionStatus;
import com.university.admission.repository.AdmissionSessionRepository;
import com.university.admission.repository.MajorRepository;
import com.university.admission.repository.SessionMajorRepository;
import com.university.admission.service.AdmissionSessionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmissionSessionServiceImpl implements AdmissionSessionService {

    private final AdmissionSessionRepository sessionRepository;
    private final SessionMajorRepository sessionMajorRepository;
    private final MajorRepository majorRepository;

    public AdmissionSessionServiceImpl(AdmissionSessionRepository sessionRepository,
                                        SessionMajorRepository sessionMajorRepository,
                                        MajorRepository majorRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionMajorRepository = sessionMajorRepository;
        this.majorRepository = majorRepository;
    }

    @Override
    public List<AdmissionSession> findAll() {
        return sessionRepository.findAll();
    }

    @Override
    public List<AdmissionSession> findOpenSessions() {
        return sessionRepository.findByStatus(SessionStatus.OPEN);
    }

    @Override
    public AdmissionSession findById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dot tuyen sinh voi id: " + id));
    }

    @Override
    public AdmissionSession save(AdmissionSession session) {
        return sessionRepository.save(session);
    }

    @Override
    public void delete(Long id) {
        sessionRepository.deleteById(id);
    }

    @Override
    public List<SessionMajor> getSessionMajors(Long sessionId) {
        return sessionMajorRepository.findBySessionId(sessionId);
    }

    @Override
    public SessionMajor addMajorToSession(Long sessionId, Long majorId, Integer quota) {
        AdmissionSession session = findById(sessionId);
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nganh voi id: " + majorId));

        SessionMajor sm = sessionMajorRepository.findBySessionIdAndMajorId(sessionId, majorId)
                .orElse(new SessionMajor());
        sm.setSession(session);
        sm.setMajor(major);
        sm.setQuota(quota);
        return sessionMajorRepository.save(sm);
    }

    @Override
    public void removeSessionMajor(Long sessionMajorId) {
        sessionMajorRepository.deleteById(sessionMajorId);
    }
}
