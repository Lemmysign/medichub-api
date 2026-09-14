package com.medichub.controller;

import com.medichub.dto.response.UploadedImageResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.service.ImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/** Creator-side image uploads (question pictures) → Cloudinary; returns the delivery URL. */
@RestController
@RequestMapping("/api/images")
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;

    public ImageUploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadedImageResponse uploadQuestionImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded image");
        }
        String publicId = "medichub/question-images/" + UUID.randomUUID();
        return new UploadedImageResponse(imageStorageService.uploadImage(publicId, bytes));
    }
}
