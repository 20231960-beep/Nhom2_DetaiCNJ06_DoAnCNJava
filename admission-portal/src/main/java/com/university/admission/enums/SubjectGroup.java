package com.university.admission.enums;

/**
 * Khoi thi ma thi sinh chon cho 3 mon khoi (ngoai 3 mon chinh Toan/Van/Anh).
 * NATURAL: Vat ly, Hoa hoc, Sinh hoc.
 * SOCIAL:  Lich su, Dia li, Giao duc cong dan.
 */
public enum SubjectGroup {
    NATURAL("Khoa học Tự nhiên (Vật lý, Hóa học, Sinh học)"),
    SOCIAL("Khoa học Xã hội (Lịch sử, Địa lí, Giáo dục công dân)");

    private final String label;

    SubjectGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
