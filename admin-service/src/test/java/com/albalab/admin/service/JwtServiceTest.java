package com.albalab.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void debeGenerarTokenDeAdministrador() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("token-admin")
                        .header("alg", "none")
                        .subject("admin@albalab.cl")
                        .build());

        JwtService jwtService = new JwtService(jwtEncoder, 8);
        String token = jwtService.generarToken("admin@albalab.cl", "ADMIN");

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(token).isEqualTo("token-admin");
        assertThat(claims.getClaims().get("iss")).isEqualTo("albalab-admin-service");
        assertThat(claims.getSubject()).isEqualTo("admin@albalab.cl");
        assertThat(claims.getClaims().get("roles")).isEqualTo(List.of("ADMIN"));
        assertThat((Object) claims.getClaim("scope")).isEqualTo("ADMIN");
        assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(28800L);
    }
}
