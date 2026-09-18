package com.pbtp1.auth;

import com.pbtp1.model.PerfilUsuario;
import com.pbtp1.model.Usuario;
import com.pbtp1.repository.UsuarioRepository;
import com.pbtp1.shared.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.auth.secret:pb-at-local-development-secret-change-me}")
    private String secret;

    public AuthResponse registrar(String nome, String email, String senha) {
        validarCadastro(nome, email, senha);
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(nome.trim())
                .email(emailNormalizado)
                .senhaHash(hash(senha))
                .perfil(PerfilUsuario.USER)
                .build());
        return resposta(usuario);
    }

    public AuthResponse login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
        if (!matches(senha, usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        return resposta(usuario);
    }

    public Usuario requireUser(String authorization) {
        TokenService.Claims claims = claims(authorization);
        return usuarioRepository.findById(claims.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));
    }

    public Usuario requireAdmin(String authorization) {
        Usuario usuario = requireUser(authorization);
        if (usuario.getPerfil() != PerfilUsuario.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo para administradores");
        }
        return usuario;
    }

    public TokenService.Claims claims(String authorization) {
        try {
            return TokenService.verify(authorization, secret);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para continuar");
        }
    }

    public Usuario criarInicial(String nome, String email, String senha, PerfilUsuario perfil) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .nome(nome)
                        .email(email.toLowerCase())
                        .senhaHash(hash(senha))
                        .perfil(perfil)
                        .build()));
    }

    private AuthResponse resposta(Usuario usuario) {
        return new AuthResponse(
                TokenService.issue(usuario.getId(), usuario.getEmail(), usuario.getNome(), usuario.getPerfil().name(), secret),
                new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name()));
    }

    private void validarCadastro(String nome, String email, String senha) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank() || senha == null || senha.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, e-mail e senha de 6 caracteres são obrigatórios");
        }
    }

    private String hash(String senha) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(senha, salt, ITERATIONS));
    }

    private boolean matches(String senha, String armazenada) {
        try {
            String[] partes = armazenada.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);
            return MessageDigest.isEqual(esperado, derive(senha, salt, ITERATIONS));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private byte[] derive(String senha, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível proteger a senha", exception);
        }
    }

    public record AuthResponse(String token, UsuarioResponse usuario) {
    }

    public record UsuarioResponse(Long id, String nome, String email, String perfil) {
    }
}
