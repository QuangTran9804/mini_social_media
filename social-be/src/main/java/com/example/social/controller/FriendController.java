package com.example.social.controller;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Friend;
import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import com.example.social.domain.User;
import com.example.social.dto.request.friend.ReqSendFriendRequest;
import com.example.social.repository.FriendRequestRepository;
import com.example.social.service.CurrentUserService;
import com.example.social.service.FriendService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FriendController {

    private final FriendService friendService;
    private final FriendRequestRepository friendRequestRepository;
    private final CurrentUserService currentUserService;

    public FriendController(FriendService friendService,
                            FriendRequestRepository friendRequestRepository,
                            CurrentUserService currentUserService) {
        this.friendService = friendService;
        this.friendRequestRepository = friendRequestRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/friend-requests")
    public ResponseEntity<FriendRequest> send(@Valid @RequestBody ReqSendFriendRequest request)
            throws ResourceAlreadyExistsException, ResourceNotFoundException {
        return ResponseEntity.ok(friendService.sendRequest(currentUserService.getCurrentUserId(), request.getReceiverId()));
    }

    @DeleteMapping("/friend-requests/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id)
            throws ResourceNotFoundException, IllegalAccessException {
        friendService.cancelRequest(currentUserService.getCurrentUserId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friend-requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id)
            throws ResourceNotFoundException, IllegalAccessException {
        friendService.respondRequest(currentUserService.getCurrentUserId(), id, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friend-requests/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id)
            throws ResourceNotFoundException, IllegalAccessException {
        friendService.respondRequest(currentUserService.getCurrentUserId(), id, false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friend-requests/inbox")
    public ResponseEntity<List<FriendRequest>> inbox() throws ResourceNotFoundException {
        User receiver = currentUserService.getCurrentUser();
        return ResponseEntity.ok(friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING));
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<List<FriendRequest>> sent() throws ResourceNotFoundException {
        User sender = currentUserService.getCurrentUser();
        return ResponseEntity.ok(friendRequestRepository.findBySender(sender));
    }

    @GetMapping("/friends")
    public ResponseEntity<List<Friend>> listFriends() throws ResourceNotFoundException {
        return ResponseEntity.ok(friendService.listFriends(currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable Long friendId)
            throws ResourceNotFoundException {
        friendService.unfriend(currentUserService.getCurrentUserId(), friendId);
        return ResponseEntity.ok().build();
    }
}


