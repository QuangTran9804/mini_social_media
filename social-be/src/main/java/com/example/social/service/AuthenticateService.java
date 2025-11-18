package com.example.social.service;

import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthenticateService {

    @Value("${security.authentication.jwt.token-validity-in-seconds}")
    private long accessTokenExpiration;

    private final JwtEncoder jwtEncoder;

    public AuthenticateService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createToken(String email, ResGetUserDTO userDB) {

        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        Map<String, Object> userClaim = buildUserClaim(userDB);
        List<String> permissions = List.of("ROLE_USER");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", userClaim)
                .claim("permissions", permissions)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(SecurityUtils.JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private Map<String, Object> buildUserClaim(ResGetUserDTO user) {
        Map<String, Object> claim = new HashMap<>();
        if (user == null) {
            return claim;
        }
        claim.put("id", user.getId());
        claim.put("username", user.getUsername());
        claim.put("avatarUrl", user.getAvatarUrl());
        claim.put("bio", user.getBio());
        claim.put("createdAt", user.getCreatedAt());
        claim.put("updatedAt", user.getUpdatedAt());
        return claim;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
