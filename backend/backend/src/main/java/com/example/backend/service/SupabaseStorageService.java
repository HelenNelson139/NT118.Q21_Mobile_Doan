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
    @Value("${supabase.key}")
    private String supabaseKey;
    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public String uploadFile(MultipartFile file, String folder) {
        try {
            validateImage(file);

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = "avatar_" + System.currentTimeMillis() + extension;
            String filePath = folder + "/" + fileName;

            String uploadUrl = supabaseUrl
                    + "/storage/v1/object/"
                    + supabaseBucket
                    + "/"
                    + filePath;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            headers.set("x-upsert", "true");

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Upload image failed");
            }

            return supabaseUrl
                    + "/storage/v1/object/public/"
                    + supabaseBucket
                    + "/"
                    + filePath;

        } catch (Exception e) {
            throw new RuntimeException("Could not upload file to Supabase: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        long maxSize = 5 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new RuntimeException("Image size must be less than 5MB");
        }
    }
}
