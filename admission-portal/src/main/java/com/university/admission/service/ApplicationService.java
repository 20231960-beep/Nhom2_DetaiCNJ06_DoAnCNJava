package com.university.admission.service;

import com.university.admission.entity.Application;
import com.university.admission.entity.Document;
import com.university.admission.entity.User;
import com.university.admission.enums.AdmissionCombination;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.enums.DocumentType;
import com.university.admission.enums.SubjectGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ApplicationService {

    List<Application> findByCandidateId(Long candidateId);

    Application findById(Long id);

    List<Application> findByStatus(ApplicationStatus status);

    // Lay tat ca ho so tru ban nhap DRAFT - dung cho trang danh sach cua nhan vien
    List<Application> findAllExcludingDraft();

    // Danh sach thi sinh TRUNG TUYEN (ADMITTED hoac da ENROLLED), sap xep theo ten - dung de xuat Excel
    List<Application> findAdmittedList();

    // Staff xoa han 1 ho so dang ky xet tuyen. DB da cau hinh ON DELETE CASCADE nen minh chung
    // (documents) va lich su trang thai (application_status_history) se tu dong bi xoa theo.
    // File vat ly da upload cua ho so nay cung se duoc don dep tren o dia.
    void deleteApplication(Long applicationId);

    // Tim kiem theo ten thi sinh (co the de trong) + loc theo trang thai (co the null) + phan trang
    Page<Application> searchApplications(String keyword, ApplicationStatus status, Pageable pageable);

    // Thi sinh tu nhap diem 6 mon khi nop ho so: 3 mon chinh (bat buoc) + 3 mon khoi tuy theo subjectGroup.
    // Neu subjectGroup = NATURAL thi physics/chemistry/biology bat buoc (history/geography/civic se bi xoa).
    // Neu subjectGroup = SOCIAL thi history/geography/civic bat buoc (physics/chemistry/biology se bi xoa).
    // Nem IllegalArgumentException neu thieu diem 3 mon khoi tuong ung voi subjectGroup da chon.
    Application updateSubjectScores(Long applicationId,
                                     BigDecimal mathScore, BigDecimal literatureScore, BigDecimal englishScore,
                                     SubjectGroup subjectGroup,
                                     BigDecimal physicsScore, BigDecimal chemistryScore, BigDecimal biologyScore,
                                     BigDecimal historyScore, BigDecimal geographyScore, BigDecimal civicScore);

    // Thi sinh tao ho so moi (trang thai DRAFT)
    Application createApplication(Long candidateId, Long sessionId, Long majorId);

    // Thi sinh nop ho so chinh thuc (DRAFT -> SUBMITTED)
    Application submit(Long applicationId);

    // Nhan vien/Admin doi trang thai ho so, tu dong ghi lich su
    Application changeStatus(Long applicationId, ApplicationStatus newStatus, User changedBy, String note);

    // Nhan vien nhap diem xet tuyen cho ho so
    // Staff nhap diem trung tuyen kem khoi trung tuyen (VD: A00). admissionCombination co the null
    // (truong hop chua chon khoi, chi nhap diem truoc).
    Application updateScore(Long applicationId, java.math.BigDecimal score, AdmissionCombination admissionCombination);

    // Xet tuyen TU DONG: so sanh diem cac ho so APPROVED voi chi tieu, tu dong xep ADMITTED/NOT_ADMITTED,
    // dong thoi cap nhat diem chuan (benchmark_score) cho SessionMajor tuong ung.
    void runAdmission(Long sessionMajorId);

    // Upload 1 file minh chung cho ho so
    Document uploadDocument(Long applicationId, DocumentType type, MultipartFile file) throws IOException;

    // Nhan vien danh gia 1 minh chung la hop le/khong hop le
    void updateDocumentStatus(Long documentId, com.university.admission.enums.DocumentStatus status);

    // Thong ke so ho so theo tung trang thai (nhan tieng Viet -> so luong), dung cho bieu do Admin
    Map<String, Long> countApplicationsByStatus();

    // Thong ke so ho so theo tung nganh (ten nganh -> so luong), dung cho bieu do Admin
    Map<String, Long> countApplicationsByMajor();
}
