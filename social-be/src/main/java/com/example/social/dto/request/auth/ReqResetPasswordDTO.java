package com.example.social.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqResetPasswordDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String verificationCode;

    @NotBlank
    private String newPassword;
}

