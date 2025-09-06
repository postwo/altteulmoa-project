package com.example.altteulmoa_project.service;

import com.example.altteulmoa_project.dto.LoginResponseDTO;
import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import com.example.altteulmoa_project.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public LoginResponseDTO loginWithGoogle(String code, HttpServletResponse response) {

        // 1. 구글 토큰 요청
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        Map tokenResponse = restTemplate.postForObject(tokenUrl, tokenRequest, Map.class);

        String accessToken = (String) tokenResponse.get("access_token");

        // 2. 사용자 정보 요청
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);

        HttpEntity<?> userInfoRequest = new HttpEntity<>(infoHeaders);
        Map userInfo = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        ).getBody();

        String email = (String) userInfo.get("email");
        String username = "google_" + userInfo.get("id");

        // 3. DB 저장 or 조회
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User(username, null, "ROLE_USER");
                    return userRepository.save(newUser);
                });

        String jwtAccess = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return new LoginResponseDTO(jwtAccess, null);
    }
}
