package com.example.social.controller;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.post.ReqCreatePostDTO;
import com.example.social.dto.response.post.PostResponseDTO;
import com.example.social.service.CurrentUserService;
import com.example.social.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostController {

    private final PostService postService;
    private final CurrentUserService currentUserService;

    public PostController(PostService postService,
                          CurrentUserService currentUserService) {
        this.postService = postService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> feed() throws ResourceNotFoundException {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(postService.getFeedForUser(userId));
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> create(@Validated @RequestBody ReqCreatePostDTO request)
            throws ResourceNotFoundException {
        Long userId = currentUserService.getCurrentUserId();
        PostResponseDTO created = postService.createPost(userId, request);
        return ResponseEntity.ok(created);
    }
}


