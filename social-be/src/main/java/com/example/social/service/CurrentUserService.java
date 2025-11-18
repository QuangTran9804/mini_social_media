package com.example.social.service;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.User;
import com.example.social.repository.UserRepository;
import com.example.social.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() throws ResourceNotFoundException {
        String email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found for email: " + email);
        }
        return user;
    }

    public Long getCurrentUserId() throws ResourceNotFoundException {
        return getCurrentUser().getId();
    }
}

