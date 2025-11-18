package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Post;
import com.example.social.domain.PostLike;
import com.example.social.domain.Reaction;
import com.example.social.domain.User;
import com.example.social.dto.request.post.ReqCreatePostDTO;
import com.example.social.dto.response.post.PostAuthorDTO;
import com.example.social.dto.response.post.PostResponseDTO;
import com.example.social.repository.PostLikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> getFeedForUser(Long viewerId) throws ResourceNotFoundException {
        userRepository.findById(viewerId).orElseThrow(() -> new ResourceNotFoundException("Viewer not found"));

        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(post -> toDto(post, viewerId))
                .toList();
    }

    @Transactional
    public PostResponseDTO createPost(Long userId, ReqCreatePostDTO request) throws ResourceNotFoundException {
        if ((request.getContent() == null || request.getContent().isBlank())
                && (request.getImageUrl() == null || request.getImageUrl().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post must have content or image");
        }
        User author = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .user(author)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        Post saved = postRepository.save(post);
        return toDto(saved, userId);
    }

    private PostResponseDTO toDto(Post post, Long viewerId) {
        List<PostLike> likes = postLikeRepository.findByPost(post);

        Map<Reaction, Long> reactionCounts = new EnumMap<>(Reaction.class);
        for (Reaction reaction : Reaction.values()) {
            reactionCounts.put(reaction, 0L);
        }

        Reaction viewerReaction = null;
        for (PostLike like : likes) {
            reactionCounts.merge(like.getReaction(), 1L, Long::sum);
            if (like.getUser() != null && like.getUser().getId().equals(viewerId)) {
                viewerReaction = like.getReaction();
            }
        }

        Map<String, Long> reactionSummary = new LinkedHashMap<>();
        for (Reaction reaction : Reaction.values()) {
            reactionSummary.put(reaction.name(), reactionCounts.getOrDefault(reaction, 0L));
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .author(PostAuthorDTO.builder()
                        .id(post.getUser().getId())
                        .username(post.getUser().getUsername())
                        .avatarUrl(post.getUser().getAvatarUrl())
                        .build())
                .reactions(reactionSummary)
                .viewerReaction(viewerReaction != null ? viewerReaction.name() : null)
                .totalLikes(likes.size())
                .build();
    }
}


