package com.agrovalue.backend.config;

import com.agrovalue.backend.entity.Role;
import com.agrovalue.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
            if (roleRepository.findByName("ROLE_FARMER").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_FARMER"));
            }
            if (roleRepository.findByName("ROLE_BUYER").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_BUYER"));
            }
        };
    }
}