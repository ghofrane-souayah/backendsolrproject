package com.example.usermangment.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private boolean used = false;

    public EmailVerificationToken() {}

    public EmailVerificationToken(String token, Instant expiresAt, User user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
        this.used = false;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public User getUser() { return user; }
    public boolean isUsed() { return used; }

    public void setUsed(boolean used) { this.used = used; }
}

