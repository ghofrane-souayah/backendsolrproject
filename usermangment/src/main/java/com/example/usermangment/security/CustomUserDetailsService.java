package com.example.usermangment.security;

import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        // ✅ CORRECT : Role est un enum
        Role role = user.getRole() != null ? user.getRole() : Role.USER;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled()) // ✅ important
                .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()))
                .build();

    }
}