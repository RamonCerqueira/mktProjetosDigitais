package com.mktplace.service;

import com.mktplace.model.User;
import com.mktplace.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {
    private final UserRepository userRepository;
    public UserContextService(UserRepository userRepository) { this.userRepository = userRepository; }
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}
