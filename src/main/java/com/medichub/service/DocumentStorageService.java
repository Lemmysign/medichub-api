package com.medichub.service;

/** Stores non-image documents (study PDFs/Word) and returns a delivery URL — Cloudinary (raw)-backed. */
public interface DocumentStorageService {

    /** Upload (or overwrite) a raw file at {@code publicId} (include the extension) and return its URL. */
    String uploadDocument(String publicId, byte[] content);

    /** Best-effort delete; never throws. */
    void deleteDocument(String publicId);
}
