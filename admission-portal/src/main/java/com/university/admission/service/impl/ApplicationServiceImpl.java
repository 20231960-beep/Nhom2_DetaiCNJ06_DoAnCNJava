package com.university.admission.service.impl;

import com.university.admission.entity.*;
import com.university.admission.enums.AdmissionCombination;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.enums.DocumentType;
import com.university.admission.enums.SubjectGroup;
import com.university.admission.repository.*;
import com.university.admission.service.ApplicationService;
import com.university.admission.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final AdmissionSessionRepository sessionRepository;
    private final MajorRepository majorRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final SessionMajorRepository sessionMajorRepository;
    private final FileUploadUtil fileUploadUtil;

    // Cung thu muc goc luu file minh chung nhu FileUploadUtil, dung de xoa file khi xoa ho so
    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                   CandidateRepository candidateRepository,
                                   AdmissionSessionRepository sessionRepository,
                                   MajorRepository majorRepository,
                                   DocumentRepository documentRepository,
                                   ApplicationStatusHistoryRepository historyRepository,
                                   SessionMajorRepository sessionMajorRepository,
                                   FileUploadUtil fileUploadUtil) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.sessionRepository = sessionRepository;
        this.majorRepository = majorRepository;
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.sessionMajorRepository = sessionMajorRepository;
        this.fileUploadUtil = fileUploadUtil;
    }

    @Override
    public List<Application> findByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Override
    public Application findById(Long id) {
        return applicationRepository.findDetailedById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay ho so voi id: " + id));
    }

    @Override
    public List<Application> findByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    @Override
    public List<Application> findAllExcludingDraft() {
        return applicationRepository.findByStatusNot(ApplicationStatus.DRAFT);
    }

    @Override
    public List<Application> findAdmittedList() {
        return applicationRepository.findByStatusInOrderByCandidate_User_FullNameAsc(
                java.util.List.of(ApplicationStatus.ADMITTED, ApplicationStatus.ENROLLED));
    }

    @Override
    @Transactional
    public void deleteApplication(Long applicationId) {
        Application app = findById(applicationId);

        // Xoa ban ghi trong DB - FK "documents"/"application_status_history" da co ON DELETE CASCADE
        // nen MySQL se tu dong xoa minh chung va lich su trang thai lien quan, khong can xoa tay tung bang.
        applicationRepository.delete(app);

        // Don dep file minh chung vat ly da upload cho ho so nay tren o dia (neu co).
        // Khong de loi o buoc nay lam that bai toan bo thao tac xoa (ho so DB da xoa xong roi).
        try {
            Path appUploadDir = Paths.get(uploadDir, String.valueOf(applicationId));
            if (Files.exists(appUploadDir)) {
                try (Stream<Path> walk = Files.walk(appUploadDir)) {
                    walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                            // Bo qua file/thu muc khong xoa duoc, khong anh huong ket qua chinh
                        }
                    });
                }
            }
        } catch (IOException ignored) {
            // Khong chan ket qua xoa ho so neu don dep file that bai
        }
    }

    @Override
    public Page<Application> searchApplications(String keyword, ApplicationStatus status, Pageable pageable) {
        // Chuan hoa keyword: trim khoang trang, chuyen "" neu null de query xu ly dong nhat
        String normalizedKeyword = (keyword != null) ? keyword.trim() : "";
        return applicationRepository.search(normalizedKeyword, status, pageable);
    }

    @Override
    @Transactional
    public Application updateSubjectScores(Long applicationId,
                                            BigDecimal mathScore, BigDecimal literatureScore, BigDecimal englishScore,
                                            SubjectGroup subjectGroup,
                                            BigDecimal physicsScore, BigDecimal chemistryScore, BigDecimal biologyScore,
                                            BigDecimal historyScore, BigDecimal geographyScore, BigDecimal civicScore) {
        Application app = findById(applicationId);

        // Reset toan bo 6 diem mon khoi truoc, sau do chi dien lai dung 3 mon ung voi khoi da chon
        // (tranh truong hop thi sinh doi tu Tu nhien sang Xa hoi ma con sot lai diem mon cu trong DB)
        app.setPhysicsScore(null);
        app.setChemistryScore(null);
        app.setBiologyScore(null);
        app.setHistoryScore(null);
        app.setGeographyScore(null);
        app.setCivicScore(null);

        if (subjectGroup == SubjectGroup.NATURAL) {
            if (physicsScore == null || chemistryScore == null || biologyScore == null) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập đủ điểm 3 môn khối Tự nhiên (Vật lý, Hóa học, Sinh học)");
            }
            app.setPhysicsScore(physicsScore);
            app.setChemistryScore(chemistryScore);
            app.setBiologyScore(biologyScore);
        } else if (subjectGroup == SubjectGroup.SOCIAL) {
            if (historyScore == null || geographyScore == null || civicScore == null) {
                throw new IllegalArgumentException(
                        "Vui lòng nhập đủ điểm 3 môn khối Xã hội (Lịch sử, Địa lí, Giáo dục công dân)");
            }
            app.setHistoryScore(historyScore);
            app.setGeographyScore(geographyScore);
            app.setCivicScore(civicScore);
        } else {
            throw new IllegalArgumentException("Vui lòng chọn khối thi (Tự nhiên hoặc Xã hội)");
        }

        app.setMathScore(mathScore);
        app.setLiteratureScore(literatureScore);
        app.setEnglishScore(englishScore);
        app.setSubjectGroup(subjectGroup);

        return applicationRepository.save(app);
    }

    @Override
    @Transactional
    public Application createApplication(Long candidateId, Long sessionId, Long majorId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thi sinh"));
        AdmissionSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay dot tuyen sinh"));
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nganh"));

        Application app = new Application();
        app.setCandidate(candidate);
        app.setAdmissionSession(session);
        app.setMajor(major);
        app.setStatus(ApplicationStatus.DRAFT);
        return applicationRepository.save(app);
    }

    @Override
    @Transactional
    public Application submit(Long applicationId) {
        Application app = findById(applicationId);
        String oldStatus = app.getStatus().name();
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setSubmittedAt(LocalDateTime.now());
        applicationRepository.save(app);

        recordHistory(app, oldStatus, ApplicationStatus.SUBMITTED.name(), null, "Thi sinh nop ho so");
        return app;
    }

    @Override
    @Transactional
    public Application changeStatus(Long applicationId, ApplicationStatus newStatus, User changedBy, String note) {
        Application app = findById(applicationId);
        String oldStatus = app.getStatus().name();
        app.setStatus(newStatus);
        applicationRepository.save(app);

        recordHistory(app, oldStatus, newStatus.name(), changedBy, note);
        return app;
    }

    private void recordHistory(Application app, String oldStatus, String newStatus, User changedBy, String note) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        historyRepository.save(history);
    }

    @Override
    @Transactional
    public Application updateScore(Long applicationId, BigDecimal score, AdmissionCombination admissionCombination) {
        Application app = findById(applicationId);
        app.setScore(score);
        app.setAdmissionCombination(admissionCombination);
        return applicationRepository.save(app);
    }

    @Override
    @Transactional
    public void runAdmission(Long sessionMajorId) {
        SessionMajor sm = sessionMajorRepository.findById(sessionMajorId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chi tieu nganh voi id: " + sessionMajorId));

        Long sessionId = sm.getSession().getId();
        Long majorId = sm.getMajor().getId();
        int quota = sm.getQuota() != null ? sm.getQuota() : 0;

        // Chi xet nhung ho so da duoc nhan vien duyet ho so hop le (APPROVED)
        List<Application> approvedApps = applicationRepository
                .findByAdmissionSession_IdAndMajor_IdAndStatus(sessionId, majorId, ApplicationStatus.APPROVED);

        // Sap xep giam dan theo diem (ho so chua co diem coi nhu 0, xep cuoi cung)
        approvedApps.sort(Comparator.comparing(
                (Application a) -> a.getScore() != null ? a.getScore() : BigDecimal.ZERO
        ).reversed());

        BigDecimal benchmarkScore = null;

        for (int i = 0; i < approvedApps.size(); i++) {
            Application app = approvedApps.get(i);
            String oldStatus = app.getStatus().name();

            if (i < quota) {
                // Nam trong chi tieu -> trung tuyen
                app.setStatus(ApplicationStatus.ADMITTED);
                benchmarkScore = app.getScore(); // diem cua nguoi cuoi cung du chi tieu chinh la diem chuan
            } else {
                app.setStatus(ApplicationStatus.NOT_ADMITTED);
            }

            applicationRepository.save(app);
            recordHistory(app, oldStatus, app.getStatus().name(), null,
                    "He thong tu dong xet tuyen theo diem va chi tieu");
        }

        // Cap nhat lai diem chuan cua nganh trong dot tuyen sinh nay
        if (benchmarkScore != null) {
            sm.setBenchmarkScore(benchmarkScore);
            sessionMajorRepository.save(sm);
        }
    }

    @Override
    @Transactional
    public Document uploadDocument(Long applicationId, DocumentType type, MultipartFile file) throws IOException {
        Application app = findById(applicationId);

        String savedPath = fileUploadUtil.saveFile(file, applicationId);

        Document doc = new Document();
        doc.setApplication(app);
        doc.setDocType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(savedPath);
        return documentRepository.save(doc);
    }

    @Override
    @Transactional
    public void updateDocumentStatus(Long documentId, com.university.admission.enums.DocumentStatus status) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay minh chung voi id: " + documentId));
        doc.setStatus(status);
        documentRepository.save(doc);
    }

    @Override
    public Map<String, Long> countApplicationsByStatus() {
        // Dung LinkedHashMap de giu dung thu tu trang thai (DRAFT -> ... -> ENROLLED) khi ve bieu do
        Map<String, Long> result = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            result.put(status.getLabel(), applicationRepository.countByStatus(status));
        }
        return result;
    }

    @Override
    public Map<String, Long> countApplicationsByMajor() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : applicationRepository.countGroupByMajorName()) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }
}
