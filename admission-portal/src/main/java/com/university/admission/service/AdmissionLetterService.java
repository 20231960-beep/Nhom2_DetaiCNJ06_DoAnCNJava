package com.university.admission.service;

import com.university.admission.entity.Application;

/**
 * Sinh file PDF "Giay bao trung tuyen" cho 1 ho so da TRUNG TUYEN (ADMITTED/ENROLLED),
 * de thi sinh tai ve / in ra mang di lam thu tuc nhap hoc.
 */
public interface AdmissionLetterService {
    byte[] generate(Application application);
}
