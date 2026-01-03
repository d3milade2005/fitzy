package com.fashion_app.closet_api.service;

import com.fashion_app.closet_api.Entity.AuthProvider;
import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Entity.UserRole;
import com.fashion_app.closet_api.Repository.UserRepository;
import com.fashion_app.closet_api.dto.AuthenticationResponse;
import com.fashion_app.closet_api.dto.UserLoginRequest;
import com.fashion_app.closet_api.dto.UserRegisterRequest;
import com.fashion_app.closet_api.exception.BusinessException;
import com.fashion_app.closet_api.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailValidator emailValidator;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void signUp(UserRegisterRequest requestInput) {
        if (!emailValidator.isValidEmail(requestInput.getEmail())) {
            throw new BusinessException(
                    ErrorCode.INVALID_EMAIL_FORMAT,
                    HttpStatus.BAD_REQUEST,
                    "Invalid email format"
            );
        }
        if (userRepository.findByEmail(requestInput.getEmail()).isPresent()) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    HttpStatus.CONFLICT,
                    "Email is already in use"
            );
        }

        String encodedPassword = bCryptPasswordEncoder.encode(requestInput.getPassword());
        User registerNewUser = new User(
                requestInput.getFirstName(),
                requestInput.getLastName(),
                requestInput.getEmail(),
                encodedPassword,
                AuthProvider.LOCAL,
                UserRole.USER
        );

        String verificationCode = generateVerificationCode();
        registerNewUser.setVerificationCode(verificationCode);
        registerNewUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        emailService.sendNotification(requestInput.getEmail(), buildEmail(requestInput.getFirstName(), verificationCode));
        userRepository.save(registerNewUser);
    }

    public void verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_VERIFICATION_CODE,
                        HttpStatus.BAD_REQUEST,
                        "Invalid verification code"
                ));
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    ErrorCode.VERIFICATION_CODE_EXPIRED,
                    HttpStatus.BAD_REQUEST,
                    "Verification code has expired"
            );
        }
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
        if (user.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_ALREADY_VERIFIED,
                    HttpStatus.CONFLICT,
                    "Account is already verified"
            );
        }
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        emailService.sendNotification(user.getEmail(), buildEmail(user.getFirstName(), verificationCode));
        userRepository.save(user);
    }

    public String generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public AuthenticationResponse login(UserLoginRequest requestInput) {
        User user = userRepository.findByEmail(requestInput.getEmail())
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "User not found with email: " + requestInput.getEmail()
                ));
        if (!user.isEnabled()){
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_VERIFIED,
                    HttpStatus.FORBIDDEN,
                    "Account is not verified"
            );
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestInput.getEmail(), requestInput.getPassword())
        );
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    HttpStatus.BAD_REQUEST,
                    "Refresh token is missing"
            );
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            }
        }

        throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED,
                "Invalid Refresh Token"
        );
    }

    private String buildEmail(String name, String verificationCode) {
        // Basic structural styles for the body/wrapper
        String html = "<div style=\"font-family: Arial, sans-serif; font-size: 16px; color: #333333; background-color: #f7f7f7; padding: 20px;\">";

        // Main container table, centered, for better client compatibility
        html += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\">";

        // Content area (max-width for desktop)
        html += "<table width=\"100%\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); border-collapse: collapse;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">";

        // --- Header Section ---
        html += "<tr><td style=\"padding: 30px 30px 10px 30px; border-bottom: 1px solid #eeeeee;\">";
        html += "<h1 style=\"margin: 0; font-size: 24px; color: #1e88e5;\">Confirm Your Email</h1>"; // Blue accent color
        html += "</td></tr>";

        // --- Body Content ---
        html += "<tr><td style=\"padding: 30px;\">";

        // Greeting and main message
        html += "<p style=\"margin-top: 0; margin-bottom: 20px; line-height: 1.5;\">Hi <strong>" + name + "</strong>,</p>";
        html += "<p style=\"margin-bottom: 30px; line-height: 1.5;\">Thank you for registering. Please use the following code to complete your verification on our website or app:</p>";

        // --- Verification Code Block (The main UI element) ---
        html += "<table role=\"presentation\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\"><tr><td align=\"center\" style=\"padding: 15px 0;\">";
        html += "<span style=\"font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #ffffff; background-color: #1e88e5; padding: 15px 25px; display: inline-block; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">" + verificationCode + "</span>";
        html += "</td></tr></table>";

        // Instructions and Expiry
        html += "<p style=\"margin-top: 30px; margin-bottom: 5px; font-size: 14px; color: #777777; text-align: center;\">This code is valid for <strong>15 minutes</strong>.</p>";

        // Closing
        html += "<p style=\"margin-top: 30px; line-height: 1.5;\">If you have any trouble, please contact our support team.</p>";
        html += "<p style=\"margin: 0; line-height: 1.5;\">Best regards,<br>The Team</p>";

        html += "</td></tr>"; // End Body Content

        // --- Footer Section (Optional) ---
        html += "<tr><td style=\"padding: 15px 30px; font-size: 12px; color: #aaaaaa; text-align: center; border-top: 1px solid #eeeeee;\">";
        html += "If you did not request this code, you can safely ignore this email.";
        html += "</td></tr>";

        html += "</table>"; // End Content Area

        html += "</td></tr></table>"; // End Main Container Table

        html += "</div>"; // End Body/Wrapper

        return html;
    }
}