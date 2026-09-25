package com.university.admission.service;

import com.university.admission.entity.Application;

import java.util.List;

/**
 * Xuat danh sach ho so ra file Excel (.xlsx), dung cho staff/admin tai ve danh sach trung tuyen.
 */
public interface ExcelExportService {
    byte[] exportAdmittedList(List<Application> applications);
}
