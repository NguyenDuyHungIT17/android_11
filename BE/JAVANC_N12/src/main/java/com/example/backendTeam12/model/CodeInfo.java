package com.example.backendTeam12.model;

import java.time.LocalDateTime;

public class CodeInfo {
    private final String code;         // mã reset 6 ký tự
    private final LocalDateTime expiry; // thời hạn hết hiệu lực

    public CodeInfo(String code, LocalDateTime expiry) {
        this.code = code;
        this.expiry = expiry;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }
}
