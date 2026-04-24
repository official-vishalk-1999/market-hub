package com.markethub.config;

import com.markethub.entity.User;
import com.markethub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin");
                admin.setRole("ADMIN");

                userRepository.save(admin);
            }
        };
    }
}