package com.example.bootcampproject.service;

import com.example.bootcampproject.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/** 업로드된 이미지를 로컬 디스크(app.upload-dir)에 저장하고, 브라우저가 접근할 URL을 돌려준다. */
@Slf4j
@Service
public class FileStorageService {

    // 클라이언트가 보낸 파일명은 신뢰하지 않고, 검증된 content-type으로만 확장자를 결정한다.
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 생성할 수 없습니다: " + uploadRoot, e);
        }
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    // 질문 첨부 이미지 저장 후 "/uploads/questions/xxx.jpg" 형태의 URL 경로 반환
    public String storeQuestionImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("이미지 파일이 비어 있습니다.");
        }
        String ext = EXTENSION_BY_CONTENT_TYPE.get(file.getContentType());
        if (ext == null) {
            throw new BusinessException("지원하지 않는 이미지 형식입니다. (jpg, png, gif, webp만 가능)");
        }

        String filename = UUID.randomUUID() + ext;
        Path targetDir = uploadRoot.resolve("questions");
        Path target = targetDir.resolve(filename);
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }
        return "/uploads/questions/" + filename;
    }

    // 이미지 파일 삭제 - 실패해도 DB 정합성에는 영향 없으므로 로그만 남기고 무시
    public void delete(String imageUrlPath) {
        if (imageUrlPath == null || !imageUrlPath.startsWith("/uploads/")) {
            return;
        }
        Path target = uploadRoot.resolve(imageUrlPath.substring("/uploads/".length()));
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("이미지 파일 삭제 실패: {}", target, e);
        }
    }
}
