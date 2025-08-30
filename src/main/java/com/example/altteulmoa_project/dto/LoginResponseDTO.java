package com.example.altteulmoa_project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}
