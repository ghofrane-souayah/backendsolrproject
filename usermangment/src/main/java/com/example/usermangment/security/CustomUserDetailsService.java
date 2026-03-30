package com.example.usermangment.security;

import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User u = userRepo.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // ✅ TRÈS IMPORTANT: Spring attend ROLE_XXX
        String role = "ROLE_" + u.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),                 // principal (auth.getName() = email)
                u.getPassword(),
                u.isEnabled(),                // enabled
                true,                         // accountNonExpired
                true,                         // credentialsNonExpired
                true,                         // accountNonLocked
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}