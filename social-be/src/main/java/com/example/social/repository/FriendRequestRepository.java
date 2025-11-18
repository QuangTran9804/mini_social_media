package com.example.social.repository;

import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    boolean existsBySenderAndReceiver(User sender, User receiver);

    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    List<FriendRequest> findBySender(User sender);

    @Query("""
        select case when count(fr) > 0 then true else false end
        from FriendRequest fr
        where ((fr.sender = ?1 and fr.receiver = ?2) or (fr.sender = ?2 and fr.receiver = ?1))
          and fr.status = 'PENDING'
    """)
    boolean existsPendingBetween(User a, User b);
}


