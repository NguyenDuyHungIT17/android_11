package com.example.backendTeam12.utils;

public class Validate {
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^[0-9]{9,11}$");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
