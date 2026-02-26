package com.bokbok.meow.modules.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.bokbok.meow.modules.media.dto.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    // ── Allowed types ────────────────────────────────────────────

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "image/webp", "image/heic"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/quicktime", "video/x-msvideo",
            "video/webm", "video/3gpp"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp4", "audio/ogg",
            "audio/wav", "audio/aac", "audio/webm",
            "audio/amr", "audio/3gpp"
    );

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "text/plain"
    );

    // Max sizes
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;   // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;  // 100 MB
    private static final long MAX_AUDIO_SIZE = 20 * 1024 * 1024;   // 20 MB
    private static final long MAX_FILE_SIZE  = 50 * 1024 * 1024;   // 50 MB

    // ── Avatar Upload (called from UserService) ──────────────────

    public String uploadAvatar(MultipartFile file, String userId) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "chatapp/avatars",
                            "public_id",     "avatar_" + userId,
                            "overwrite",     true,
                            "resource_type", "image",
                            "transformation","w_300,h_300,c_fill,g_face"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Avatar upload failed: " + e.getMessage());
        }
    }

    // ── Image Upload ─────────────────────────────────────────────

    public MediaUploadResponse uploadImage(MultipartFile file, String userId) {
        validateFile(file, ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "chatapp/images/" + userId,
                            "public_id",     UUID.randomUUID().toString(),
                            "resource_type", "image",
                            "quality",       "auto",
                            "fetch_format",  "auto"
                    )
            );
            return MediaUploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .mediaType("IMAGE")
                    .format((String) result.get("format"))
                    .fileSizeBytes(toLong(result.get("bytes")))
                    .width(toInt(result.get("width")))
                    .height(toInt(result.get("height")))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    // ── Video Upload ─────────────────────────────────────────────

    public MediaUploadResponse uploadVideo(MultipartFile file, String userId) {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "chatapp/videos/" + userId,
                            "public_id",     UUID.randomUUID().toString(),
                            "resource_type", "video",
                            "quality",       "auto"
                    )
            );

            // Auto-generate thumbnail from first frame
            String thumbnailUrl = cloudinary.url()
                    .resourceType("video")
                    .format("jpg")
                    .transformation(
                            new com.cloudinary.Transformation()
                                    .width(400).height(300).crop("fill")
                    )
                    .generate((String) result.get("public_id"));

            return MediaUploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .mediaType("VIDEO")
                    .format((String) result.get("format"))
                    .fileSizeBytes(toLong(result.get("bytes")))
                    .width(toInt(result.get("width")))
                    .height(toInt(result.get("height")))
                    .duration(toDouble(result.get("duration")))
                    .thumbnailUrl(thumbnailUrl)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Video upload failed: " + e.getMessage());
        }
    }

    // ── Audio / Voice Note Upload ────────────────────────────────

    public MediaUploadResponse uploadAudio(MultipartFile file, String userId) {
        validateFile(file, ALLOWED_AUDIO_TYPES, MAX_AUDIO_SIZE);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "chatapp/audio/" + userId,
                            "public_id",     UUID.randomUUID().toString(),
                            "resource_type", "video"  // Cloudinary uses "video" for audio
                    )
            );
            return MediaUploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .mediaType("AUDIO")
                    .format((String) result.get("format"))
                    .fileSizeBytes(toLong(result.get("bytes")))
                    .duration(toDouble(result.get("duration")))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Audio upload failed: " + e.getMessage());
        }
    }

    // ── File Upload (PDF, DOC, ZIP etc) ─────────────────────────

    public MediaUploadResponse uploadFile(MultipartFile file, String userId) {
        validateFile(file, ALLOWED_FILE_TYPES, MAX_FILE_SIZE);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",        "chatapp/files/" + userId,
                            "public_id",     UUID.randomUUID().toString(),
                            "resource_type", "raw",   // raw = any file type
                            "use_filename",  true
                    )
            );
            return MediaUploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .mediaType("FILE")
                    .format((String) result.get("format"))
                    .fileSizeBytes(toLong(result.get("bytes")))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    // ── Delete from Cloudinary ───────────────────────────────────

    public void deleteMedia(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
        } catch (IOException e) {
            log.error("Failed to delete media: {}", e.getMessage());
        }
    }

    // ── Validation ───────────────────────────────────────────────

    private void validateFile(MultipartFile file,
                              List<String> allowedTypes,
                              long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException(
                    "File type not allowed: " + file.getContentType()
            );
        }
        if (file.getSize() > maxSize) {
            throw new RuntimeException(
                    "File too large. Max size: " + (maxSize / 1024 / 1024) + "MB"
            );
        }
    }

    // ── Type Conversion Helpers ──────────────────────────────────

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        return ((Number) value).intValue();
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        return ((Number) value).doubleValue();
    }
}
