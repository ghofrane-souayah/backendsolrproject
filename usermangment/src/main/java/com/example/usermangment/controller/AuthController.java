package com.example.usermangment.controller;

import com.example.usermangment.dto.LoginRequest;
import com.example.usermangment.dto.LoginResponse;
import com.example.usermangment.dto.RegisterRequest;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.EmailVerificationToken;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.EmailVerificationTokenRepository;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.security.JwtService;
import com.example.usermangment.service.MailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationTokenRepository tokenRepo;
    private final MailService mailService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          CompanyRepository companyRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          EmailVerificationTokenRepository tokenRepo,
                          MailService mailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepo = tokenRepo;
        this.mailService = mailService;
    }

    // ✅ LOGIN (bloqué si email non confirmé)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String email = request.getEmail().trim().toLowerCase();

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        // ✅ PRO: refuser login si pas confirmé
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }

        List<String> roles = List.of(user.getRole().name());
        String token = jwtService.generate(email, roles);

        return ResponseEntity.ok(
                new LoginResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        roles,
                        token
                )
        );
    }

    // ✅ REGISTER PRO (créé disabled + envoie mail confirmation)
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {

        // ✅ validation
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getUsername() == null || request.getUsername().isBlank()
                || request.getCompanyCode() == null || request.getCompanyCode().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();
        String companyCode = request.getCompanyCode().trim();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Company company = companyRepository.findByCode(companyCode).orElse(null);
        if (company == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // ✅ rôle par défaut
        Role roleEnum = Role.USER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                roleEnum = Role.valueOf(request.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                roleEnum = Role.USER;
            }
        }

        // ✅ créer user DISABLED
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleEnum);
        user.setEnabled(false);         // ✅ IMPORTANT (pro)
        user.setCompany(company);

        User saved = userRepository.save(user);

        // supprimer anciens tokens si existent
        tokenRepo.deleteByUser_Id(saved.getId());

        // créer token 24h
        String token = UUID.randomUUID().toString().replace("-", "");
        EmailVerificationToken t = new EmailVerificationToken(
                token,
                Instant.now().plus(24, ChronoUnit.HOURS),
                saved
        );
        tokenRepo.save(t);

        // lien de validation
        String link = frontendBaseUrl + "/verify-email?token=" + token;

        // envoyer mail
        try {
            mailService.send(
                    saved.getEmail(),
                    "Confirmez votre compte",
                    "Bonjour " + saved.getUsername() + "...\n" + link
            );
        } catch (Exception ex) {
            // log seulement, ne casse pas le register
            System.out.println("MAIL FAILED: " + ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

        // ✅ VERIFY EMAIL
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {

        EmailVerificationToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid token"));

        if (t.isUsed()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Token already used");
        }
        if (t.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Token expired");
        }

        User user = t.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        t.setUsed(true);
        tokenRepo.save(t);

        return ResponseEntity.ok("Account verified");
    }

    @GetMapping("/ping")
    public String ping() {
        return "auth ok";
    }
}
