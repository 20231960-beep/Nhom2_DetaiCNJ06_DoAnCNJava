package com.university.admission.service.impl;

import com.university.admission.dto.request.CandidateProfileRequest;
import com.university.admission.dto.request.RegisterRequest;
import com.university.admission.entity.Candidate;
import com.university.admission.entity.User;
import com.university.admission.enums.Role;
import com.university.admission.repository.CandidateRepository;
import com.university.admission.repository.UserRepository;
import com.university.admission.service.CandidateService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateServiceImpl implements CandidateService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public CandidateServiceImpl(UserRepository userRepository,
                                 CandidateRepository candidateRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Candidate register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ten dang nhap da ton tai");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email da duoc su dung");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // MA HOA mat khau bang BCrypt truoc khi luu - khong bao gio luu plaintext
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(Role.CANDIDATE);
        user.setEnabled(true);
        userRepository.save(user);

        Candidate candidate = new Candidate();
        candidate.setUser(user);
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate findByUserId(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ho so thi sinh"));
    }

    @Override
    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    @Override
    @Transactional
    public Candidate updateProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = findByUserId(userId);
        candidate.setDob(request.getDob());
        candidate.setGender(request.getGender());
        candidate.setIdCardNumber(request.getIdCardNumber());
        candidate.setPhone(request.getPhone());
        candidate.setAddress(request.getAddress());
        candidate.setProvince(request.getProvince());
        candidate.setHighSchool(request.getHighSchool());
        candidate.setGraduationYear(request.getGraduationYear());
        return candidateRepository.save(candidate);
    }
}
