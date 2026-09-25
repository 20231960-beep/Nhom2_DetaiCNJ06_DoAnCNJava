package com.university.admission.controller;

import com.university.admission.entity.Document;
import com.university.admission.enums.Role;
import com.university.admission.repository.DocumentRepository;
import com.university.admission.security.CustomUserDetails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLConnection;

/**
 * Phuc vu xem/tai file minh chung da upload (hoc ba, CCCD...).
 * Khong dat trong package admin/staff/candidate rieng vi ca 3 vai tro deu can dung,
 * chi khac nhau o quyen truy cap duoc kiem tra thu cong ben trong.
 */
@RestController
public class DocumentController {

    private final DocumentRepository documentRepository;

    public DocumentController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping("/documents/{id}/view")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay minh chung voi id: " + id));

        // Kiem tra quyen: ADMIN/STAFF xem duoc moi minh chung.
        // CANDIDATE chi duoc xem minh chung thuoc chinh ho so cua minh (tranh xem trom file nguoi khac).
        Role role = userDetails.getUser().getRole();
        if (role == Role.CANDIDATE) {
            Long ownerId = doc.getApplication().getCandidate().getUser().getId();
            if (!ownerId.equals(userDetails.getUser().getId())) {
                throw new AccessDeniedException("Ban khong co quyen xem file nay");
            }
        }

        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("File khong con ton tai tren server: " + doc.getFileName());
        }

        Resource resource = new FileSystemResource(file);

        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // "inline" de trinh duyet HIEN THI truc tiep (anh/PDF) thay vi bat buoc tai xuong
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
}
