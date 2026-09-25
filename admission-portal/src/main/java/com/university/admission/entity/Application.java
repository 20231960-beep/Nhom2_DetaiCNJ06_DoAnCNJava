package com.university.admission.entity;

import com.university.admission.enums.AdmissionCombination;
import com.university.admission.enums.ApplicationStatus;
import com.university.admission.enums.SubjectGroup;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity TRUNG TAM cua he thong - dai dien cho mot ho so dang ky xet tuyen.
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private AdmissionSession admissionSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "priority_order")
    private Integer priorityOrder = 1;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    // Khoi trung tuyen (VD: A00, D01...) - do STAFF chon khi nhap diem trung tuyen,
    // khac voi "subjectGroup" (Tu nhien/Xa hoi) la do THI SINH tu chon luc nop ho so.
    @Enumerated(EnumType.STRING)
    @Column(name = "admission_combination", length = 10)
    private AdmissionCombination admissionCombination;

    // ===== Diem 6 mon do thi sinh tu khai khi nop ho so =====
    // 3 mon chinh - bat buoc voi moi thi sinh
    @Column(name = "math_score", precision = 4, scale = 2)
    private BigDecimal mathScore;

    @Column(name = "literature_score", precision = 4, scale = 2)
    private BigDecimal literatureScore;

    @Column(name = "english_score", precision = 4, scale = 2)
    private BigDecimal englishScore;

    // Khoi thi sinh chon: Tu nhien hoac Xa hoi
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_group", length = 20)
    private SubjectGroup subjectGroup;

    // 3 mon khoi Tu nhien - chi co gia tri neu subjectGroup = NATURAL
    @Column(name = "physics_score", precision = 4, scale = 2)
    private BigDecimal physicsScore;

    @Column(name = "chemistry_score", precision = 4, scale = 2)
    private BigDecimal chemistryScore;

    @Column(name = "biology_score", precision = 4, scale = 2)
    private BigDecimal biologyScore;

    // 3 mon khoi Xa hoi - chi co gia tri neu subjectGroup = SOCIAL
    @Column(name = "history_score", precision = 4, scale = 2)
    private BigDecimal historyScore;

    @Column(name = "geography_score", precision = 4, scale = 2)
    private BigDecimal geographyScore;

    @Column(name = "civic_score", precision = 4, scale = 2)
    private BigDecimal civicScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApplicationStatusHistory> statusHistories = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public AdmissionSession getAdmissionSession() { return admissionSession; }
    public void setAdmissionSession(AdmissionSession admissionSession) { this.admissionSession = admissionSession; }

    public Major getMajor() { return major; }
    public void setMajor(Major major) { this.major = major; }

    public Integer getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(Integer priorityOrder) { this.priorityOrder = priorityOrder; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public AdmissionCombination getAdmissionCombination() { return admissionCombination; }
    public void setAdmissionCombination(AdmissionCombination admissionCombination) { this.admissionCombination = admissionCombination; }

    public BigDecimal getMathScore() { return mathScore; }
    public void setMathScore(BigDecimal mathScore) { this.mathScore = mathScore; }

    public BigDecimal getLiteratureScore() { return literatureScore; }
    public void setLiteratureScore(BigDecimal literatureScore) { this.literatureScore = literatureScore; }

    public BigDecimal getEnglishScore() { return englishScore; }
    public void setEnglishScore(BigDecimal englishScore) { this.englishScore = englishScore; }

    public SubjectGroup getSubjectGroup() { return subjectGroup; }
    public void setSubjectGroup(SubjectGroup subjectGroup) { this.subjectGroup = subjectGroup; }

    public BigDecimal getPhysicsScore() { return physicsScore; }
    public void setPhysicsScore(BigDecimal physicsScore) { this.physicsScore = physicsScore; }

    public BigDecimal getChemistryScore() { return chemistryScore; }
    public void setChemistryScore(BigDecimal chemistryScore) { this.chemistryScore = chemistryScore; }

    public BigDecimal getBiologyScore() { return biologyScore; }
    public void setBiologyScore(BigDecimal biologyScore) { this.biologyScore = biologyScore; }

    public BigDecimal getHistoryScore() { return historyScore; }
    public void setHistoryScore(BigDecimal historyScore) { this.historyScore = historyScore; }

    public BigDecimal getGeographyScore() { return geographyScore; }
    public void setGeographyScore(BigDecimal geographyScore) { this.geographyScore = geographyScore; }

    public BigDecimal getCivicScore() { return civicScore; }
    public void setCivicScore(BigDecimal civicScore) { this.civicScore = civicScore; }

    // Tong diem 6 mon (neu da nhap du) - tien ich de hien thi, khong luu vao DB
    @Transient
    public BigDecimal getTotalSubjectScore() {
        if (mathScore == null || literatureScore == null || englishScore == null || subjectGroup == null) {
            return null;
        }
        BigDecimal a, b, c;
        if (subjectGroup == SubjectGroup.NATURAL) {
            if (physicsScore == null || chemistryScore == null || biologyScore == null) return null;
            a = physicsScore; b = chemistryScore; c = biologyScore;
        } else {
            if (historyScore == null || geographyScore == null || civicScore == null) return null;
            a = historyScore; b = geographyScore; c = civicScore;
        }
        return mathScore.add(literatureScore).add(englishScore).add(a).add(b).add(c);
    }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    public List<ApplicationStatusHistory> getStatusHistories() { return statusHistories; }
    public void setStatusHistories(List<ApplicationStatusHistory> statusHistories) { this.statusHistories = statusHistories; }
}
