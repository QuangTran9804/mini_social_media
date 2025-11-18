package com.example.social.controller;

import com.example.social.domain.Reaction;
import com.example.social.repository.UserRepository;
import com.example.social.security.SecurityUtils;
import com.example.social.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@Validated
public class LikeController {

    private final LikeService likeService;
    private final UserRepository userRepository;

    public LikeController(LikeService likeService, UserRepository userRepository) {
        this.likeService = likeService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> toggle(@PathVariable Long postId, @RequestBody Map<String, String> body) throws com.example.social.controller.error.ResourceNotFoundException {
        String reactionStr = body.getOrDefault("reaction", "LIKE");
        Reaction reaction = Reaction.valueOf(reactionStr.toUpperCase());
        String email = SecurityUtils.getCurrentUserLogin().orElseThrow();
        Long userId = userRepository.findByEmail(email).getId();
        var result = likeService.toggleReaction(postId, userId, reaction);
        return ResponseEntity.ok(Map.of(
                "liked", result.liked(),
                "reaction", result.reaction() != null ? result.reaction().name() : null,
                "totalLikes", result.totalLikes()
        ));
    }
}


