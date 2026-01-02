package com.fashion_app.closet_api.service;

import org.springframework.stereotype.Service;

@Service
public class EmailValidator {
    public boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
