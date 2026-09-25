package com.university.admission.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicController {

    // Trang chu, ai cung xem duoc
    @GetMapping("/")
    public String home() {
        return "public/home";
    }

    // Trang dang nhap - Spring Security se dieu huong toi day khi chua xac thuc
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Trang bao loi khi truy cap khong du quyen (403)
    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
