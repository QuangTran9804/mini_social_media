package com.example.social.dto.response.auth;

import com.example.social.dto.response.user.ResGetUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResLoginDTO {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private ResGetUserDTO user;
}
