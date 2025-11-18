package com.example.social.service;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.LoginHistory;
import com.example.social.domain.LoginStatus;
import com.example.social.domain.User;
import com.example.social.dto.request.auth.ReqForgotPasswordDTO;
import com.example.social.dto.request.auth.ReqLoginDTO;
import com.example.social.dto.request.auth.ReqRegisterDTO;
import com.example.social.dto.request.auth.ReqResetPasswordDTO;
import com.example.social.dto.response.auth.ResAuthMessageDTO;
import com.example.social.dto.response.auth.ResLoginDTO;
import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.repository.LoginHistoryRepository;
import com.example.social.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthAccountService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticateService authenticateService;

    @Value("${app.auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.auth.lock-duration-minutes:15}")
    private long lockDurationMinutes;

    @Value("${app.auth.reset-token-expiry-minutes:15}")
    private long resetTokenExpiryMinutes;

    private final SecureRandom random = new SecureRandom();

    public AuthAccountService(UserRepository userRepository,
                              LoginHistoryRepository loginHistoryRepository,
                              PasswordEncoder passwordEncoder,
                              AuthenticateService authenticateService) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticateService = authenticateService;
    }

    @Transactional
    public ResLoginDTO login(ReqLoginDTO request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmailOrUsername(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không đúng"));

        if (isLocked(user)) {
            throw new BadCredentialsException("Tài khoản đang bị khoá tạm thời. Vui lòng thử lại sau.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedAttempt(user, ipAddress, userAgent);
            throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
        }

        resetFailedAttempts(user);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        logAttempt(user, LoginStatus.SUCCESS, ipAddress, userAgent);

        ResGetUserDTO userDTO = mapToUserDTO(user);
        String token = authenticateService.createToken(user.getEmail(), userDTO);

        return ResLoginDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(authenticateService.getAccessTokenExpiration())
                .user(userDTO)
                .build();
    }

    @Transactional
    public void register(ReqRegisterDTO request) throws ResourceAlreadyExistsException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã được sử dụng.");
        }
        Optional<User> byUsername = userRepository.findByUsername(request.getUsername());
        if (byUsername.isPresent()) {
            throw new ResourceAlreadyExistsException("Username " + request.getUsername() + " đã tồn tại.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .bio("")
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public ResAuthMessageDTO forgotPassword(ReqForgotPasswordDTO request) throws ResourceNotFoundException {
        User user = Optional.ofNullable(userRepository.findByEmail(request.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email " + request.getEmail()));

        String code = generateVerificationCode();
        user.setResetCode(code);
        user.setResetCodeExpiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes));
        userRepository.save(user);

        return ResAuthMessageDTO.builder()
                .message("Mã đặt lại mật khẩu: " + code + ". Mã có hiệu lực trong " + resetTokenExpiryMinutes + " phút.")
                .build();
    }

    @Transactional
    public ResAuthMessageDTO resetPassword(ReqResetPasswordDTO request) throws ResourceNotFoundException {
        User user = Optional.ofNullable(userRepository.findByEmail(request.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email " + request.getEmail()));

        if (user.getResetCode() == null ||
                !user.getResetCode().equals(request.getVerificationCode()) ||
                user.getResetCodeExpiresAt() == null ||
                user.getResetCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Mã xác minh không hợp lệ hoặc đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);

        return ResAuthMessageDTO.builder()
                .message("Đặt lại mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.")
                .build();
    }

    private void handleFailedAttempt(User user, String ipAddress, String userAgent) {
        int attempts = Optional.ofNullable(user.getFailedLoginAttempts()).orElse(0) + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setLockoutEndTime(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);
        logAttempt(user, LoginStatus.FAILED, ipAddress, userAgent);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
    }

    private boolean isLocked(User user) {
        return user.getLockoutEndTime() != null && user.getLockoutEndTime().isAfter(LocalDateTime.now());
    }

    private void logAttempt(User user, LoginStatus status, String ipAddress, String userAgent) {
        LoginHistory history = LoginHistory.builder()
                .username(user.getUsername())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .build();
        loginHistoryRepository.save(history);
    }

    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private ResGetUserDTO mapToUserDTO(User user) {
        return ResGetUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

