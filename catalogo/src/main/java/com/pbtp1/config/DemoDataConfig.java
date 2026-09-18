package com.pbtp1.config;

import com.pbtp1.auth.AuthService;
import com.pbtp1.model.*;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.ProdutoRepository;
import com.pbtp1.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataConfig {
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;
    private final AuthService authService;

    @Bean
    public CommandLineRunner demoDataSeeder() {
        return args -> {
            authService.criarInicial("Administrador", "admin@pbat.local", "admin123", PerfilUsuario.ADMIN);
            authService.criarInicial("Cliente demonstração", "user@pbat.local", "user123", PerfilUsuario.USER);

            Map<String, Categoria> categorias = new LinkedHashMap<>();
            categorias.put("Eletrônicos", categoria("Eletrônicos", "Tecnologia para o dia a dia"));
            categorias.put("Casa", categoria("Casa", "Conforto e utilidades"));
            categorias.put("Mobilidade", categoria("Mobilidade", "Produtos para ir mais longe"));
            categorias.put("Escritório", categoria("Escritório", "Produtividade e organização"));
            categorias.put("Áudio", categoria("Áudio", "Som para todos os momentos"));

            produto("Notebook Ultra 14", "Notebook leve com 16GB de RAM e SSD de 512GB", 4299.90, categorias.get("Escritório"), 12, StatusProduto.ATIVO);
            produto("Fone Noise Cancelling", "Fone Bluetooth com cancelamento ativo de ruído", 799.90, categorias.get("Áudio"), 2, StatusProduto.ESTOQUE_BAIXO);
            produto("Camera Mirrorless X1", "Câmera mirrorless 24MP para fotografia e vídeo", 3599.00, categorias.get("Eletrônicos"), 8, StatusProduto.ATIVO);
            produto("Smartwatch Pulse", "Relógio inteligente com monitoramento de atividades", 649.50, categorias.get("Eletrônicos"), 0, StatusProduto.ESGOTADO);
            produto("Cafeteira Espresso Pro", "Cafeteira automática para espresso e cappuccino", 1199.00, categorias.get("Casa"), 0, StatusProduto.INATIVO);
            produto("Mochila Executiva", "Mochila resistente para notebook e acessórios", 289.90, categorias.get("Escritório"), 15, StatusProduto.ATIVO);
            produto("Tablet Horizon 11", "Tela ampla, bateria para o dia inteiro e capa inclusa", 1899.00, categorias.get("Eletrônicos"), 20, StatusProduto.ATIVO);
            produto("Console NovaPlay", "Console compacto com controle sem fio", 2499.00, categorias.get("Eletrônicos"), 3, StatusProduto.ESTOQUE_BAIXO);
            produto("Aspirador Orbital", "Aspirador vertical silencioso para a casa", 899.00, categorias.get("Casa"), 0, StatusProduto.ESGOTADO);
            produto("Bicicleta Urbana", "Bicicleta híbrida para deslocamentos urbanos", 2199.00, categorias.get("Mobilidade"), 0, StatusProduto.INATIVO);
            produto("Monitor Studio 27", "Monitor QHD com cores precisas para criação", 1799.00, categorias.get("Escritório"), 7, StatusProduto.ATIVO);
            produto("Teclado Mecânico Air", "Teclado compacto com switches silenciosos", 449.90, categorias.get("Escritório"), 4, StatusProduto.ATIVO);
        };
    }

    private Categoria categoria(String nome, String descricao) {
        return categoriaRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder().nome(nome).descricao(descricao).build()));
    }

    private void produto(String nome, String descricao, double preco, Categoria categoria, int estoque, StatusProduto status) {
        Produto existente = produtoRepository.findByNomeIgnoreCase(nome).orElse(null);
        Produto dados = Produto.builder().nome(nome).descricao(descricao).preco(preco).categoria(categoria).estoque(estoque).status(status).build();
        if (existente == null) {
            produtoService.salvar(dados);
        } else if (existente.getStatus() == null || existente.getEstoque() == null || existente.getCategoria() == null) {
            produtoService.atualizar(existente.getId(), dados);
        }
    }
}
