package com.university.admission.service;

import com.university.admission.dto.request.UserAdminRequest;
import com.university.admission.entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    // Admin tao tai khoan moi (co the la ADMIN, STAFF, hoac CANDIDATE)
    User createUser(UserAdminRequest request);

    // Admin sua thong tin tai khoan. Neu request.password rong -> giu nguyen mat khau cu.
    User updateUser(Long id, UserAdminRequest request);

    // Khoa / mo khoa tai khoan (bat/tat cot enabled)
    void setEnabled(Long id, boolean enabled);

    void deleteUser(Long id);
}
