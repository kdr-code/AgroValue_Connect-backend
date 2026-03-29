package com.agrovalue.backend.service;

import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public String register(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Email already exists";
        }

        user.setVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return "User registered. Check email for verification.";
    }

    public String verifyUser(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return "Email verified successfully!";
    }

    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            return "Please verify your email first";
        }

        if (!user.getPassword().equals(password)) {
            return "Invalid password";
        }

        return "Login successful"; // later replace with JWT
    }
}