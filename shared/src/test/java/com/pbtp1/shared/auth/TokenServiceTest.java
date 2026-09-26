package com.pbtp1.shared.auth;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {
    private static final String SECRET = "test-token-secret";

    @Test
    void emiteEVerificaClaimsComPrefixoBearer() {
        String token = TokenService.issue(42L, "ana@example.com", "Ana Silva", "USER", SECRET);

        TokenService.Claims claims = TokenService.verify("Bearer " + token, SECRET);

        assertThat(claims.id()).isEqualTo(42L);
        assertThat(claims.email()).isEqualTo("ana@example.com");
        assertThat(claims.name()).isEqualTo("Ana Silva");
        assertThat(claims.role()).isEqualTo("USER");
        assertThat(claims.expiresAt()).isGreaterThan(System.currentTimeMillis() / 1000);
    }

    @Test
    void rejeitaTokenAusenteMalformadoOuComAssinaturaInvalida() {
        assertThatThrownBy(() -> TokenService.verify(null, SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token ausente");
        assertThatThrownBy(() -> TokenService.verify("  ", SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token ausente");
        assertThatThrownBy(() -> TokenService.verify("malformado", SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token inválido");
        assertThatThrownBy(() -> TokenService.verify("abc.def", SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token inválido");

        String token = TokenService.issue(1L, "a@b.com", "A", "ADMIN", SECRET);
        assertThatThrownBy(() -> TokenService.verify(token, "different-secret"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token inválido");
    }

    @Test
    void rejeitaPayloadComFormatoInvalidoETokenExpirado() throws Exception {
        assertThatThrownBy(() -> TokenService.verify(tokenAssinado("apenas|quatro|partes|USER", SECRET), SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token expirado");

        String expirado = "7|" + codificar("ana@example.com") + "|" + codificar("Ana") + "|USER|1";
        assertThatThrownBy(() -> TokenService.verify(tokenAssinado(expirado, SECRET), SECRET))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Token expirado");
    }

    private String tokenAssinado(String payload, String secret) throws Exception {
        String encoded = codificar(payload);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(encoded.getBytes(StandardCharsets.UTF_8)));
        return encoded + "." + signature;
    }

    private String codificar(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
