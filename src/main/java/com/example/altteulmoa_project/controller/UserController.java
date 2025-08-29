package com.example.altteulmoa_project.controller;

import com.example.altteulmoa_project.dto.LoginRequestDTO;
import com.example.altteulmoa_project.dto.UserRequestDTO;
import com.example.altteulmoa_project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public String signUp(@RequestBody UserRequestDTO userRequestDTO) {
        userService.signUp(userRequestDTO);
        return "회원가입";
    }

    // 로그인
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.login(loginRequestDTO);
    }


}
