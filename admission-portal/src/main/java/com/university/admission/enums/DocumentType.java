package com.university.admission.enums;

public enum DocumentType {
    TRANSCRIPT("Học bạ / Bảng điểm"),
    ID_CARD("CCCD / CMND"),
    BIRTH_CERT("Giấy khai sinh"),
    PRIORITY_CERT("Giấy chứng nhận ưu tiên / khu vực"),
    OTHER("Khác");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
