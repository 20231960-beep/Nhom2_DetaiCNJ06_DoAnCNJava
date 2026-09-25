package com.university.admission.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sau khi dang nhap thanh cong, dieu huong nguoi dung ve dashboard
 * phu hop voi vai tro (ADMIN / STAFF / CANDIDATE) thay vi mot trang chung.
 */
@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        String redirectUrl = switch (role) {
            case "ROLE_ADMIN" -> "/admin/dashboard";
            case "ROLE_STAFF" -> "/staff/dashboard";
            default -> "/candidate/dashboard"; // ROLE_CANDIDATE
        };

        response.sendRedirect(redirectUrl);
    }
}
