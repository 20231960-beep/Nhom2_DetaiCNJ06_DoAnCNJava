package com.university.admission.controller.admin;

import com.university.admission.entity.Application;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.security.CustomUserDetails;
import com.university.admission.service.AdmissionSessionService;
import com.university.admission.service.ApplicationService;
import com.university.admission.service.ExcelExportService;
import com.university.admission.service.MajorService;
import com.university.admission.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdmissionSessionService sessionService;
    private final MajorService majorService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final ExcelExportService excelExportService;

    public AdminDashboardController(AdmissionSessionService sessionService, MajorService majorService,
                                     UserService userService, ApplicationService applicationService,
                                     ExcelExportService excelExportService) {
        this.sessionService = sessionService;
        this.majorService = majorService;
        this.userService = userService;
        this.applicationService = applicationService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("fullName", userDetails.getUser().getFullName());
        model.addAttribute("totalSessions", sessionService.findAll().size());
        model.addAttribute("totalMajors", majorService.findAll().size());
        model.addAttribute("totalUsers", userService.findAll().size());
        return "admin/dashboard";
    }

    @GetMapping("/statistics")
    public String statistics(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("fullName", userDetails.getUser().getFullName());

        // Thong ke theo trang thai ho so - tach Map thanh 2 danh sach song song de Chart.js de dung
        Map<String, Long> byStatus = applicationService.countApplicationsByStatus();
        List<String> statusLabels = new ArrayList<>(byStatus.keySet());
        List<Long> statusCounts = new ArrayList<>(byStatus.values());
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusCounts", statusCounts);

        // Thong ke theo nganh
        Map<String, Long> byMajor = applicationService.countApplicationsByMajor();
        List<String> majorLabels = new ArrayList<>(byMajor.keySet());
        List<Long> majorCounts = new ArrayList<>(byMajor.values());
        model.addAttribute("majorLabels", majorLabels);
        model.addAttribute("majorCounts", majorCounts);

        // Tong so ho so thuc su (khong tinh DRAFT) de hien thi nhanh
        long totalReal = statusCounts.stream().mapToLong(Long::longValue).sum() - byStatus.getOrDefault("Nháp", 0L);
        model.addAttribute("totalApplications", totalReal);

        return "admin/statistics";
    }

    @GetMapping("/applications")
    public String applications(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(required = false) ApplicationStatus status,
                                @RequestParam(required = false) String keyword,
                                @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model) {
        model.addAttribute("fullName", userDetails.getUser().getFullName());

        Page<Application> applicationPage = applicationService.searchApplications(keyword, status, pageable);

        model.addAttribute("pendingApplications", applicationPage.getContent());
        model.addAttribute("page", applicationPage);
        model.addAttribute("allStatuses", ApplicationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin/application-list";
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
}
