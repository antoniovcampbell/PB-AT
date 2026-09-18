package com.pbtp1.avaliacoes.controller;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.service.AvaliacaoService;
import com.pbtp1.avaliacoes.auth.AuthClaimsService;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import com.pbtp1.shared.auth.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Autowired(required = false)
    private AuthClaimsService authClaimsService;

    @Autowired(required = false)
    private CompraProdutoRepository compraProdutoRepository;

    @GetMapping
    public List<AvaliacaoDTO> listar(
            @RequestParam(required = false) Long produtoId,
            @RequestParam(required = false) Integer nota) {

        if (produtoId != null && nota != null) {
            return avaliacaoService.listarPorProdutoENota(produtoId, nota);
        }
        if (produtoId != null) {
            return avaliacaoService.listarPorProduto(produtoId);
        }
        return avaliacaoService.listarTodas();
    }

    @GetMapping("/{id}")
    public AvaliacaoDTO buscarPorId(@PathVariable Long id) {
        return avaliacaoService.buscarPorId(id);
    }

    @GetMapping("/produtos/{produtoId}")
    public List<AvaliacaoDTO> listarPorProduto(@PathVariable Long produtoId) {
        return avaliacaoService.listarPorProduto(produtoId);
    }

    @GetMapping("/produtos/{produtoId}/media")
    public AvaliacaoService.MediaAvaliacao calcularMedia(@PathVariable Long produtoId) {
        return avaliacaoService.calcularMediaPorProduto(produtoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoDTO salvar(@Valid @RequestBody Avaliacao avaliacao,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        TokenService.Claims claims = autenticar(authorization);
        if (claims != null) {
            if (compraProdutoRepository != null && !compraProdutoRepository.existsByUsuarioIdAndProdutoId(claims.id(), avaliacao.getProdutoId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compre o produto antes de avaliá-lo");
            }
            avaliacao.setUsuarioId(claims.id());
            avaliacao.setNomeUsuario(claims.name());
        }
        return avaliacaoService.salvar(avaliacao);
    }

    @PutMapping("/{id}")
    public AvaliacaoDTO atualizar(@PathVariable Long id,
                                  @Valid @RequestBody Avaliacao avaliacao,
                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        autorizarDonoOuAdmin(id, authorization);
        return avaliacaoService.atualizar(id, avaliacao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id,
                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        autorizarDonoOuAdmin(id, authorization);
        avaliacaoService.deletar(id);
    }

    private TokenService.Claims autenticar(String authorization) {
        if (authClaimsService == null) {
            return null;
        }
        return authClaimsService.require(authorization);
    }

    private void autorizarDonoOuAdmin(Long id, String authorization) {
        TokenService.Claims claims = autenticar(authorization);
        if (claims == null || authClaimsService.isAdmin(claims)) {
            return;
        }
        if (!claims.id().equals(avaliacaoService.usuarioId(id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode alterar suas próprias avaliações");
        }
    }
}
