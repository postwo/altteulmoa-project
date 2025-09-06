package com.example.altteulmoa_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;

    @Column(length = 500)
    private String refreshToken;

    public User(String username, String password ,String roleUser) {
        this.username = username;
        this.password = password; // 굳이 안넣어도 됨
        this.role = roleUser;
    }
}
