package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.secret-key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    // =========================================================
    // 1. UPLOAD AVATAR / IMAGE
    // Dùng cho: /api/users/{id}/avatar
    // Chỉ cho upload image/*
    // =========================================================
    public String uploadFile(MultipartFile file, String folder) {
        try {
            validateImage(file);

            return uploadToSupabase(file, folder, "avatar");

        } catch (Exception e) {
            throw new RuntimeException("Could not upload image to Supabase: " + e.getMessage());
        }
    }

    // =========================================================
    // 2. UPLOAD MODULE FILE
    // Dùng cho file bài học: pdf, docx, pptx, zip, txt, image...
    // =========================================================
    public String uploadModuleFile(MultipartFile file, String folder) {
        try {
            validateGeneralFile(file);

            return uploadToSupabase(file, folder, "module_file");

        } catch (Exception e) {
            throw new RuntimeException("Could not upload module file to Supabase: " + e.getMessage());
        }
    }

    // =========================================================
    // 3. HÀM UPLOAD CHUNG LÊN SUPABASE
    // =========================================================
    private String uploadToSupabase(MultipartFile file, String folder, String prefix) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = prefix + "_" + System.currentTimeMillis() + extension;
        String filePath = folder + "/" + fileName;

        String uploadUrl = supabaseUrl
                + "/storage/v1/object/"
                + supabaseBucket
                + "/"
                + filePath;

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.setBearerAuth(supabaseKey);
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> requestEntity =
                new HttpEntity<>(file.getBytes(), headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }

        return supabaseUrl
                + "/storage/v1/object/public/"
                + supabaseBucket
                + "/"
                + filePath;
    }

    // =========================================================
    // 4. VALIDATE AVATAR
    // =========================================================
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        long maxSize = 5 * 1024 * 1024; // 5MB

        if (file.getSize() > maxSize) {
            throw new RuntimeException("Image size must be less than 5MB");
        }
    }

    // =========================================================
    // 5. VALIDATE FILE MODULE
    // =========================================================
    private void validateGeneralFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        long maxSize = 20 * 1024 * 1024; // 20MB

        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size must be less than 20MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            throw new RuntimeException("Invalid file type");
        }

        boolean allowed =
                contentType.equals("application/pdf")
                        || contentType.equals("application/msword")
                        || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        || contentType.equals("application/vnd.ms-powerpoint")
                        || contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                        || contentType.equals("application/vnd.ms-excel")
                        || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        || contentType.equals("application/zip")
                        || contentType.equals("text/plain")
                        || contentType.startsWith("image/");

        if (!allowed) {
            throw new RuntimeException("File type is not allowed: " + contentType);
        }
    }
}