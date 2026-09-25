package com.university.admission.service.impl;

import com.university.admission.entity.Application;
import com.university.admission.entity.Candidate;
import com.university.admission.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] HEADERS = {
            "STT", "Họ và tên", "Ngày sinh", "Giới tính", "Số CCCD/CMND", "Số điện thoại",
            "Ngành trúng tuyển", "Đợt tuyển sinh", "Khối trúng tuyển", "Điểm trúng tuyển", "Trạng thái"
    };

    @Override
    public byte[] exportAdmittedList(List<Application> applications) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách trúng tuyển");

            CellStyle titleStyle = titleStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dataStyle = dataStyle(workbook);

            int rowIdx = 0;

            // Dong tieu de lon, gop o tren cung
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH THÍ SINH TRÚNG TUYỂN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            rowIdx++; // dong trong

            // Dong tieu de cot
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Du lieu tung dong
            int stt = 1;
            for (Application app : applications) {
                Candidate candidate = app.getCandidate();
                Row row = sheet.createRow(rowIdx++);

                setCell(row, 0, stt++, dataStyle);
                setCell(row, 1, candidate != null && candidate.getUser() != null ? candidate.getUser().getFullName() : "", dataStyle);
                setCell(row, 2, candidate != null && candidate.getDob() != null ? candidate.getDob().format(DATE_FMT) : "", dataStyle);
                setCell(row, 3, candidate != null && candidate.getGender() != null ? candidate.getGender().getLabel() : "", dataStyle);
                setCell(row, 4, candidate != null ? candidate.getIdCardNumber() : "", dataStyle);
                setCell(row, 5, candidate != null ? candidate.getPhone() : "", dataStyle);
                setCell(row, 6, app.getMajor() != null ? app.getMajor().getName() : "", dataStyle);
                setCell(row, 7, app.getAdmissionSession() != null ? app.getAdmissionSession().getName() : "", dataStyle);
                setCell(row, 8, app.getAdmissionCombination() != null ? app.getAdmissionCombination().name() : "", dataStyle);
                setCell(row, 9, app.getScore() != null ? app.getScore().doubleValue() : null, dataStyle);
                setCell(row, 10, app.getStatus() != null ? app.getStatus().getLabel() : "", dataStyle);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // No trong 1 chut de khong bi sat chu, autoSizeColumn tinh theo ky tu nen doi voi tieng Viet co dau
                // co the hoi chat, cong them 1 khoang du
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 800);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Loi khi xuat file Excel danh sach trung tuyen", e);
        }
    }

    private void setCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    private CellStyle titleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle dataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
