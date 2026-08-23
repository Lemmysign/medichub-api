package com.medichub.service;

import java.time.Duration;

/** Abstraction over object storage (Cloudflare R2). */
public interface StorageService {

    /** Store bytes under {@code key}; returns the stored key. */
    String upload(String key, byte[] content, String contentType);

    void delete(String key);

    /** Public URL for openly-served objects (e.g. thumbnails). */
    String publicUrl(String key);

    /** Short-lived signed GET URL for gated objects (e.g. course materials). */
    String presignedGetUrl(String key, Duration ttl);
}
