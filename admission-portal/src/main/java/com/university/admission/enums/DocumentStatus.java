package com.university.admission.enums;

public enum DocumentStatus {
    PENDING("Đang chờ duyệt"),
    VALID("Hợp lệ"),
    INVALID("Không hợp lệ");

    private final String label;

    DocumentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
