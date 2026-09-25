package com.university.admission.service;

import com.university.admission.entity.Major;

import java.util.List;

public interface MajorService {
    List<Major> findAll();
    Major findById(Long id);
    Major save(Major major);
    void delete(Long id);
}
