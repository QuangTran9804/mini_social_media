package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.*;
import com.example.social.repository.PostLikeRepository;
import com.example.social.repository.PostRepository;
import com.example.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;

    public LikeService(PostRepository postRepository,
                       UserRepository userRepository,
                       PostLikeRepository postLikeRepository,
                       NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ToggleLikeResult toggleReaction(Long postId, Long userId, Reaction reaction) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return postLikeRepository.findByPostAndUser(post, user)
                .map(existing -> {
                    if (existing.getReaction() == reaction) {
                        postLikeRepository.delete(existing);
                        notificationService.emitPostLikeUpdated(post.getId());
                        return new ToggleLikeResult(false, null, postLikeRepository.countByPost(post));
                    } else {
                        existing.setReaction(reaction);
                        postLikeRepository.save(existing);
                        if (!post.getUser().getId().equals(userId)) {
                            notificationService.notifyUserPostReacted(post.getUser(), post.getId(), userId, reaction);
                        }
                        notificationService.emitPostLikeUpdated(post.getId());
                        return new ToggleLikeResult(true, reaction, postLikeRepository.countByPost(post));
                    }
                })
                .orElseGet(() -> {
                    PostLike created = PostLike.builder().post(post).user(user).reaction(reaction).build();
                    postLikeRepository.save(created);
                    if (!post.getUser().getId().equals(userId)) {
                        notificationService.notifyUserPostReacted(post.getUser(), post.getId(), userId, reaction);
                    }
                    notificationService.emitPostLikeUpdated(post.getId());
                    return new ToggleLikeResult(true, reaction, postLikeRepository.countByPost(post));
                });
    }

    public record ToggleLikeResult(boolean liked, Reaction reaction, long totalLikes) {}
}


