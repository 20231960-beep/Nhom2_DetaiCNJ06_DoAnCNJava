package com.university.admission.service.impl;

import com.university.admission.dto.request.UserAdminRequest;
import com.university.admission.entity.Candidate;
import com.university.admission.entity.User;
import com.university.admission.enums.Role;
import com.university.admission.repository.CandidateRepository;
import com.university.admission.repository.UserRepository;
import com.university.admission.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                            CandidateRepository candidateRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan voi id: " + id));
    }

    @Override
    @Transactional
    public User createUser(UserAdminRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ten dang nhap da ton tai");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email da duoc su dung");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Vui long nhap mat khau cho tai khoan moi");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());
        userRepository.save(user);

        // Neu tao tai khoan vai tro CANDIDATE, tao luon ho so Candidate rong di kem
        // (giong het co che tu dang ky, de thi sinh co the dang nhap va nop ho so ngay)
        if (request.getRole() == Role.CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(user);
            candidateRepository.save(candidate);
        }

        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserAdminRequest request) {
        User user = findById(id);

        // Kiem tra trung username/email voi NGUOI KHAC (khong tinh chinh no)
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ten dang nhap da ton tai");
            }
        });
        if (userRepository.existsByEmail(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("Email da duoc su dung");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setEnabled(request.isEnabled());

        // Chi doi mat khau neu admin thuc su nhap gia tri moi
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Role oldRole = user.getRole();
        Role newRole = request.getRole();
        user.setRole(newRole);
        userRepository.save(user);

        // Neu doi VAI TRO THANH CANDIDATE ma truoc do chua co ho so Candidate -> tao moi
        if (newRole == Role.CANDIDATE && oldRole != Role.CANDIDATE) {
            candidateRepository.findByUserId(user.getId()).orElseGet(() -> {
                Candidate candidate = new Candidate();
                candidate.setUser(user);
                return candidateRepository.save(candidate);
            });
        }

        return user;
    }

    @Override
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
