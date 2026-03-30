package com.example.usermangment.controller;

import com.example.usermangment.dto.LoginRequest;
import com.example.usermangment.dto.LoginResponse;
import com.example.usermangment.dto.RegisterRequest;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.security.JwtService;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          CompanyRepository companyRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()
                || req.getCompanyCode() == null || req.getCompanyCode().isBlank()) {
            return ResponseEntity.badRequest().body("All fields required");
        }

        String email = req.getEmail().trim().toLowerCase();
        String companyCode = req.getCompanyCode().trim();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("EMAIL_EXISTS");
        }

        Company company = companyRepository.findByCodeIgnoreCase(companyCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_COMPANY_CODE"));

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setCompany(company); // ✅ obligatoire sinon 400/500

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("REGISTER_OK");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Email/password required");
        }

        String email = request.getEmail().trim().toLowerCase();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found after auth"));

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ACCOUNT_DISABLED");
            }

            List<String> roles = List.of(user.getRole().name());
            String token = jwtService.generate(user.getEmail(), roles);

            return ResponseEntity.ok(new LoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    roles,
                    token
            ));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("BAD_CREDENTIALS");
        }
    }

    @GetMapping("/ping")
    public String ping() {
        return "auth ok";
    }
}