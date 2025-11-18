package com.example.social.service;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Friend;
import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import com.example.social.domain.User;
import com.example.social.repository.FriendRepository;
import com.example.social.repository.FriendRequestRepository;
import com.example.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRepository friendRepository,
                         FriendRequestRepository friendRequestRepository,
                         UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    public boolean areFriends(User a, User b) {
        return friendRepository.existsByUserAndFriend(a, b) || friendRepository.existsByUserAndFriend(b, a);
    }

    @Transactional
    public FriendRequest sendRequest(Long senderId, Long receiverId) throws ResourceAlreadyExistsException, ResourceNotFoundException {
        if (senderId.equals(receiverId)) {
            throw new ResourceAlreadyExistsException("Cannot send friend request to yourself");
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (areFriends(sender, receiver)) {
            throw new ResourceAlreadyExistsException("Already friends");
        }
        if (friendRequestRepository.existsPendingBetween(sender, receiver)) {
            throw new ResourceAlreadyExistsException("There is already a pending request between these users");
        }
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            // existing record (accepted or rejected) should not block resending unless pending; we enforce business rule to avoid duplicates overall
            throw new ResourceAlreadyExistsException("Friend request already exists");
        }

        FriendRequest fr = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();
        return friendRequestRepository.save(fr);
    }

    @Transactional
    public void cancelRequest(Long requesterId, Long requestId) throws ResourceNotFoundException, IllegalAccessException {
        FriendRequest fr = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        if (!fr.getSender().getId().equals(requesterId)) {
            throw new IllegalAccessException("Only the requester can cancel this request");
        }
        if (fr.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }
        friendRequestRepository.delete(fr);
    }

    @Transactional
    public void respondRequest(Long receiverId, Long requestId, boolean accept) throws ResourceNotFoundException, IllegalAccessException {
        FriendRequest fr = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        if (!fr.getReceiver().getId().equals(receiverId)) {
            throw new IllegalAccessException("Only the receiver can respond to this request");
        }
        if (fr.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been processed");
        }
        if (accept) {
            fr.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(fr);
            // create two directional friend rows
            if (!friendRepository.existsByUserAndFriend(fr.getSender(), fr.getReceiver())) {
                friendRepository.save(Friend.builder().user(fr.getSender()).friend(fr.getReceiver()).build());
            }
            if (!friendRepository.existsByUserAndFriend(fr.getReceiver(), fr.getSender())) {
                friendRepository.save(Friend.builder().user(fr.getReceiver()).friend(fr.getSender()).build());
            }
        } else {
            fr.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepository.save(fr);
        }
    }

    public List<Friend> listFriends(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return friendRepository.findByUser(user);
    }

    @Transactional
    public void unfriend(Long userId, Long friendId) throws ResourceNotFoundException {
        if (userId.equals(friendId)) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User other = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend user not found"));
        if (friendRepository.existsByUserAndFriend(user, other)) {
            friendRepository.deleteByUserAndFriend(user, other);
        }
        if (friendRepository.existsByUserAndFriend(other, user)) {
            friendRepository.deleteByUserAndFriend(other, user);
        }
    }
}


