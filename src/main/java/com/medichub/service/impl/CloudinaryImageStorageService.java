package com.medichub.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.medichub.exception.BadRequestException;
import com.medichub.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(String publicId, byte[] content) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image"));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new BadRequestException("Image upload did not return a URL");
            }
            return url.toString();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cloudinary image upload failed for publicId={}: {}", publicId, e.getMessage());
            throw new BadRequestException("Could not upload image. Please try again.");
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception e) {
            log.warn("Cloudinary delete failed for publicId={}: {}", publicId, e.getMessage());
        }
    }
}
