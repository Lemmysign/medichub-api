package com.medichub.service;

/** Public image storage (course thumbnails/pictures) — Cloudinary-backed. */
public interface ImageStorageService {

    /**
     * Upload (or overwrite) an image at a stable {@code publicId} and return its public
     * delivery URL. Re-uploading the same publicId replaces the previous image.
     */
    String uploadImage(String publicId, byte[] content);

    /** Best-effort delete; never throws. */
    void deleteImage(String publicId);
}
