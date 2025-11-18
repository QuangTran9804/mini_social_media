package com.example.social.controller;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.auth.ReqForgotPasswordDTO;
import com.example.social.dto.request.auth.ReqLoginDTO;
import com.example.social.dto.request.auth.ReqRegisterDTO;
import com.example.social.dto.request.auth.ReqResetPasswordDTO;
import com.example.social.dto.response.auth.ResAuthMessageDTO;
import com.example.social.dto.response.auth.ResLoginDTO;
import com.example.social.service.AuthAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticateController {

    private final AuthAccountService authAccountService;

    public AuthenticateController(AuthAccountService authAccountService) {
        this.authAccountService = authAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLogin,
                                             HttpServletRequest servletRequest) {
        ResLoginDTO response = authAccountService.login(
                reqLogin,
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody ReqRegisterDTO request)
            throws ResourceAlreadyExistsException {
        authAccountService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResAuthMessageDTO> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(authAccountService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResAuthMessageDTO> resetPassword(@Valid @RequestBody ReqResetPasswordDTO request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(authAccountService.resetPassword(request));
    }
}
