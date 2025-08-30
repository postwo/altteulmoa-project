package com.example.altteulmoa_project.controller;

import com.example.altteulmoa_project.entity.User;
import com.example.altteulmoa_project.repository.UserRepository;
import com.example.altteulmoa_project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository  userRepository;

    @GetMapping("/dashboard")
    public String adminOnly(){
        return "관리자 전용 대시보드";
    }

    @GetMapping("/users")
    public List<User> getAllUser() { return userRepository.findAll(); }
}
