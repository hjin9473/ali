package com.finalProject.ali.image.service;

import net.coobird.thumbnailator.Thumbnails; // [추가] 썸네일 라이브러리
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${file.upload.path}")
    private String uploadPath;

    // 경로 끝에 '/'가 없으면 붙여주는 안전장치 (리눅스 경로 에러 방지용)
    private String getUploadPath() {
        return uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
    }

    public String uploadImage(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) return null;

        // 1. 경로 생성
        String fullPath = getUploadPath() + folderName + "/";
        File folder = new File(fullPath);
        if (!folder.exists()) folder.mkdirs();

        // 2. UUID 파일명 생성
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String savedName = UUID.randomUUID().toString() + extension;

        File targetFile = new File(fullPath + savedName);

        try {
            // 메모리에서 이미지를 읽어 리사이징 후 저장
            Thumbnails.of(file.getInputStream())
                    .size(800, 800)        // 최대 크기 800x800
                    .outputQuality(0.8)    // 화질 80%
                    .toFile(targetFile);   // 저장

            // 성공 시 경로 반환
            return "/upload/" + folderName + "/" + savedName;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ [ImageService] 이미지 변환 저장 실패");
            return null;
        }
    }

    public void deleteActualFile(String webPath) {
        if (webPath == null || webPath.isEmpty()) return;

        // 웹 경로를 물리 경로로 변환
        String relativePath = webPath.replace("/upload/", "");
        File file = new File(getUploadPath() + relativePath);

        if (file.exists()) {
            file.delete();
        }
    }
}