package com.pbtp1.shared.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public final class TokenService {
    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private TokenService() {
    }

    public static String issue(Long id, String email, String name, String role, String secret) {
        long expiresAt = Instant.now().plusSeconds(60 * 60 * 24).getEpochSecond();
        String payload = String.join("|", String.valueOf(id), encode(email), encode(name), role, String.valueOf(expiresAt));
        String encodedPayload = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload, secret);
    }

    public static Claims verify(String token, String secret) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token ausente");
        }
        String[] parts = token.replaceFirst("^Bearer\\s+", "").split("\\.", 2);
        if (parts.length != 2 || !constantTimeEquals(parts[1], sign(parts[0], secret))) {
            throw new IllegalArgumentException("Token inválido");
        }
        String[] values = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8).split("\\|", -1);
        if (values.length != 5 || Long.parseLong(values[4]) < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("Token expirado");
        }
        return new Claims(Long.valueOf(values[0]), decode(values[1]), decode(values[2]), values[3], Long.valueOf(values[4]));
    }

    private static String sign(String value, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível assinar o token", exception);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(DECODER.decode(value), StandardCharsets.UTF_8);
    }

    public record Claims(Long id, String email, String name, String role, Long expiresAt) {
    }
}
