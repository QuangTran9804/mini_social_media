package com.example.social.repository;

import com.example.social.domain.Message;
import com.example.social.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        select m from Message m
        where (m.sender = ?1 and m.receiver = ?2) or (m.sender = ?2 and m.receiver = ?1)
        order by m.sentAt asc
    """)
    List<Message> findConversation(User a, User b);

    long countByReceiverAndIsReadIsFalse(User receiver);
}


