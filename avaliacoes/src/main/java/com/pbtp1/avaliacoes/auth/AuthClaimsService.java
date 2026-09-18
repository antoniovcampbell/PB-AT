package com.pbtp1.avaliacoes.auth;

import com.pbtp1.shared.auth.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthClaimsService {
    @Value("${app.auth.secret:pb-at-local-development-secret-change-me}")
    private String secret;

    public TokenService.Claims require(String authorization) {
        try {
            return TokenService.verify(authorization, secret);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para continuar");
        }
    }

    public boolean isAdmin(TokenService.Claims claims) {
        return claims != null && "ADMIN".equals(claims.role());
    }
}
