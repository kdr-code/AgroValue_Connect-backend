package com.agrovalue.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    // 🔐 Email verification
    private boolean isVerified = false;
    private String verificationToken;
    
    // 🌐 OAuth2 Fields
    @Column(name = "provider")
    private String provider;  // "google" or "local"
    
    @Column(name = "provider_id")
    private String providerId; // Google sub ID
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "email_verified")
    private boolean emailVerified = false;

    // 🔑 Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // 📦 Orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;
}