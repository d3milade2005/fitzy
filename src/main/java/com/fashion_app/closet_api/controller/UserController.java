package com.fashion_app.closet_api.controller;

import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.dto.AuthenticationResponse;
import com.fashion_app.closet_api.dto.UserLoginRequest;
import com.fashion_app.closet_api.dto.UserRegisterRequest;
import com.fashion_app.closet_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegisterRequest requestInput) {
        userService.signUp(requestInput);
        Map<String, Object> response = Map.of(
                "message", "User registered successfully"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("verificationCode") String verificationCode) {
        userService.verifyUser(verificationCode);
        return ResponseEntity.ok("User verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserLoginRequest requestInput) {
        return ResponseEntity.ok(userService.login(requestInput));
    }

    @PostMapping("/resend")
    public ResponseEntity<String> resendVerificationCode(@RequestParam("email") String email) {
        userService.resendVerificationCode(email);
        return ResponseEntity.ok("Verification code sent successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }
}
