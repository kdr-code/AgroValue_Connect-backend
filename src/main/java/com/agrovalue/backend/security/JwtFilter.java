package com.agrovalue.backend.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Only skip login and OAuth initiation endpoints
        if (path.equals("/auth/login") || 
            path.equals("/auth/register") ||
            path.startsWith("/oauth2/authorization") ||
            path.startsWith("/login/oauth2")) {
            chain.doFilter(request, response);
            return;
        }

        // Process JWT for all other endpoints including /auth/oauth2/user
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Token invalid - continue without authentication
            }
        }

        chain.doFilter(request, response);
    }
}