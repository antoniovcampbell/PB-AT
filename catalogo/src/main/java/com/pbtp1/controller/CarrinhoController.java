package com.pbtp1.controller;

import com.pbtp1.auth.AuthService;
import com.pbtp1.service.CarrinhoService;
import com.pbtp1.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {
    private final CarrinhoService carrinhoService;
    private final AuthService authService;

    @GetMapping
    public CarrinhoService.CarrinhoResponse listar(@RequestHeader("Authorization") String authorization) {
        return carrinhoService.listar(authService.requireUser(authorization).getId());
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public CarrinhoService.CarrinhoResponse adicionar(@RequestHeader("Authorization") String authorization,
                                                        @RequestBody CarrinhoService.ItemCarrinhoRequest request) {
        return carrinhoService.adicionar(authService.requireUser(authorization).getId(), request);
    }

    @PutMapping("/itens/{produtoId}")
    public CarrinhoService.CarrinhoResponse atualizar(@PathVariable Long produtoId,
                                                        @RequestHeader("Authorization") String authorization,
                                                        @RequestBody CarrinhoService.ItemCarrinhoRequest request) {
        return carrinhoService.atualizar(authService.requireUser(authorization).getId(), produtoId, request);
    }

    @DeleteMapping("/itens/{produtoId}")
    public CarrinhoService.CarrinhoResponse remover(@PathVariable Long produtoId,
                                                     @RequestHeader("Authorization") String authorization) {
        return carrinhoService.remover(authService.requireUser(authorization).getId(), produtoId);
    }

    @PostMapping("/finalizar")
    public CompraService.CompraResponse finalizar(@RequestHeader("Authorization") String authorization,
                                                   @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return carrinhoService.finalizar(authService.requireUser(authorization).getId(), idempotencyKey);
    }
}
