package com.university.admission.enums;

/**
 * Ma to hop xet tuyen (khoi thi truyen thong) ma staff chon khi nhap diem trung tuyen cho 1 ho so.
 * Chi liet ke cac to hop tao tu 6 mon ma thi sinh da tu khai (Toan, Van, Anh + 3 mon khoi).
 */
public enum AdmissionCombination {
    A00("A00 - Toán, Vật lý, Hóa học"),
    A01("A01 - Toán, Vật lý, Tiếng Anh"),
    B00("B00 - Toán, Hóa học, Sinh học"),
    C00("C00 - Ngữ văn, Lịch sử, Địa lí"),
    C19("C19 - Ngữ văn, Lịch sử, Giáo dục công dân"),
    C20("C20 - Ngữ văn, Địa lí, Giáo dục công dân"),
    D01("D01 - Ngữ văn, Toán, Tiếng Anh"),
    D14("D14 - Ngữ văn, Lịch sử, Tiếng Anh");

    private final String label;

    AdmissionCombination(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
