package com.university.admission.config;

import com.university.admission.security.AuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cau hinh trung tam cua Spring Security:
 * - Ma hoa mat khau bang BCrypt
 * - Phan quyen truy cap theo URL prefix (/admin/**, /staff/**, /candidate/**)
 * - Cau hinh trang dang nhap/dang xuat tuy chinh
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthSuccessHandler authSuccessHandler;

    public SecurityConfig(AuthSuccessHandler authSuccessHandler) {
        this.authSuccessHandler = authSuccessHandler;
    }

    // Bean ma hoa mat khau - dung o moi noi can so sanh/luu mat khau (BCrypt, khong the giai ma nguoc)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Cho phep truy cap khong can dang nhap
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error", "/error/**").permitAll()
                // Phan quyen theo tien to URL - khop voi cau truc package controller/admin, controller/staff
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**").hasRole("STAFF")
                .requestMatchers("/candidate/**").hasRole("CANDIDATE")
                // Con lai bat buoc phai dang nhap
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")           // trang dang nhap tuy chinh (Thymeleaf)
                .successHandler(authSuccessHandler) // dieu huong theo role sau khi dang nhap
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Trang bao loi 403 tuy chinh khi user khong du quyen truy cap
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));
            // Luu y: KHONG tat CSRF. Thymeleaf tu dong chen token CSRF khi form dung th:action.

        return http.build();
    }
}
