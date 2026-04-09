package com.agrovalue.backend.config;

import com.agrovalue.backend.dto.GoogleOAuth2UserInfo;
import com.agrovalue.backend.entity.Role;
import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.repository.RoleRepository;
import com.agrovalue.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        
        
        User user = saveOrUpdateUser(userInfo, provider);
        
        
        return new CustomOAuth2User(oAuth2User, user);
    }
    
    @Transactional
    private User saveOrUpdateUser(GoogleOAuth2UserInfo userInfo, String provider) {
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .orElseGet(() -> userRepository.findByEmail(userInfo.getEmail())
                        .orElse(new User()));
        
        user.setEmail(userInfo.getEmail());
        user.setName(userInfo.getName());
        user.setProvider(provider);
        user.setProviderId(userInfo.getId());
        user.setImageUrl(userInfo.getImageUrl());
        user.setEmailVerified(true);
        user.setVerified(true); 
        
        
        if (user.getId() == null) {
            
            initializeDefaultRoles();
            
            
            Role buyerRole = roleRepository.findByName("ROLE_BUYER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_BUYER not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(buyerRole);
            user.setRoles(roles);
        }
        
        return userRepository.save(user);
    }
    
    private void initializeDefaultRoles() {
        
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            
            Role farmerRole = new Role();
            farmerRole.setName("ROLE_FARMER");
            
            Role buyerRole = new Role();
            buyerRole.setName("ROLE_BUYER");
            
            roleRepository.save(adminRole);
            roleRepository.save(farmerRole);
            roleRepository.save(buyerRole);
        }
    }
}
