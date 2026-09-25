package com.university.admission.controller.staff;

import com.university.admission.entity.Application;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.security.CustomUserDetails;
import com.university.admission.service.ApplicationService;
import com.university.admission.service.ExcelExportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    private final ApplicationService applicationService;
    private final ExcelExportService excelExportService;

    public StaffDashboardController(ApplicationService applicationService, ExcelExportService excelExportService) {
        this.applicationService = applicationService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(required = false) ApplicationStatus status,
                             @RequestParam(required = false) String keyword,
                             @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                             Model model) {
        model.addAttribute("fullName", userDetails.getUser().getFullName());

        // Tim kiem theo ten thi sinh (keyword co the rong) + loc theo trang thai (co the null) + phan trang
        Page<Application> applicationPage = applicationService.searchApplications(keyword, status, pageable);

        model.addAttribute("pendingApplications", applicationPage.getContent());
        model.addAttribute("page", applicationPage);
        model.addAttribute("allStatuses", ApplicationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "staff/dashboard";
    }

    // Xuat danh sach thi sinh TRUNG TUYEN (ADMITTED/ENROLLED) ra file Excel de tai ve
    @GetMapping("/applications/export-admitted")
    public ResponseEntity<byte[]> exportAdmittedList() {
        byte[] excel = excelExportService.exportAdmittedList(applicationService.findAdmittedList());
        String fileName = "danh-sach-trung-tuyen.xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(excel);
    }

    // Staff xoa han 1 ho so dang ky xet tuyen cua thi sinh (khong the hoan tac)
    @PostMapping("/applications/{id}/delete")
    public String deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return "redirect:/staff/dashboard";
    }

    @GetMapping("/applications/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("app", applicationService.findById(id));
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("combinations", com.university.admission.enums.AdmissionCombination.values());
        return "staff/review-detail";
    }

    @PostMapping("/applications/{id}/change-status")
    public String changeStatus(@PathVariable Long id,
                                @RequestParam ApplicationStatus newStatus,
                                @RequestParam(required = false) String note,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        applicationService.changeStatus(id, newStatus, userDetails.getUser(), note);
        return "redirect:/staff/applications/" + id;
    }

    @PostMapping("/applications/{id}/update-score")
    public String updateScore(@PathVariable Long id,
                               @RequestParam java.math.BigDecimal score,
                               @RequestParam(required = false) com.university.admission.enums.AdmissionCombination admissionCombination) {
        applicationService.updateScore(id, score, admissionCombination);
        return "redirect:/staff/applications/" + id;
    }

    @PostMapping("/documents/{docId}/status")
    public String updateDocumentStatus(@PathVariable Long docId,
                                        @RequestParam com.university.admission.enums.DocumentStatus status,
                                        @RequestParam Long applicationId) {
        applicationService.updateDocumentStatus(docId, status);
        return "redirect:/staff/applications/" + applicationId;
    }
}
