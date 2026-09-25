package com.university.admission.dto.request;

import com.university.admission.enums.Role;

/**
 * Du lieu form tao/sua tai khoan nguoi dung (danh cho Admin).
 * Khi SUA: neu password de trong nghia la GIU NGUYEN mat khau cu.
 * Khi TAO MOI: password la bat buoc.
 */
public class UserAdminRequest {

    private Long id;
    private String username;
    private String password; // co the de trong khi sua
    private String email;
    private String fullName;
    private Role role;
    private boolean enabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
