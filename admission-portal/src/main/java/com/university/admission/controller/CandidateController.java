package com.university.admission.controller;

import com.university.admission.dto.request.CandidateProfileRequest;
import com.university.admission.entity.Application;
import com.university.admission.entity.Candidate;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.enums.DocumentType;
import com.university.admission.enums.Gender;
import com.university.admission.enums.SubjectGroup;
import com.university.admission.security.CustomUserDetails;
import com.university.admission.service.AdmissionLetterService;
import com.university.admission.service.AdmissionSessionService;
import com.university.admission.service.ApplicationService;
import com.university.admission.service.CandidateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final AdmissionSessionService sessionService;
    private final AdmissionLetterService admissionLetterService;

    public CandidateController(CandidateService candidateService,
                                ApplicationService applicationService,
                                AdmissionSessionService sessionService,
                                AdmissionLetterService admissionLetterService) {
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.sessionService = sessionService;
        this.admissionLetterService = admissionLetterService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("fullName", userDetails.getUser().getFullName());
        Candidate candidate = candidateService.findByUserId(userDetails.getUser().getId());
        model.addAttribute("applications", applicationService.findByCandidateId(candidate.getId()));
        return "candidate/dashboard";
    }

    // Form chon dot tuyen sinh + nganh de tao ho so moi
    @GetMapping("/applications/new")
    public String newApplicationForm(Model model) {
        model.addAttribute("openSessions", sessionService.findOpenSessions());
        return "candidate/application-form";
    }

    @PostMapping("/applications/create")
    public String createApplication(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam Long sessionId,
                                     @RequestParam Long majorId) {
        Candidate candidate = candidateService.findByUserId(userDetails.getUser().getId());
        Application app = applicationService.createApplication(candidate.getId(), sessionId, majorId);
        return "redirect:/candidate/applications/" + app.getId();
    }

    // Xem chi tiet 1 ho so: trang thai, danh sach minh chung, nut nop ho so
    @GetMapping("/applications/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("app", applicationService.findById(id));
        model.addAttribute("documentTypes", DocumentType.values());
        return "candidate/application-detail";
    }

    @PostMapping("/applications/{id}/submit")
    public String submit(@PathVariable Long id) {
        applicationService.submit(id);
        return "redirect:/candidate/applications/" + id;
    }

    @PostMapping("/applications/{id}/upload")
    public String uploadDocument(@PathVariable Long id,
                                  @RequestParam DocumentType docType,
                                  @RequestParam MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            applicationService.uploadDocument(id, docType, file);
        }
        return "redirect:/candidate/applications/" + id;
    }

    // Thi sinh tu nhap diem 6 mon (3 mon chinh + 3 mon khoi tuy chon Tu nhien/Xa hoi) khi nop ho so
    @PostMapping("/applications/{id}/scores")
    public String saveScores(@PathVariable Long id,
                              @RequestParam BigDecimal mathScore,
                              @RequestParam BigDecimal literatureScore,
                              @RequestParam BigDecimal englishScore,
                              @RequestParam SubjectGroup subjectGroup,
                              @RequestParam(required = false) BigDecimal physicsScore,
                              @RequestParam(required = false) BigDecimal chemistryScore,
                              @RequestParam(required = false) BigDecimal biologyScore,
                              @RequestParam(required = false) BigDecimal historyScore,
                              @RequestParam(required = false) BigDecimal geographyScore,
                              @RequestParam(required = false) BigDecimal civicScore,
                              Model model) {
        try {
            applicationService.updateSubjectScores(id, mathScore, literatureScore, englishScore, subjectGroup,
                    physicsScore, chemistryScore, biologyScore, historyScore, geographyScore, civicScore);
        } catch (IllegalArgumentException e) {
            // Thieu diem 3 mon khoi tuong ung -> hien lai trang chi tiet kem thong bao loi
            model.addAttribute("scoreErrorMessage", e.getMessage());
            model.addAttribute("app", applicationService.findById(id));
            model.addAttribute("documentTypes", DocumentType.values());
            return "candidate/application-detail";
        }
        return "redirect:/candidate/applications/" + id;
    }


    // Xuat file PDF "Giay bao trung tuyen" - chi cho phep khi ho so da ADMITTED/ENROLLED,
    // va chi chinh chu thi sinh cua ho so do moi duoc tai (tranh doan/sua URL de xem ho so nguoi khac).
    @GetMapping("/applications/{id}/admission-letter")
    public ResponseEntity<byte[]> exportAdmissionLetter(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Application app = applicationService.findById(id);
        Candidate candidate = candidateService.findByUserId(userDetails.getUser().getId());

        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem hồ sơ này");
        }
        if (app.getStatus() != ApplicationStatus.ADMITTED && app.getStatus() != ApplicationStatus.ENROLLED) {
            throw new IllegalStateException("Chỉ có thể xuất giấy báo trúng tuyển khi hồ sơ đã trúng tuyển");
        }

        byte[] pdf = admissionLetterService.generate(app);
        String fileName = "giay-bao-trung-tuyen-ho-so-" + app.getId() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // "inline" de trinh duyet mo PDF ngay tren tab moi (thay vi bat buoc tai ve) - thi sinh xem truoc roi tu in/luu
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(pdf);
    }

    // Xem trang thong tin ca nhan (ho so Candidate: ngay sinh, CCCD, SDT, dia chi...)
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Candidate candidate = candidateService.findByUserId(userDetails.getUser().getId());

        CandidateProfileRequest form = new CandidateProfileRequest();
        form.setDob(candidate.getDob());
        form.setGender(candidate.getGender());
        form.setIdCardNumber(candidate.getIdCardNumber());
        form.setPhone(candidate.getPhone());
        form.setAddress(candidate.getAddress());
        form.setProvince(candidate.getProvince());
        form.setHighSchool(candidate.getHighSchool());
        form.setGraduationYear(candidate.getGraduationYear());

        model.addAttribute("profileRequest", form);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("fullName", userDetails.getUser().getFullName());
        model.addAttribute("email", userDetails.getUser().getEmail());
        return "candidate/profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @ModelAttribute("profileRequest") CandidateProfileRequest request,
                               Model model) {
        try {
            candidateService.updateProfile(userDetails.getUser().getId(), request);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Thuong xay ra khi so CCCD bi trung voi thi sinh khac (cot idCardNumber la UNIQUE)
            model.addAttribute("errorMessage", "Số CCCD/CMND này đã được sử dụng bởi tài khoản khác.");
            model.addAttribute("profileRequest", request);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("fullName", userDetails.getUser().getFullName());
            model.addAttribute("email", userDetails.getUser().getEmail());
            return "candidate/profile";
        }
        return "redirect:/candidate/profile?updated=true";
    }
}
