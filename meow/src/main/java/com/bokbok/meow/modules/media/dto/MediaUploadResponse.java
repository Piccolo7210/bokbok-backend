package com.bokbok.meow.modules.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadResponse {
    private String url;           // Cloudinary secure URL
    private String publicId;      // Cloudinary public ID (for deletion)
    private String mediaType;     // IMAGE, VIDEO, AUDIO, FILE
    private String format;        // jpg, mp4, mp3, pdf etc
    private Long fileSizeBytes;
    private Integer width;        // images/videos only
    private Integer height;       // images/videos only
    private Double duration;      // audio/video only (seconds)
    private String thumbnailUrl;  // video thumbnail
}