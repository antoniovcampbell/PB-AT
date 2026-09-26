package com.pbtp1.avaliacoes.auth;

import com.pbtp1.shared.auth.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthClaimsServiceTest {
    private static final String SECRET = "claims-test-secret";
    private final AuthClaimsService auth = new AuthClaimsService();

    @BeforeEach
    void configurarSecret() {
        ReflectionTestUtils.setField(auth, "secret", SECRET);
    }

    @Test
    void requireExtraiClaimsValidos() {
        String token = TokenService.issue(12L, "user@example.com", "User Name", "USER", SECRET);

        TokenService.Claims claims = auth.require("Bearer " + token);

        assertThat(claims.id()).isEqualTo(12L);
        assertThat(claims.name()).isEqualTo("User Name");
    }

    @Test
    void requireConverteFalhaDeTokenEmUnauthorized() {
        assertThatThrownBy(() -> auth.require(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThatThrownBy(() -> auth.require("Bearer inválido"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void identificaAdminEExigePerfilAdmin() {
        TokenService.Claims admin = new TokenService.Claims(1L, "admin@example.com", "Admin", "ADMIN", Long.MAX_VALUE);
        TokenService.Claims usuario = new TokenService.Claims(2L, "user@example.com", "User", "USER", Long.MAX_VALUE);

        assertThat(auth.isAdmin(admin)).isTrue();
        assertThat(auth.isAdmin(usuario)).isFalse();
        assertThat(auth.isAdmin(null)).isFalse();
        assertThat(auth.requireAdmin("Bearer " + TokenService.issue(1L, "admin@example.com", "Admin", "ADMIN", SECRET)).role())
                .isEqualTo("ADMIN");
        assertThatThrownBy(() -> auth.requireAdmin("Bearer " + TokenService.issue(2L, "user@example.com", "User", "USER", SECRET)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.FORBIDDEN);
    }
}
