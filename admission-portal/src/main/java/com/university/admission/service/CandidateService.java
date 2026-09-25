package com.university.admission.service;

import com.university.admission.dto.request.CandidateProfileRequest;
import com.university.admission.dto.request.RegisterRequest;
import com.university.admission.entity.Candidate;

public interface CandidateService {
    // Dang ky tai khoan moi: tao User (role CANDIDATE) + Candidate rong di kem
    Candidate register(RegisterRequest request);

    Candidate findByUserId(Long userId);

    Candidate save(Candidate candidate);

    // Thi sinh tu cap nhat thong tin ca nhan cua chinh minh
    Candidate updateProfile(Long userId, CandidateProfileRequest request);
}
