package com.pbtp1.controller;

import com.pbtp1.model.Produto;
import com.pbtp1.service.ProdutoService;
import com.pbtp1.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final AuthService authService;

    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    @GetMapping("/busca")
    public List<Produto> buscar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Double precoMin,
            @RequestParam(required = false) Double precoMax,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String termo) {

        if (termo != null) return produtoService.buscarPorTermo(termo);
        if (nome != null) return produtoService.buscarPorNome(nome);
        if (precoMin != null && precoMax != null) return produtoService.buscarPorFaixaPreco(precoMin, precoMax);
        if (categoriaId != null) return produtoService.buscarPorCategoria(categoriaId);

        return produtoService.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto salvar(@Valid @RequestBody Produto produto,
                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return produtoService.salvar(produto);
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id,
                             @Valid @RequestBody Produto produto,
                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return produtoService.atualizar(id, produto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id,
                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        produtoService.deletar(id);
    }
}
