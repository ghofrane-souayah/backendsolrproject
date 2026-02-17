package com.example.usermangment;

import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@solr.com");
            admin.setPassword(passwordEncoder.encode("12345")); // ✅ BCrypt
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            User viewer = new User();
            viewer.setUsername("viewer");
            viewer.setEmail("viewer@solr.com");
            viewer.setPassword(passwordEncoder.encode("12345")); // ✅ BCrypt
            viewer.setRole(Role.USER);
            viewer.setEnabled(true);

            userRepository.save(admin);
            userRepository.save(viewer);

            System.out.println("✅ Comptes admin / user créés (BCrypt)");
        }
    }
}
