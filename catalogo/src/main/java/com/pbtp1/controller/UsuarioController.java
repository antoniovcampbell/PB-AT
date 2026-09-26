package com.pbtp1.controller;

import com.pbtp1.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final AuthService authService;

    @GetMapping
    public List<AuthService.UsuarioAdminResponse> listar(@RequestHeader("Authorization") String authorization) {
        authService.requireAdmin(authorization);
        return authService.listarUsuarios();
    }

    @PutMapping("/{id}/ativo")
    public AuthService.UsuarioAdminResponse atualizarAtivo(@PathVariable Long id, @RequestParam boolean ativo,
                                                            @RequestHeader("Authorization") String authorization) {
        authService.requireAdmin(authorization);
        return authService.atualizarAtivo(id, ativo);
    }
}
