package com.pbtp1.service;

import com.pbtp1.model.CarrinhoItem;
import com.pbtp1.model.Produto;
import com.pbtp1.model.StatusProduto;
import com.pbtp1.repository.CarrinhoItemRepository;
import com.pbtp1.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {
    @Mock
    private CarrinhoItemRepository carrinhoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CompraService compraService;

    @InjectMocks
    private CarrinhoService carrinhoService;

    @Test
    void listarMontaItensETotalDoCarrinho() {
        when(carrinhoRepository.findByUsuarioIdOrderByIdAsc(7L)).thenReturn(List.of(
                item(7L, 10L, 2), item(7L, 11L, 1)));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto(10L, "Teclado", "12.50", 8)));
        when(produtoRepository.findById(11L)).thenReturn(Optional.of(produto(11L, "Cabo", "5.00", 10)));

        CarrinhoService.CarrinhoResponse resposta = carrinhoService.listar(7L);

        assertThat(resposta.itens()).hasSize(2);
        assertThat(resposta.itens().get(0).nomeProduto()).isEqualTo("Teclado");
        assertThat(resposta.total()).isEqualByComparingTo("30.00");
    }

    @Test
    void adicionarCriaNovoItemEIncrementaItemExistente() {
        Produto produto = produto(10L, "Teclado", "12.50", 5);
        AtomicReference<CarrinhoItem> persistido = new AtomicReference<>();
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 10L))
                .thenAnswer(invocacao -> Optional.ofNullable(persistido.get()));
        when(carrinhoRepository.save(any(CarrinhoItem.class))).thenAnswer(invocacao -> {
            CarrinhoItem salvo = invocacao.getArgument(0);
            persistido.set(salvo);
            return salvo;
        });
        when(carrinhoRepository.findByUsuarioIdOrderByIdAsc(7L)).thenAnswer(invocacao ->
                Optional.ofNullable(persistido.get()).map(List::of).orElseGet(List::of));

        CarrinhoService.CarrinhoResponse resposta = carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 1));

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.itens().getFirst().quantidade()).isEqualTo(1);
        CarrinhoItem itemExistente = item(7L, 10L, 1);
        persistido.set(itemExistente);
        CarrinhoService.CarrinhoResponse atualizado = carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 2));
        assertThat(atualizado.itens().getFirst().quantidade()).isEqualTo(3);
        verify(carrinhoRepository, times(2)).save(any(CarrinhoItem.class));
    }

    @Test
    void adicionarRejeitaQuantidadeInvalidaEProdutoIndisponivel() {
        assertThatThrownBy(() -> carrinhoService.adicionar(7L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 0)))
                .isInstanceOf(IllegalArgumentException.class);

        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto(10L, "Teclado", "12.50", 1)));
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 10L)).thenReturn(Optional.of(item(7L, 10L, 1)));
        assertThatThrownBy(() -> carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sem estoque");
        verify(carrinhoRepository, never()).save(any());
    }

    @Test
    void atualizarAlteraQuantidadeERejeitaItemAusenteOuProdutoSemEstoque() {
        Produto produto = produto(10L, "Teclado", "12.50", 4);
        CarrinhoItem item = item(7L, 10L, 1);
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 10L)).thenReturn(Optional.of(item));
        when(carrinhoRepository.save(item)).thenReturn(item);
        when(carrinhoRepository.findByUsuarioIdOrderByIdAsc(7L)).thenReturn(List.of(item));

        assertThat(carrinhoService.atualizar(7L, 10L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 3)).itens().getFirst().quantidade()).isEqualTo(3);

        when(produtoRepository.findById(99L)).thenReturn(Optional.of(produto(99L, "Mouse", "10.00", 4)));
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> carrinhoService.atualizar(7L, 99L,
                new CarrinhoService.ItemCarrinhoRequest(99L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está no carrinho");

        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> carrinhoService.atualizar(7L, 10L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sem estoque");
    }

    @Test
    void removerDeletaSeExistirEIgnoraItemAusente() {
        CarrinhoItem item = item(7L, 10L, 1);
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 10L)).thenReturn(Optional.of(item));
        when(carrinhoRepository.findByUsuarioIdAndProdutoId(7L, 11L)).thenReturn(Optional.empty());
        when(carrinhoRepository.findByUsuarioIdOrderByIdAsc(7L)).thenReturn(List.of());

        carrinhoService.remover(7L, 10L);
        carrinhoService.remover(7L, 11L);

        verify(carrinhoRepository).delete(item);
    }

    @Test
    void finalizarRejeitaCarrinhoVazioEFinalizaEEsfaziaCarrinho() {
        when(carrinhoRepository.findByUsuarioIdOrderByIdAsc(7L)).thenReturn(List.of(), List.of(item(7L, 10L, 2)));
        assertThatThrownBy(() -> carrinhoService.finalizar(7L, "chave"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");

        CompraService.CompraResponse compra = new CompraService.CompraResponse(20L, 7L, "CRIADA",
                BigDecimal.valueOf(25), null, List.of());
        when(compraService.criar(any(), any(), any())).thenReturn(compra);
        assertThat(carrinhoService.finalizar(7L, "chave")).isEqualTo(compra);
        verify(carrinhoRepository).deleteByUsuarioId(7L);
    }

    @Test
    void rejeitaProdutoInexistenteENaoDisponivel() {
        when(produtoRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(10L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Produto não encontrado");

        when(produtoRepository.findById(11L)).thenReturn(Optional.of(Produto.builder()
                .id(11L).nome("Indisponível").preco(BigDecimal.TEN).estoque(10).status(StatusProduto.INATIVO).build()));
        assertThatThrownBy(() -> carrinhoService.adicionar(7L,
                new CarrinhoService.ItemCarrinhoRequest(11L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sem estoque");
    }

    private CarrinhoItem item(Long usuarioId, Long produtoId, int quantidade) {
        return CarrinhoItem.builder().usuarioId(usuarioId).produtoId(produtoId).quantidade(quantidade).build();
    }

    private Produto produto(Long id, String nome, String preco, int estoque) {
        return Produto.builder().id(id).nome(nome).preco(new BigDecimal(preco)).estoque(estoque)
                .status(StatusProduto.ATIVO).build();
    }
}
