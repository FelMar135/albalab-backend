package com.albalab.admin.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.expiration-hours:8}") long expirationHours
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationHours * 3600;
    }

    public String generarToken(String email, String role) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("albalab-admin-service")
                .subject(email)
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(expirationSeconds))
                .claim("roles", List.of(role))
                .claim("scope", role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
