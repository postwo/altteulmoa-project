package com.example.altteulmoa_project.controller;

import com.example.altteulmoa_project.dto.LoginRequestDTO;
import com.example.altteulmoa_project.dto.LoginResponseDTO;
import com.example.altteulmoa_project.dto.UserRequestDTO;
import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import com.example.altteulmoa_project.service.UserService;
import com.example.altteulmoa_project.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
//    @PostMapping("/login")
//    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO) {
//        return userService.login(loginRequestDTO);
//    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        LoginResponseDTO tokens = userService.login(request);

        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponseDTO(tokens.getAccessToken(),null));
    }

    // 토큰 재발급
    //Authorization 키 값 = header에 있는 refreshToken을 가지고 온다
    @PostMapping("/refresh")
    public ResponseEntity<?> reissue(HttpServletRequest request) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다");
        }

        String username  = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("사용자가 존재 하지 않습니다 "));// 유저 찾기

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("서버에 저장된 리프레시 토큰과 다릅니다 ");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken,null));
    }

    // 로그아웃 토큰 삭제
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken,HttpServletResponse response) {
        String token = accessToken.replace("Bearer ",""); //Bearer 와 공백 까지 지우고 토큰을 가지고 온다
        String username = jwtTokenProvider.getUsernameFromToken(token);

        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("사용자가 존재 하지 않습니다 "));// 유저 찾기

        user.setRefreshToken(null);
        userRepository.save(user);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("로그아웃");
    }

}
