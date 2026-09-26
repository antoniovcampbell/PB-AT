package com.pbtp1.controller;

import com.pbtp1.auth.AuthService;
import com.pbtp1.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthService.AuthResponse registrar(@RequestBody CadastroRequest request) {
        return authService.registrar(request.nome(), request.email(), request.senha());
    }

    @PostMapping("/login")
    public AuthService.AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.email(), request.senha());
    }

    @GetMapping("/me")
    public AuthService.UsuarioResponse me(@RequestHeader("Authorization") String authorization) {
        Usuario usuario = authService.requireUser(authorization);
        return new AuthService.UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                !Boolean.FALSE.equals(usuario.getAtivo()));
    }

    public record CadastroRequest(@NotBlank String nome, @Email @NotBlank String email, @NotBlank String senha) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String senha) {
    }
}
