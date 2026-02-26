package com.bokbok.meow.modules.media.controller;

import com.bokbok.meow.modules.media.dto.MediaUploadResponse;
import com.bokbok.meow.modules.media.service.MediaService;
import com.bokbok.meow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // Upload image
    @PostMapping(value = "/upload/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(mediaService.uploadImage(file, userId));
    }

    // Upload video
    @PostMapping(value = "/upload/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(mediaService.uploadVideo(file, userId));
    }

    // Upload any file (pdf, doc, zip etc)
    @PostMapping(value = "/upload/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(mediaService.uploadFile(file, userId));
    }

    // Upload voice note
    @PostMapping(value = "/upload/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadAudio(
            @RequestParam("file") MultipartFile file) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(mediaService.uploadAudio(file, userId));
    }
}
