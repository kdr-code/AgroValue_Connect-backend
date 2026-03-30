package com.agrovalue.backend.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agrovalue.backend.dto.AuthResponse;
import com.agrovalue.backend.dto.RegisterRequest;
import com.agrovalue.backend.entity.Role;
import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.repository.RoleRepository;
import com.agrovalue.backend.repository.UserRepository;
import com.agrovalue.backend.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ REGISTER
    public String register(RegisterRequest request) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        return "Email already exists";
    }

    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());
    user.setVerified(false);

    // 🔥 Normalize role input
    String roleName = "ROLE_" + request.getRole().toUpperCase();

    Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Invalid role"));

    user.setRoles(Set.of(role));

    // 📧 Email verification
    String token = UUID.randomUUID().toString();
    user.setVerificationToken(token);

    userRepository.save(user);

    emailService.sendVerificationEmail(user.getEmail(), token);

    return "User registered. Check email.";
}

    // ✅ VERIFY EMAIL
    public String verifyUser(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return "Email verified successfully!";
    }

    // ✅ LOGIN (JWT)
    public AuthResponse login(String email, String password) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.isVerified()) {
        throw new RuntimeException("Verify email first");
    }

    if (!user.getPassword().equals(password)) {
        throw new RuntimeException("Invalid password");
    }

    String token = jwtUtil.generateToken(email);

    String role = user.getRoles().iterator().next().getName();

    return new AuthResponse(token, email, role);
}
}