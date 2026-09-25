package com.university.admission.controller.admin;

import com.university.admission.dto.request.UserAdminRequest;
import com.university.admission.entity.User;
import com.university.admission.enums.Role;
import com.university.admission.security.CustomUserDetails;
import com.university.admission.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/user-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("userRequest", new UserAdminRequest());
        model.addAttribute("roles", Role.values());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);

        UserAdminRequest request = new UserAdminRequest();
        request.setId(user.getId());
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setFullName(user.getFullName());
        request.setRole(user.getRole());
        request.setEnabled(user.isEnabled());
        // Khong dien lai mat khau cu (da ma hoa, khong the/khong nen hien thi)

        model.addAttribute("userRequest", request);
        model.addAttribute("roles", Role.values());
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("userRequest") UserAdminRequest request,
                        Model model) {
        try {
            if (request.getId() == null) {
                userService.createUser(request);
            } else {
                userService.updateUser(request.getId(), request);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("isEdit", request.getId() != null);
            return "admin/user-form";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Khong cho admin tu xoa chinh tai khoan dang dang nhap - tranh khoa mat quyen truy cap
        if (!currentUser.getUser().getId().equals(id)) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users";
    }
}
