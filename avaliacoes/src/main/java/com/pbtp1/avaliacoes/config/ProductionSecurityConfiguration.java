package com.pbtp1.avaliacoes.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("production")
public class ProductionSecurityConfiguration {
    @Value("${app.auth.secret}")
    private String authSecret;

    @Value("${app.internal.secret}")
    private String internalSecret;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @PostConstruct
    void validateSecrets() {
        requireStrongSecret("APP_AUTH_SECRET", authSecret);
        requireStrongSecret("APP_INTERNAL_SECRET", internalSecret);
        requireConfigured("SPRING_DATASOURCE_PASSWORD", databasePassword);
        requireConfigured("SPRING_RABBITMQ_PASSWORD", rabbitPassword);
    }

    private void requireStrongSecret(String name, String value) {
        requireConfigured(name, value);
        if (value.length() < 32) {
            throw new IllegalStateException(name + " precisa ter pelo menos 32 caracteres");
        }
    }

    private void requireConfigured(String name, String value) {
        if (value == null || value.isBlank() || value.startsWith("REPLACE_WITH")
                || value.contains("demo-secret") || value.contains("internal-secret")) {
            throw new IllegalStateException(name + " nao foi configurado para producao");
        }
    }
}
