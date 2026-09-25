package com.university.admission.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Du lieu form dang ky tai khoan thi sinh.
 */
public class RegisterRequest {

    @NotBlank(message = "Vui long nhap ten dang nhap")
    @Size(min = 4, max = 50, message = "Ten dang nhap phai tu 4-50 ky tu")
    private String username;

    @NotBlank(message = "Vui long nhap mat khau")
    @Size(min = 6, message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message = "Vui long nhap email")
    @Email(message = "Email khong hop le")
    private String email;

    @NotBlank(message = "Vui long nhap ho ten")
    private String fullName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
