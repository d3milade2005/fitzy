package com.fashion_app.closet_api.config;

import com.fashion_app.closet_api.Entity.AuthProvider;
import com.fashion_app.closet_api.Entity.User;
import com.fashion_app.closet_api.Entity.UserRole;
import com.fashion_app.closet_api.Repository.UserRepository;
import com.fashion_app.closet_api.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User registerNewUser = new User(
                            oAuth2User.getAttribute("given_name"),
                            oAuth2User.getAttribute("family_name"),
                            email,
                            null,
                            AuthProvider.GOOGLE,
                            UserRole.USER
                    );
                    registerNewUser.setEnabled(true);
                    return userRepository.save(registerNewUser);
                });

//        String jwtToken = jwtService.generateToken(user);
//
//        Cookie cookie = new Cookie("AUTH_TOKEN", jwtToken);
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);  // set to true in prod (requires HTTPS)
//        cookie.setMaxAge(60 * 60);
//        response.addCookie(cookie);
//        response.sendRedirect("http://localhost:3000/dashboard");

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String redirectUrl = String.format(
                "http://localhost:3000/dashboard?access_token=%s&refresh_token=%s",
                accessToken,
                refreshToken
        );

        response.sendRedirect(redirectUrl);
    }
}
