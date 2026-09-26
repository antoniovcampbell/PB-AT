package com.pbtp1.controller;

import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.UsuarioRepository;
import com.pbtp1.model.Usuario;
import com.pbtp1.service.ProdutoService;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.EventoProduto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/internal/projection")
public class ProjectionController {
    private final ProdutoService produtoService;
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final String internalSecret;

    public ProjectionController(ProdutoService produtoService,
                                 CompraRepository compraRepository,
                                 UsuarioRepository usuarioRepository,
                                 @Value("${app.internal.secret:pb-at-internal-secret}") String internalSecret) {
        this.produtoService = produtoService;
        this.compraRepository = compraRepository;
        this.usuarioRepository = usuarioRepository;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/products")
    @Transactional(readOnly = true)
    public List<EventoProduto> produtos(@RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        validarSecret(secret);
        return produtoService.listarTodos().stream()
                .map(produto -> produtoService.eventoAtual(produto, "SINCRONIZADO"))
                .toList();
    }

    @GetMapping("/purchases")
    @Transactional(readOnly = true)
    public List<EventoCompra> compras(@RequestHeader(value = "X-Internal-Secret", required = false) String secret) {
        validarSecret(secret);
        return compraRepository.findAll().stream()
                        .flatMap(compra -> compra.getItens().stream()
                                .map(item -> new EventoCompra(compra.getId(), compra.getUsuarioId(), item.getProdutoId(), compra.getStatus().name(), compra.getCriadaEm(),
                                Boolean.TRUE.equals(compra.getDemonstracao()),
                                usuarioRepository.findById(compra.getUsuarioId()).map(Usuario::getNome).orElse("Cliente"))))
                .toList();
    }

    private void validarSecret(String recebido) {
        if (recebido == null || !MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8), recebido.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso interno não autorizado");
        }
    }
}
