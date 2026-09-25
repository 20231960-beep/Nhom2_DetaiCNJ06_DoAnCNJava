package com.university.admission.enums;

/**
 * Vai tro cua tai khoan trong he thong.
 * Khop voi cot `role` ENUM('ADMIN','STAFF','CANDIDATE') trong bang users.
 */
public enum Role {
    ADMIN("Quản trị viên"),
    STAFF("Nhân viên tuyển sinh"),
    CANDIDATE("Thí sinh");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
