package com.university.admission.service.impl;

import com.university.admission.entity.Major;
import com.university.admission.repository.MajorRepository;
import com.university.admission.service.MajorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MajorServiceImpl implements MajorService {

    private final MajorRepository majorRepository;

    public MajorServiceImpl(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    @Override
    public List<Major> findAll() {
        return majorRepository.findAll();
    }

    @Override
    public Major findById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nganh voi id: " + id));
    }

    @Override
    public Major save(Major major) {
        return majorRepository.save(major);
    }

    @Override
    public void delete(Long id) {
        majorRepository.deleteById(id);
    }
}
