package com.albalab.usuario.service;

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
    void debeGenerarTokenConLosDatosDelUsuario() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("token-usuario")
                        .header("alg", "none")
                        .subject("usuario@albalab.cl")
                        .build());

        JwtService jwtService = new JwtService(jwtEncoder, 8);
        String token = jwtService.generarToken(1L, "usuario@albalab.cl", "USER");

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(token).isEqualTo("token-usuario");
        assertThat(claims.getClaims().get("iss")).isEqualTo("albalab-usuario-service");
        assertThat(claims.getSubject()).isEqualTo("usuario@albalab.cl");
        assertThat((Object) claims.getClaim("usuarioId")).isEqualTo(1L);
        assertThat(claims.getClaims().get("roles")).isEqualTo(List.of("USER"));
        assertThat((Object) claims.getClaim("scope")).isEqualTo("USER");
        assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(28800L);
    }

    @Test
    void debePermitirGenerarTokenSinIdDeUsuario() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject("usuario@albalab.cl")
                        .build());

        new JwtService(jwtEncoder, 1).generarToken("usuario@albalab.cl", "USER");

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        assertThat(captor.getValue().getClaims().getClaims()).doesNotContainKey("usuarioId");
    }
}
