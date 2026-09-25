package com.university.admission.repository;

import com.university.admission.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {
    boolean existsByCode(String code);
}
