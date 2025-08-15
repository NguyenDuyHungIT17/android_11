package com.example.backendTeam12.model;

import java.time.LocalDateTime;

public class TokenInfo {
    private final String email;
    private final LocalDateTime expiry;

    public TokenInfo(String email, LocalDateTime expiry) {
        this.email = email;
        this.expiry = expiry;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }
}
