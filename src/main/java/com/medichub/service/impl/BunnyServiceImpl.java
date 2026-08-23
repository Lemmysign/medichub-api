package com.medichub.service.impl;

import com.medichub.config.BunnyProperties;
import com.medichub.exception.BadRequestException;
import com.medichub.service.BunnyService;
import com.medichub.service.BunnyVideoUpload;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class BunnyServiceImpl implements BunnyService {

    private static final String TUS_ENDPOINT = "https://video.bunnycdn.com/tusupload";
    private static final Duration UPLOAD_WINDOW = Duration.ofHours(2);

    private final RestClient bunnyRestClient;
    private final BunnyProperties props;

    public BunnyServiceImpl(RestClient bunnyRestClient, BunnyProperties props) {
        this.bunnyRestClient = bunnyRestClient;
        this.props = props;
    }

    @Override
    public BunnyVideoUpload createVideo(String title) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = bunnyRestClient.post()
                .uri("/library/{libraryId}/videos", props.libraryId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("title", title))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("guid") == null) {
            throw new BadRequestException("Bunny did not return a video id");
        }
        String guid = response.get("guid").toString();

        long expires = Instant.now().plus(UPLOAD_WINDOW).getEpochSecond();
        // Bunny presigned upload signature: sha256hex(libraryId + apiKey + expiration + videoId)
        String signature = sha256Hex(props.libraryId() + props.apiKey() + expires + guid);

        return new BunnyVideoUpload(guid, props.libraryId(), TUS_ENDPOINT, expires, signature);
    }

    @Override
    public void deleteVideo(String guid) {
        bunnyRestClient.delete()
                .uri("/library/{libraryId}/videos/{guid}", props.libraryId(), guid)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public String signedPlaybackUrl(String guid, Duration ttl) {
        long expires = Instant.now().plus(ttl).getEpochSecond();
        String path = "/" + guid + "/playlist.m3u8";
        // Bunny CDN token authentication: base64url(sha256_raw(tokenKey + path + expires)).
        String token = base64Url(sha256Raw(props.tokenAuthKey() + path + expires));
        return "https://" + props.cdnHostname() + path + "?token=" + token + "&expires=" + expires;
    }

    @Override
    public String signedDownloadUrl(String guid, Duration ttl) {
        long expires = Instant.now().plus(ttl).getEpochSecond();
        String path = "/" + guid + "/play_720p.mp4";
        String token = base64Url(sha256Raw(props.tokenAuthKey() + path + expires));
        return "https://" + props.cdnHostname() + path + "?token=" + token + "&expires=" + expires;
    }

    private static byte[] sha256Raw(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String sha256Hex(String input) {
        return HexFormat.of().formatHex(sha256Raw(input));
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
