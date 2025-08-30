package com.example.altteulmoa_project.controller;

import com.example.altteulmoa_project.dto.LoginRequestDTO;
import com.example.altteulmoa_project.dto.LoginResponseDTO;
import com.example.altteulmoa_project.dto.UserRequestDTO;
import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import com.example.altteulmoa_project.service.UserService;
import com.example.altteulmoa_project.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 회원가입
    @PostMapping("/signup")
    public String signUp(@RequestBody UserRequestDTO userRequestDTO) {
        userService.signUp(userRequestDTO);
        return "회원가입";
    }

    // 로그인
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.login(loginRequestDTO);
    }

    // 토큰 재발급
    //Authorization 키 값 = header에 있는 refreshToken을 가지고 온다
    @PostMapping("/refresh")
    public LoginResponseDTO reissue(@RequestHeader("Authorization") String refreshToken) {
        String token =refreshToken.replace("Bearer ",""); //Bearer 와 공백 까지 지우고 토큰 값을 가지고 온다

        // 코인이 유효 한지 검사
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("사용자를 찾을 수 없습니다"));

        // refresh token이 일치하는지 검사
        if (!token.equals(user.getRefreshToken())) {
            throw new RuntimeException("서버에 저장된 Refresh Tokenr과 일치 하지 않습니다");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, newRefreshToken)).getBody();
    }

    // 로그아웃 토큰 삭제
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ",""); //Bearer 와 공백 까지 지우고 토큰을 가지고 온다
        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("사용자가 존재 하지 않습니다 "));// 유저 찾기

        user.setRefreshToken(null);
        userRepository.save(user);

        return "로그아웃 성공";
    }

}
