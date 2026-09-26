package com.pbtp1.controller;

import com.pbtp1.auth.AuthService;
import com.pbtp1.model.*;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.UsuarioRepository;
import com.pbtp1.service.*;
import com.pbtp1.shared.messaging.EventoProduto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerCoverageTest {
    @Mock private AuthService authService;
    @Mock private CategoriaService categoriaService;
    @Mock private ProdutoService produtoService;
    @Mock private CompraService compraService;
    @Mock private CarrinhoService carrinhoService;
    @Mock private HistoricoService historicoService;
    @Mock private CompraRepository compraRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private AuthController authController;
    private CategoriaController categoriaController;
    private ProdutoController produtoController;
    private CompraController compraController;
    private CarrinhoController carrinhoController;
    private UsuarioController usuarioController;
    private HistoricoController historicoController;
    private ProjectionController projectionController;

    @BeforeEach
    void criarControllers() {
        authController = new AuthController(authService);
        categoriaController = new CategoriaController(categoriaService, authService);
        produtoController = new ProdutoController(produtoService, authService);
        compraController = new CompraController(compraService, authService);
        carrinhoController = new CarrinhoController(carrinhoService, authService);
        usuarioController = new UsuarioController(authService);
        historicoController = new HistoricoController(historicoService);
        projectionController = new ProjectionController(produtoService, compraRepository, usuarioRepository, "segredo-interno");
    }

    @Test
    void authControllerEncaminhaCadastroLoginEUsuarioAtual() {
        AuthService.AuthResponse resposta = new AuthService.AuthResponse("token", new AuthService.UsuarioResponse(
                9L, "Ana", "ana@example.com", "USER", true));
        when(authService.registrar("Ana", "ana@example.com", "senha123")).thenReturn(resposta);
        when(authService.login("ana@example.com", "senha123")).thenReturn(resposta);
        when(authService.requireUser("Bearer token")).thenReturn(usuario(9L, PerfilUsuario.USER));

        assertThat(authController.registrar(new AuthController.CadastroRequest("Ana", "ana@example.com", "senha123")))
                .isSameAs(resposta);
        assertThat(authController.login(new AuthController.LoginRequest("ana@example.com", "senha123"))).isSameAs(resposta);
        assertThat(authController.me("Bearer token").email()).isEqualTo("user9@example.com");
    }

    @Test
    void categoriaControllerEncaminhaConsultasMutacoesEExclusao() {
        Categoria categoria = Categoria.builder().id(3L).nome("Áudio").descricao("Som").build();
        when(categoriaService.listarTodas()).thenReturn(List.of(categoria));
        when(categoriaService.buscarPorId(3L)).thenReturn(categoria);
        when(categoriaService.salvar(categoria)).thenReturn(categoria);
        when(categoriaService.atualizar(3L, categoria)).thenReturn(categoria);
        when(authService.requireAdmin("admin-token")).thenReturn(usuario(1L, PerfilUsuario.ADMIN));

        assertThat(categoriaController.listarTodas()).containsExactly(categoria);
        assertThat(categoriaController.buscarPorId(3L)).isSameAs(categoria);
        assertThat(categoriaController.salvar(categoria, "admin-token")).isSameAs(categoria);
        assertThat(categoriaController.atualizar(3L, categoria, "admin-token")).isSameAs(categoria);
        categoriaController.deletar(3L, "admin-token");
        verify(categoriaService).deletar(3L);
        verify(authService, org.mockito.Mockito.times(3)).requireAdmin("admin-token");
    }

    @Test
    void produtoControllerBuscaPorTodosOsFiltrosEDelegaMutacoes() {
        Produto produto = Produto.builder().id(4L).nome("Fone").preco(BigDecimal.TEN).estoque(2).build();
        when(produtoService.buscarPorTermo("fone")).thenReturn(List.of(produto));
        when(produtoService.buscarPorNome("fone")).thenReturn(List.of(produto));
        when(produtoService.buscarPorFaixaPreco(BigDecimal.ONE, BigDecimal.TEN)).thenReturn(List.of(produto));
        when(produtoService.buscarPorCategoria(3L)).thenReturn(List.of(produto));
        when(produtoService.listarTodos()).thenReturn(List.of(produto));
        when(produtoService.buscarPorId(4L)).thenReturn(produto);
        when(produtoService.salvar(produto)).thenReturn(produto);
        when(produtoService.atualizar(4L, produto)).thenReturn(produto);
        when(authService.requireAdmin("admin-token")).thenReturn(usuario(1L, PerfilUsuario.ADMIN));

        assertThat(produtoController.listarTodos()).containsExactly(produto);
        assertThat(produtoController.buscarPorId(4L)).isSameAs(produto);
        assertThat(produtoController.buscar(null, null, null, null, "fone")).containsExactly(produto);
        assertThat(produtoController.buscar("fone", null, null, null, null)).containsExactly(produto);
        assertThat(produtoController.buscar(null, BigDecimal.ONE, BigDecimal.TEN, null, null)).containsExactly(produto);
        assertThat(produtoController.buscar(null, null, null, 3L, null)).containsExactly(produto);
        assertThat(produtoController.buscar(null, null, null, null, null)).containsExactly(produto);
        assertThat(produtoController.salvar(produto, "admin-token")).isSameAs(produto);
        assertThat(produtoController.atualizar(4L, produto, "admin-token")).isSameAs(produto);
        produtoController.deletar(4L, "admin-token");
        verify(produtoService).deletar(4L);
    }

    @Test
    void compraControllerAplicaIdentidadeEPermissoes() {
        CompraService.CompraRequest request = new CompraService.CompraRequest(List.of(new CompraService.ItemRequest(4L, 1)));
        CompraService.CompraResponse response = compra(8L);
        when(compraService.criar(9L, request, "chave")).thenReturn(response);
        when(compraService.minhas(9L)).thenReturn(List.of(response));
        when(compraService.todas()).thenReturn(List.of(response));
        when(compraService.atualizarStatus(8L, StatusCompra.PAGA)).thenReturn(response);
        when(compraService.cancelar(8L)).thenReturn(response);
        when(authService.requireUser("user-token")).thenReturn(usuario(9L, PerfilUsuario.USER));
        when(authService.requireAdmin("admin-token")).thenReturn(usuario(1L, PerfilUsuario.ADMIN));

        assertThat(compraController.criar("user-token", "chave", request)).isSameAs(response);
        assertThat(compraController.minhas("user-token")).containsExactly(response);
        assertThat(compraController.todas("admin-token")).containsExactly(response);
        assertThat(compraController.atualizarStatus(8L, StatusCompra.PAGA, "admin-token")).isSameAs(response);
        assertThat(compraController.cancelar(8L, "admin-token")).isSameAs(response);
    }

    @Test
    void carrinhoControllerEncaminhaOperacoesComUsuarioAutenticado() {
        CarrinhoService.ItemCarrinhoRequest request = new CarrinhoService.ItemCarrinhoRequest(4L, 2);
        CarrinhoService.CarrinhoResponse response = new CarrinhoService.CarrinhoResponse(List.of(), BigDecimal.ZERO);
        CompraService.CompraResponse compra = compra(8L);
        when(carrinhoService.listar(9L)).thenReturn(response);
        when(carrinhoService.adicionar(9L, request)).thenReturn(response);
        when(carrinhoService.atualizar(9L, 4L, request)).thenReturn(response);
        when(carrinhoService.remover(9L, 4L)).thenReturn(response);
        when(carrinhoService.finalizar(9L, "chave")).thenReturn(compra);
        when(authService.requireUser("user-token")).thenReturn(usuario(9L, PerfilUsuario.USER));

        assertThat(carrinhoController.listar("user-token")).isSameAs(response);
        assertThat(carrinhoController.adicionar("user-token", request)).isSameAs(response);
        assertThat(carrinhoController.atualizar(4L, "user-token", request)).isSameAs(response);
        assertThat(carrinhoController.remover(4L, "user-token")).isSameAs(response);
        assertThat(carrinhoController.finalizar("user-token", "chave")).isSameAs(compra);
    }

    @Test
    void usuarioControllerProtegeListagemEAlteracaoDeAtivo() {
        AuthService.UsuarioAdminResponse user = new AuthService.UsuarioAdminResponse(9L, "Ana", "ana@example.com", "USER", true);
        when(authService.listarUsuarios()).thenReturn(List.of(user));
        when(authService.atualizarAtivo(9L, false)).thenReturn(new AuthService.UsuarioAdminResponse(
                9L, "Ana", "ana@example.com", "USER", false));
        when(authService.requireAdmin("admin-token")).thenReturn(usuario(1L, PerfilUsuario.ADMIN));

        assertThat(usuarioController.listar("admin-token")).containsExactly(user);
        assertThat(usuarioController.atualizarAtivo(9L, false, "admin-token").ativo()).isFalse();
        verify(authService, org.mockito.Mockito.times(2)).requireAdmin("admin-token");
    }

    @Test
    void historicoControllerEncaminhaAsTresConsultas() {
        Produto produto = Produto.builder().id(4L).nome("Fone").build();
        when(historicoService.listarRevisoesProduto(4L)).thenAnswer(invocacao -> List.of("revisao"));
        when(historicoService.listarTodasRevisoes()).thenAnswer(invocacao -> List.of("revisao"));
        when(historicoService.buscarProdutoNaRevisao(4L, 2)).thenReturn(produto);

        assertThat(historicoController.listarRevisoesProduto(4L)).hasSize(1);
        assertThat(historicoController.listarTodasRevisoes()).hasSize(1);
        assertThat(historicoController.buscarProdutoNaRevisao(4L, 2)).isSameAs(produto);
    }

    @Test
    void projectionControllerValidaSecretEProjetaComprasComNomeDoUsuario() {
        when(produtoService.listarTodos()).thenReturn(List.of());
        assertThatThrownBy(() -> projectionController.produtos(null))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(projectionController.produtos("segredo-interno")).isEmpty();
        assertThatThrownBy(() -> projectionController.compras("incorreto"))
                .isInstanceOf(ResponseStatusException.class);

        Usuario comprador = usuario(9L, PerfilUsuario.USER);
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(comprador));
        Compra compra = Compra.builder().id(8L).usuarioId(9L).status(StatusCompra.PAGA)
                .criadaEm(LocalDateTime.now()).demonstracao(false).build();
        compra.getItens().add(ItemCompra.builder().compra(compra).produtoId(4L).nomeProduto("Fone")
                .precoUnitario(BigDecimal.TEN).quantidade(1).build());
        when(compraRepository.findAll()).thenReturn(List.of(compra));

        assertThat(projectionController.compras("segredo-interno")).singleElement()
                .satisfies(evento -> {
                    assertThat(evento.compraId()).isEqualTo(8L);
                    assertThat(evento.nomeUsuario()).isEqualTo(comprador.getNome());
                });
    }

    private Usuario usuario(Long id, PerfilUsuario perfil) {
        return Usuario.builder().id(id).nome("User " + id).email("user" + id + "@example.com")
                .perfil(perfil).ativo(true).build();
    }

    private CompraService.CompraResponse compra(Long id) {
        return new CompraService.CompraResponse(id, 9L, "CRIADA", BigDecimal.TEN, LocalDateTime.now(), List.of());
    }
}
