package com.medichub.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.medichub.exception.BadRequestException;
import com.medichub.service.DocumentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Stores study documents (PDF/Word) on Cloudinary as {@code raw} resources. The public_id keeps the
 * original extension so the delivery URL serves the right content type (and our client can detect
 * PDF vs DOCX). Cloudinary serves permissive CORS, so browser fetch (mammoth) works without extra config.
 */
@Service
public class CloudinaryDocumentStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryDocumentStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryDocumentStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadDocument(String publicId, byte[] content) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "raw",
                    "overwrite", true,
                    "invalidate", true));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new BadRequestException("Document upload did not return a URL");
            }
            return url.toString();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Cloudinary document upload failed for publicId={}: {}", publicId, e.getMessage());
            throw new BadRequestException("Could not upload the document. Please try again.");
        }
    }

    @Override
    public void deleteDocument(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception e) {
            log.warn("Cloudinary document delete failed for publicId={}: {}", publicId, e.getMessage());
        }
    }
}
