package com.pbtp1.avaliacoes.controller;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.service.AvaliacaoService;
import com.pbtp1.avaliacoes.auth.AuthClaimsService;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import com.pbtp1.shared.auth.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    private final AuthClaimsService authClaimsService;
    private final CompraProdutoRepository compraProdutoRepository;

    @GetMapping
    public List<AvaliacaoDTO> listar(
            @RequestParam(required = false) Long produtoId,
            @RequestParam(required = false) Integer nota,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (produtoId != null && nota != null) {
            return avaliacaoService.listarPorProdutoENota(produtoId, nota);
        }
        if (produtoId != null) {
            return avaliacaoService.listarPorProduto(produtoId);
        }
        authClaimsService.requireAdmin(authorization);
        return avaliacaoService.listarTodas();
    }

    @GetMapping("/minhas")
    public List<AvaliacaoDTO> listarMinhas(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        TokenService.Claims claims = authClaimsService.require(authorization);
        return avaliacaoService.listarDoUsuario(claims.id());
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
    public AvaliacaoDTO salvar(@Valid @RequestBody CriarAvaliacaoRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        TokenService.Claims claims = authClaimsService.require(authorization);
        boolean compraValida = request.compraId() != null
                && compraProdutoRepository.findByCompraIdAndProdutoIdAndUsuarioIdAndAtivaTrue(
                        request.compraId(), request.produtoId(), claims.id()).isPresent();
        if (!compraValida) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "A avaliação precisa corresponder a um produto de uma compra ativa sua");
        Avaliacao avaliacao = Avaliacao.builder()
                .compraId(request.compraId())
                .produtoId(request.produtoId())
                .nota(request.nota())
                .comentario(request.comentario())
                .build();
        avaliacao.setUsuarioId(claims.id());
        avaliacao.setNomeUsuario(claims.name());
        try {
            return avaliacaoService.salvar(avaliacao, claims.id());
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este produto desta compra já foi avaliado");
        }
    }

    public record CriarAvaliacaoRequest(
            @NotNull Long compraId,
            @NotNull Long produtoId,
            @NotNull @Min(1) @Max(5) Integer nota,
            String comentario) {
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
        return authClaimsService.require(authorization);
    }

    private void autorizarDonoOuAdmin(Long id, String authorization) {
        TokenService.Claims claims = autenticar(authorization);
        if (authClaimsService.isAdmin(claims)) {
            return;
        }
        if (!claims.id().equals(avaliacaoService.usuarioId(id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode alterar suas próprias avaliações");
        }
    }
}
