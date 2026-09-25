package com.university.admission.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "session_major", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_id", "major_id"})
})
public class SessionMajor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AdmissionSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(nullable = false)
    private Integer quota = 0;

    @Column(name = "benchmark_score", precision = 4, scale = 2)
    private BigDecimal benchmarkScore;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AdmissionSession getSession() { return session; }
    public void setSession(AdmissionSession session) { this.session = session; }

    public Major getMajor() { return major; }
    public void setMajor(Major major) { this.major = major; }

    public Integer getQuota() { return quota; }
    public void setQuota(Integer quota) { this.quota = quota; }

    public BigDecimal getBenchmarkScore() { return benchmarkScore; }
    public void setBenchmarkScore(BigDecimal benchmarkScore) { this.benchmarkScore = benchmarkScore; }
}
