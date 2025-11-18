package com.example.social.repository;

import com.example.social.domain.Friend;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);

    Optional<Friend> findByUserAndFriend(User user, User friend);

    @Query("select f.friend.id from Friend f where f.user.id = ?1")
    List<Long> findFriendIdsByUserId(Long userId);

    void deleteByUserAndFriend(User user, User friend);
}


