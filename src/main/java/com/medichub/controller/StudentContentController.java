package com.medichub.controller;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.MaterialResponse;
import com.medichub.dto.response.VideoPlaybackResponse;
import com.medichub.service.MaterialService;
import com.medichub.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Student content consumption — all subscription-gated in the service layer. */
@RestController
@RequestMapping("/api/student/courses")
public class StudentContentController {

    private final VideoService videoService;
    private final MaterialService materialService;

    public StudentContentController(VideoService videoService, MaterialService materialService) {
        this.videoService = videoService;
        this.materialService = materialService;
    }

    @GetMapping("/{courseId}/topics/{topicId}/playback")
    public VideoPlaybackResponse playback(@PathVariable Long courseId, @PathVariable Long topicId) {
        return videoService.getPlayback(courseId, topicId);
    }

    @GetMapping("/{courseId}/materials")
    public List<MaterialResponse> materials(@PathVariable Long courseId) {
        return materialService.listForStudent(courseId);
    }

    @GetMapping("/{courseId}/materials/{materialId}/download")
    public DownloadUrlResponse download(@PathVariable Long courseId, @PathVariable Long materialId) {
        return materialService.getDownloadUrl(courseId, materialId);
    }
}
