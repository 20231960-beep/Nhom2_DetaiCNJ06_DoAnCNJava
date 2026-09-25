package com.university.admission.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.university.admission.entity.Application;
import com.university.admission.entity.Candidate;
import com.university.admission.service.AdmissionLetterService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AdmissionLetterServiceImpl implements AdmissionLetterService {

    // Doi ten truong/logo o day cho dung voi de tai cua ban
    private static final String UNIVERSITY_NAME = "TRƯỜNG ĐẠI HỌC ................................";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // File font Unicode dung de ve tieng Viet co dau trong PDF (iText mac dinh KHONG ho tro dau tieng Viet).
    // Ban tu them 1 file .ttf bat ky ho tro tieng Viet vao duong dan nay (xem README/ghi chu di kem).
    private static final String FONT_RESOURCE_PATH = "fonts/vietnamese-font.ttf";

    private final BaseFont vietnameseBaseFont;

    public AdmissionLetterServiceImpl() {
        this.vietnameseBaseFont = loadVietnameseFont();
    }

    // Doc file font tu classpath (src/main/resources/fonts/...) va nap vao iText voi encoding IDENTITY_H
    // (bat buoc de xuat dung dau tieng Viet), embedded=true de nguoi xem PDF khong can cai font nay.
    private BaseFont loadVietnameseFont() {
        try {
            ClassPathResource resource = new ClassPathResource(FONT_RESOURCE_PATH);
            byte[] fontBytes = StreamUtils.copyToByteArray(resource.getInputStream());
            return BaseFont.createFont(FONT_RESOURCE_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException(
                "Khong tim thay file font tieng Viet tai 'src/main/resources/" + FONT_RESOURCE_PATH + "'. " +
                "Hay them 1 file .ttf ho tro Unicode tieng Viet (vi du copy times.ttf/arial.ttf tu " +
                "C:\\Windows\\Fonts va doi ten thanh vietnamese-font.ttf) vao dung duong dan tren roi chay lai.", e);
        }
    }

    @Override
    public byte[] generate(Application app) {
        Candidate candidate = app.getCandidate();

        Font titleFont = new Font(vietnameseBaseFont, 12, Font.BOLD);
        Font headerFont = new Font(vietnameseBaseFont, 13, Font.BOLD);
        Font bigTitleFont = new Font(vietnameseBaseFont, 18, Font.BOLD);
        Font labelFont = new Font(vietnameseBaseFont, 12, Font.BOLD);
        Font normalFont = new Font(vietnameseBaseFont, 12, Font.NORMAL);
        Font italicFont = new Font(vietnameseBaseFont, 11, Font.ITALIC);
        Font noteFont = new Font(vietnameseBaseFont, 10, Font.ITALIC);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 60, 60, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Quoc hieu - tieu ngu
            Paragraph quocHieu = new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", headerFont);
            quocHieu.setAlignment(Element.ALIGN_CENTER);
            document.add(quocHieu);

            Paragraph tieuNgu = new Paragraph("Độc lập - Tự do - Hạnh phúc", titleFont);
            tieuNgu.setAlignment(Element.ALIGN_CENTER);
            document.add(tieuNgu);

            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            line.setPercentage(25f);
            Paragraph lineParagraph = new Paragraph(new Chunk(line));
            lineParagraph.setAlignment(Element.ALIGN_CENTER);
            lineParagraph.setSpacingAfter(25f);
            document.add(lineParagraph);

            // Ten truong
            Paragraph university = new Paragraph(UNIVERSITY_NAME, headerFont);
            university.setAlignment(Element.ALIGN_CENTER);
            university.setSpacingAfter(20f);
            document.add(university);

            // Tieu de chinh
            Paragraph title = new Paragraph("GIẤY BÁO TRÚNG TUYỂN", bigTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6f);
            document.add(title);

            Paragraph subTitle = new Paragraph("Mã hồ sơ: " + app.getId(), italicFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(25f);
            document.add(subTitle);

            Paragraph intro = new Paragraph(UNIVERSITY_NAME.trim() + " trân trọng thông báo:", normalFont);
            intro.setSpacingAfter(15f);
            document.add(intro);

            document.add(infoLine("Họ và tên thí sinh:", nvl(candidate.getUser().getFullName()), labelFont, normalFont));
            document.add(infoLine("Ngày sinh:", formatDob(candidate.getDob()), labelFont, normalFont));
            document.add(infoLine("Số CCCD/CMND:", nvl(candidate.getIdCardNumber()), labelFont, normalFont));
            document.add(infoLine("Nơi thường trú:", nvl(candidate.getAddress()), labelFont, normalFont));
            document.add(new Paragraph(" ", normalFont));

            document.add(infoLine("Đã trúng tuyển vào ngành:",
                    nvl(app.getMajor().getName()) + " (Mã ngành: " + nvl(app.getMajor().getCode()) + ")",
                    labelFont, normalFont));
            document.add(infoLine("Đợt tuyển sinh:", nvl(app.getAdmissionSession().getName()), labelFont, normalFont));
            document.add(infoLine("Điểm xét tuyển:",
                    app.getScore() != null ? app.getScore().toPlainString() : "—", labelFont, normalFont));

            Paragraph note = new Paragraph(
                "Đề nghị thí sinh mang theo Giấy báo này cùng các giấy tờ liên quan (CCCD/CMND, học bạ, " +
                "giấy chứng nhận tốt nghiệp THPT hoặc tương đương...) đến làm thủ tục nhập học theo đúng " +
                "thời gian quy định của nhà trường.", normalFont);
            note.setSpacingBefore(20f);
            note.setSpacingAfter(35f);
            document.add(note);

            // Ngay cap + noi ky
            Paragraph datePlace = new Paragraph(
                "................., ngày " + LocalDate.now().format(DATE_FMT), italicFont);
            datePlace.setAlignment(Element.ALIGN_RIGHT);
            document.add(datePlace);

            Paragraph signer = new Paragraph("TL. HIỆU TRƯỞNG", labelFont);
            signer.setAlignment(Element.ALIGN_RIGHT);
            document.add(signer);

            Paragraph signerNote = new Paragraph("(Đã ký, đóng dấu)", noteFont);
            signerNote.setAlignment(Element.ALIGN_RIGHT);
            document.add(signerNote);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Loi khi xuat file PDF giay bao trung tuyen", e);
        }
    }

    private Paragraph infoLine(String label, String value, Font labelFont, Font normalFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value, normalFont));
        p.setSpacingAfter(8f);
        return p;
    }

    private String formatDob(LocalDate dob) {
        return dob != null ? dob.format(DATE_FMT) : "—";
    }

    private String nvl(String value) {
        return (value != null && !value.isBlank()) ? value : "—";
    }
}
