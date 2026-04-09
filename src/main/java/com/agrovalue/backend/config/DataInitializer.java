package com.agrovalue.backend.config;

import com.agrovalue.backend.entity.Role;
import com.agrovalue.backend.entity.User;
import com.agrovalue.backend.repository.RoleRepository;
import com.agrovalue.backend.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {

            
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

            Role farmerRole = roleRepository.findByName("ROLE_FARMER")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_FARMER")));

            Role buyerRole = roleRepository.findByName("ROLE_BUYER")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_BUYER")));

            
            if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@gmail.com");

                
                admin.setPassword(passwordEncoder.encode("admin123"));

                admin.setVerified(true);
                admin.setEmailVerified(true);

                admin.setProvider("local");

                
                admin.setRoles(Set.of(adminRole));

                userRepository.save(admin);
            }
        };
    }
}
