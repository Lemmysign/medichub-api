package com.medichub.controller;

import com.medichub.dto.response.CourseResponse;
import com.medichub.dto.response.MaterialResponse;
import com.medichub.dto.response.VideoUploadCredentialResponse;
import com.medichub.service.CourseService;
import com.medichub.service.MaterialService;
import com.medichub.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Instructor media/content management for a course (thumbnail, materials, video). */
@RestController
@RequestMapping("/api/instructor/courses")
public class InstructorContentController {

    private final CourseService courseService;
    private final MaterialService materialService;
    private final VideoService videoService;

    public InstructorContentController(CourseService courseService,
                                       MaterialService materialService,
                                       VideoService videoService) {
        this.courseService = courseService;
        this.materialService = materialService;
        this.videoService = videoService;
    }

    @PostMapping(value = "/{courseId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResponse uploadThumbnail(@PathVariable Long courseId,
                                          @RequestParam("file") MultipartFile file) {
        return courseService.uploadThumbnail(courseId, file);
    }

    @GetMapping("/{courseId}/materials")
    public List<MaterialResponse> listMaterials(@PathVariable Long courseId) {
        return materialService.listForInstructor(courseId);
    }

    @PostMapping(value = "/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> uploadMaterial(@PathVariable Long courseId,
                                                           @RequestParam(value = "topicId", required = false) Long topicId,
                                                           @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.upload(courseId, topicId, file));
    }

    @DeleteMapping("/{courseId}/materials/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long courseId, @PathVariable Long materialId) {
        materialService.delete(courseId, materialId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/topics/{topicId}/video")
    public VideoUploadCredentialResponse createVideo(@PathVariable Long courseId, @PathVariable Long topicId) {
        return videoService.createVideoForTopic(courseId, topicId);
    }

    @DeleteMapping("/{courseId}/topics/{topicId}/video")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long courseId, @PathVariable Long topicId) {
        videoService.deleteVideoForTopic(courseId, topicId);
        return ResponseEntity.noContent().build();
    }
}
