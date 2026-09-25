package com.university.admission.enums;

/**
 * Vong doi trang thai cua mot ho so dang ky xet tuyen (applications.status).
 *
 * Luong di chuyen trang thai hop le (state machine):
 * DRAFT -> SUBMITTED -> (NEED_SUPPLEMENT <-> SUBMITTED) -> APPROVED/REJECTED
 * APPROVED -> ADMITTED/NOT_ADMITTED
 * ADMITTED -> ENROLLED (STAFF xac nhan thi sinh da den truong lam thu tuc that: nop ban goc, dong hoc phi...)
 */
public enum ApplicationStatus {
    DRAFT("Nháp"),
    SUBMITTED("Đã nộp"),
    NEED_SUPPLEMENT("Cần bổ sung"),
    APPROVED("Hồ sơ hợp lệ"),
    REJECTED("Hồ sơ bị từ chối"),
    ADMITTED("Trúng tuyển"),
    NOT_ADMITTED("Không trúng tuyển"),
    ENROLLED("Đã nhập học chính thức");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
