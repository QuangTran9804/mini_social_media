package com.example.social.dto.response.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private PostAuthorDTO author;
    private Map<String, Long> reactions;
    private String viewerReaction;
    private long totalLikes;
}


