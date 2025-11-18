package com.example.social.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReqLoginDTO {

    @NotBlank(message = "Username or email cannot be empty")
    @JsonAlias({"identifier", "email"})
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
