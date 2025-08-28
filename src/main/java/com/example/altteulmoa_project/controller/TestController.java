package com.example.altteulmoa_project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/public/hello")
    public String publicHello() {
        return "public hello world";
    }

    @GetMapping("/api/private/hello")
    public String privateHello() {
        return "private hello world";
    }
}
