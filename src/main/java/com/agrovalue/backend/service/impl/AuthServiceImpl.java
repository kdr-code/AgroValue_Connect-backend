package com.agrovalue.backend.service.impl;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.agrovalue.backend.dto.AuthResponse;
import com.agrovalue.backend.dto.RegisterRequest;
import com.agrovalue.backend.entity.Role;
import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.repository.RoleRepository;
import com.agrovalue.backend.repository.UserRepository;
import com.agrovalue.backend.security.JwtUtil;
import com.agrovalue.backend.service.AuthService;
import com.agrovalue.backend.service.EmailService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);

        String roleName = "ROLE_" + request.getRole().toUpperCase();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Invalid role"));

        user.setRoles(Set.of(role));

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return "User registered. Check email.";
    }

    @Override
    public String verifyUser(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return "Email verified successfully!";
    }

    @Override
    public AuthResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("Verify email first");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(email);

        String role = user.getRoles().iterator().next().getName();

        return new AuthResponse(token, email, role);
    }
}