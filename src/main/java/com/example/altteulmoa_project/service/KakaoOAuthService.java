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
public class KakaoOAuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.kakao.client-id}")
    private String clientId;

    @Value("${spring.kakao.redirect-uri}")
    private String redirectUri;

    public LoginResponseDTO kakaoLogin(String code, HttpServletResponse response) {

        // 1. 인가코드로 토큰 요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
        String kakaoAccessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 요청
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(kakaoAccessToken);
        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userInfoRequest,
                Map.class
        );

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoResponse.getBody().get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String username = "kakao_" + userInfoResponse.getBody().get("id");

        // 3. 사용자 등록 또는 조회
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User(username, null, "ROLE_USER"); // 비밀번호 null로 설정
                    return userRepository.save(newUser);
                });

        // 4. JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // 5. 쿠키 설정
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        return new LoginResponseDTO(accessToken, null);
    }
}
