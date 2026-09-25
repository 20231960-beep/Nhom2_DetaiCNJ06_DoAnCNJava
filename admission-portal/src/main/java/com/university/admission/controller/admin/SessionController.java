package com.university.admission.controller.admin;

import com.university.admission.entity.AdmissionSession;
import com.university.admission.entity.Major;
import com.university.admission.service.AdmissionSessionService;
import com.university.admission.service.ApplicationService;
import com.university.admission.service.MajorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/sessions")
public class SessionController {

    private final AdmissionSessionService sessionService;
    private final MajorService majorService;
    private final ApplicationService applicationService;

    public SessionController(AdmissionSessionService sessionService, MajorService majorService,
                              ApplicationService applicationService) {
        this.sessionService = sessionService;
        this.majorService = majorService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sessions", sessionService.findAll());
        return "admin/session-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("admissionSession", new AdmissionSession());
        return "admin/session-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("admissionSession", sessionService.findById(id));
        model.addAttribute("sessionMajors", sessionService.getSessionMajors(id));
        model.addAttribute("allMajors", majorService.findAll());
        return "admin/session-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("admissionSession") AdmissionSession session) {
        sessionService.save(session);
        return "redirect:/admin/sessions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        sessionService.delete(id);
        return "redirect:/admin/sessions";
    }

    // Gan nganh + chi tieu vao dot tuyen sinh
    @PostMapping("/{id}/add-major")
    public String addMajor(@PathVariable Long id, @RequestParam Long majorId, @RequestParam Integer quota) {
        sessionService.addMajorToSession(id, majorId, quota);
        return "redirect:/admin/sessions/" + id + "/edit";
    }

    @PostMapping("/session-major/{sessionMajorId}/delete")
    public String removeSessionMajor(@PathVariable Long sessionMajorId, @RequestParam Long sessionId) {
        sessionService.removeSessionMajor(sessionMajorId);
        return "redirect:/admin/sessions/" + sessionId + "/edit";
    }

    // Xet tuyen tu dong: so sanh diem cac ho so APPROVED voi chi tieu, tu dong xep ADMITTED/NOT_ADMITTED
    @PostMapping("/session-major/{sessionMajorId}/run-admission")
    public String runAdmission(@PathVariable Long sessionMajorId, @RequestParam Long sessionId) {
        applicationService.runAdmission(sessionMajorId);
        return "redirect:/admin/sessions/" + sessionId + "/edit";
    }
}
