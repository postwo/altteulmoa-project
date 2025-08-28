package com.example.altteulmoa_project.service;

import com.example.altteulmoa_project.dto.UserRequestDTO;
import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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



}
