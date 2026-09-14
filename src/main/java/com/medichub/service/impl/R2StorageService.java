package com.medichub.service.impl;

import com.medichub.config.R2Properties;
import com.medichub.exception.BadRequestException;
import com.medichub.service.StorageService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
public class R2StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final R2Properties props;

    public R2StorageService(S3Client r2S3Client, S3Presigner r2S3Presigner, R2Properties props) {
        this.s3Client = r2S3Client;
        this.s3Presigner = r2S3Presigner;
        this.props = props;
    }

    private void ensureConfigured() {
        if (props.accountId() == null || props.accountId().isBlank()
                || props.accessKey() == null || props.accessKey().isBlank()) {
            throw new BadRequestException("File storage (Cloudflare R2) is not configured on the server");
        }
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        ensureConfigured();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(content));
        return key;
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build());
    }

    @Override
    public String publicUrl(String key) {
        String base = props.publicBaseUrl();
        if (base == null || base.isBlank()) {
            return key;
        }
        return base.endsWith("/") ? base + key : base + "/" + key;
    }

    @Override
    public String presignedGetUrl(String key, Duration ttl) {
        ensureConfigured();
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
