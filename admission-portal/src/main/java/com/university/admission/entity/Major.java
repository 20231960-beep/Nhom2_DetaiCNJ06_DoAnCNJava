package com.university.admission.entity;

import com.university.admission.enums.AdmissionCombination;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "majors")
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String faculty;

    // Cac khoi to hop xet tuyen ma nganh nay chap nhan (VD: A00, A01, D01).
    // Truoc day luu chung 1 chuoi "A00, A01, D01" trong 1 cot (vi pham 1NF - thuoc tinh da tri).
    // Nay tach thanh bang rieng major_combination(major_id, combination_code), moi khoi 1 dong.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "major_combination", joinColumns = @JoinColumn(name = "major_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "combination_code", length = 10)
    private Set<AdmissionCombination> combinations = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionMajor> sessionMajors = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public Set<AdmissionCombination> getCombinations() { return combinations; }
    public void setCombinations(Set<AdmissionCombination> combinations) { this.combinations = combinations; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<SessionMajor> getSessionMajors() { return sessionMajors; }
    public void setSessionMajors(List<SessionMajor> sessionMajors) { this.sessionMajors = sessionMajors; }
}

