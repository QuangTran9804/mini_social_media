package com.example.social.dto.request.post;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreatePostDTO {

    @Size(max = 5000, message = "Content is too long")
    private String content;

    @Size(max = 1024, message = "Image URL is too long")
    private String imageUrl;
}


