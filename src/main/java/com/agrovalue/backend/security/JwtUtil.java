package com.agrovalue.backend.security;

import com.agrovalue.backend.config.CustomOAuth2User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mysecretkeymysecretkeymysecretkey}")
    private String SECRET;
    
    @Value("${jwt.expiration:86400000}")
    private int jwtExpiration;

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // NEW METHOD: For OAuth2 authentication
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        String email;
        String role;
        
        // Handle OAuth2 authentication
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User userPrincipal = (CustomOAuth2User) authentication.getPrincipal();
            email = userPrincipal.getEmail();
            role = userPrincipal.getAuthorities().iterator().next().getAuthority();
        } 
        // Handle regular authentication with UserDetails
        else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
            role = userDetails.getAuthorities().iterator().next().getAuthority();
        } 
        else {
            email = authentication.getName();
            role = "ROLE_USER";
        }
        
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Existing method - keep this
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}