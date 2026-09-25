package com.university.admission.controller.admin;

import com.university.admission.entity.Major;
import com.university.admission.enums.AdmissionCombination;
import com.university.admission.service.MajorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/majors")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("majors", majorService.findAll());
        return "admin/major-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("major", new Major());
        model.addAttribute("allCombinations", AdmissionCombination.values());
        return "admin/major-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("major", majorService.findById(id));
        model.addAttribute("allCombinations", AdmissionCombination.values());
        return "admin/major-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Major major) {
        majorService.save(major);
        return "redirect:/admin/majors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        majorService.delete(id);
        return "redirect:/admin/majors";
    }
}
