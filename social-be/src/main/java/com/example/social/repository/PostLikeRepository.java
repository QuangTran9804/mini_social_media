package com.example.social.repository;

import com.example.social.domain.Post;
import com.example.social.domain.PostLike;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    long countByPost(Post post);
    List<PostLike> findByPost(Post post);
}


