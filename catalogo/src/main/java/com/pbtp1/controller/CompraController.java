package com.pbtp1.controller;

import com.pbtp1.auth.AuthService;
import com.pbtp1.model.Usuario;
import com.pbtp1.model.StatusCompra;
import com.pbtp1.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {
    private final CompraService compraService;
    private final AuthService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraService.CompraResponse criar(@RequestHeader("Authorization") String authorization,
                                               @RequestBody CompraService.CompraRequest request) {
        return compraService.criar(authService.requireUser(authorization).getId(), request);
    }

    @GetMapping("/minhas")
    public List<CompraService.CompraResponse> minhas(@RequestHeader("Authorization") String authorization) {
        return compraService.minhas(authService.requireUser(authorization).getId());
    }

    @GetMapping
    public List<CompraService.CompraResponse> todas(@RequestHeader("Authorization") String authorization) {
        authService.requireAdmin(authorization);
        return compraService.todas();
    }

    @PutMapping("/{id}/status")
    public CompraService.CompraResponse atualizarStatus(@PathVariable Long id,
                                                         @RequestParam StatusCompra status,
                                                         @RequestHeader("Authorization") String authorization) {
        authService.requireAdmin(authorization);
        return compraService.atualizarStatus(id, status);
    }
}
