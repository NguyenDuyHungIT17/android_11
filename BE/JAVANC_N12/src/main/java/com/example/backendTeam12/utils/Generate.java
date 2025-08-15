package com.example.backendTeam12.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Generate {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
