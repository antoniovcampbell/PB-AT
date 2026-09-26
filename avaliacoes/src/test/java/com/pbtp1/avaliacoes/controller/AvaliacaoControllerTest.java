package com.pbtp1.avaliacoes.controller;

import com.pbtp1.avaliacoes.service.AvaliacaoService;
import com.pbtp1.avaliacoes.auth.AuthClaimsService;
import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.auth.TokenService;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvaliacaoController.class)
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @MockitoBean
    private AuthClaimsService authClaimsService;

    @MockitoBean
    private CompraProdutoRepository compraProdutoRepository;

    private final AvaliacaoDTO avaliacaoDTO = new AvaliacaoDTO(
            1L, 10L, 20L, "Ana", 5, "Ótimo", LocalDateTime.now());

    @BeforeEach
    void configurarAutorizacao() {
        TokenService.Claims admin = new TokenService.Claims(1L, "admin@pbat.local", "Admin", "ADMIN", Long.MAX_VALUE);
        when(authClaimsService.require(any())).thenReturn(admin);
        when(authClaimsService.requireAdmin(any())).thenReturn(admin);
        when(authClaimsService.isAdmin(any())).thenReturn(true);
        when(compraProdutoRepository.findByCompraIdAndProdutoIdAndUsuarioIdAndAtivaTrue(20L, 10L, 1L))
                .thenReturn(Optional.of(CompraProduto.builder().compraId(20L).produtoId(10L).usuarioId(1L).ativa(true).build()));
    }

    @Test
    void deveListarAvaliacoesPorProduto() throws Exception {
        when(avaliacaoService.listarPorProduto(10L))
                .thenReturn(List.of(avaliacaoDTO));

        mockMvc.perform(get("/avaliacoes/produtos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeUsuario").value("Ana"))
                .andExpect(jsonPath("$[0].nota").value(5));
    }

    @Test
    void deveListarTodasAvaliacoes() throws Exception {
        when(avaliacaoService.listarTodas()).thenReturn(List.of(avaliacaoDTO));

        mockMvc.perform(get("/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveExigirAdminParaListarTodas() throws Exception {
        when(authClaimsService.requireAdmin(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(get("/avaliacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBuscarAvaliacaoPorId() throws Exception {
        when(avaliacaoService.buscarPorId(1L)).thenReturn(avaliacaoDTO);

        mockMvc.perform(get("/avaliacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(10))
                .andExpect(jsonPath("$.nomeUsuario").value("Ana"));
    }

    @Test
    void deveCalcularMedia() throws Exception {
        when(avaliacaoService.calcularMediaPorProduto(10L))
                .thenReturn(new AvaliacaoService.MediaAvaliacao(10L, 4.5, 2L));

        mockMvc.perform(get("/avaliacoes/produtos/10/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(4.5))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void deveCriarAvaliacaoValida() throws Exception {
        when(avaliacaoService.salvar(org.mockito.ArgumentMatchers.any(), eq(1L)))
                .thenReturn(avaliacaoDTO);

         mockMvc.perform(post("/avaliacoes").header("Authorization", "Bearer test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "compraId": 20,
                                  "nomeUsuario": "Ana",
                                  "nota": 5,
                                  "comentario": "Ótimo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    void deveRejeitarAvaliacaoComNotaInvalida() throws Exception {
         mockMvc.perform(post("/avaliacoes").header("Authorization", "Bearer test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "nomeUsuario": "Ana",
                                  "nota": 6
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirLoginParaCriarAvaliacao() throws Exception {
        when(authClaimsService.require(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"produtoId":10,"compraId":20,"nomeUsuario":"Ana","nota":5}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAtualizarAvaliacao() throws Exception {
        when(avaliacaoService.atualizar(anyLong(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(avaliacaoDTO);

         mockMvc.perform(put("/avaliacoes/1").header("Authorization", "Bearer test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "produtoId": 10,
                                  "nomeUsuario": "Ana",
                                  "nota": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveDeletarAvaliacao() throws Exception {
        mockMvc.perform(delete("/avaliacoes/1").header("Authorization", "Bearer test"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveImpedirUsuarioDeExcluirAvaliacaoDeOutraPessoa() throws Exception {
        TokenService.Claims usuario = new TokenService.Claims(2L, "user@pbat.local", "User", "USER", Long.MAX_VALUE);
        when(authClaimsService.require(any())).thenReturn(usuario);
        when(authClaimsService.isAdmin(any())).thenReturn(false);
        when(avaliacaoService.usuarioId(1L)).thenReturn(9L);

        mockMvc.perform(delete("/avaliacoes/1").header("Authorization", "Bearer test"))
                .andExpect(status().isForbidden());
    }
}
