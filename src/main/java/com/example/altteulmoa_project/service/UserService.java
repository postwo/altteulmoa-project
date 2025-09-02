package com.example.altteulmoa_project.service;

import com.example.altteulmoa_project.dto.LoginRequestDTO;
import com.example.altteulmoa_project.dto.LoginResponseDTO;
import com.example.altteulmoa_project.dto.UserRequestDTO;
import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import com.example.altteulmoa_project.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void signUp(UserRequestDTO userRequestDTO) {
        // 유저가 있는지 검사
        if (userRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {
            // 이미 존재하는 사용자
            throw new RuntimeException("이미 존재하는 사용자 입니다");
        }

        User user = User.builder()
                .username(userRequestDTO.getUsername())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    //로그인
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 유저가 있는지 검사
        User user =userRepository.findByUsername(loginRequestDTO.getUsername()).orElseThrow(()->new RuntimeException("사용자를 찾을 수 없습니다"));

        // 패스워드가 정상적인지 검사
        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(),user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDTO(accessToken, refreshToken);
    }




}
