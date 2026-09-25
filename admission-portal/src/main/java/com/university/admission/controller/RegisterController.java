package com.university.admission.controller;

import com.university.admission.dto.request.RegisterRequest;
import com.university.admission.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final CandidateService candidateService;

    public RegisterController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String submitForm(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                              BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            candidateService.register(request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered=true";
    }
}
