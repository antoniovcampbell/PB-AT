# Plano de Melhorias Criticas

## Objetivo

Reduzir primeiro os riscos que podem impedir o uso do checkout, permitir acesso administrativo indevido, causar perda de estoque ou deixar os servicos inconsistentes.

## Estado Atual

- O frontend e servido por Nginx, mas os endpoints de carrinho e usuarios ainda nao estao roteados pelo proxy.
- O ambiente de demonstracao cria credenciais administrativas previsiveis e os ambientes implantados habilitam o seed demo.
- O checkout faz leitura e gravacao simples de estoque, sem lock ou idempotencia.
- A compra aceita transicoes arbitrarias de status.
- A elegibilidade de avaliacao nao trata cancelamento de compra de forma completa.
- O schema usa `ddl-auto=update` e nao possui migrations versionadas.
- O outbox publica no RabbitMQ sem confirmacao explicita do broker, DLQ ou politica operacional completa.
- A cobertura de testes ainda nao inclui concorrencia, checkout, carrinho, autenticacao integrada, outbox e migrations.

## Progresso da Execucao

- [x] Proxy do frontend para carrinho e usuarios.
- [x] Seed demo desabilitado no Kubernetes.
- [x] Perfil `production` adicionado aos deployments.
- [x] Secrets versionados substituidos por placeholders que bloqueiam a inicializacao sem configuracao real.
- [x] Checkout com `BigDecimal`, controle otimista de estoque e `Idempotency-Key`.
- [x] Transicoes basicas de status e teste de requisicao duplicada.
- [x] Elegibilidade de avaliacao revogada apos cancelamento.
- [x] Primeira migration versionada para a projecao de compras.
- [x] Migrations Flyway versionadas para os schemas de catalogo e avaliacoes, incluindo ajustes de compatibilidade com bancos legados.
- [x] Migrations aplicadas e verificadas no PostgreSQL local: catalogo v2 e avaliacoes v2.
- [x] Perfil de producao com `ddl-auto=validate`.
- [x] Publicacao outbox aguardando publisher confirm e retorno de mensagem nao roteada.
- [x] Retry de consumidores com DLQ para eventos que falham apos novas tentativas.
- [x] Teste de concorrencia garantindo uma unica baixa do ultimo item.
- [x] Conflitos de estoque e persistencia expostos como HTTP 409.
- [x] Migrations versionadas e validadas em PostgreSQL.

## Prioridade P0

### 1. Corrigir o proxy do frontend

Arquivos principais: `frontend/nginx.conf`.

- Adicionar proxy para `/api/carrinho` apontando para `catalogo:8090`.
- Adicionar proxy para `/api/usuarios` apontando para `catalogo:8090`.
- Criar smoke check que valide as rotas pelo mesmo host usado pelo navegador.

Aceite: carrinho, checkout e gestao de usuarios funcionam pelo container frontend.

### 2. Remover credenciais e secrets previsiveis

Arquivos principais: `DemoDataConfig.java`, `docker-compose.yml`, `k8s/configmap.yaml`, `k8s/secret.yaml` e `TokenService.java`.

- Desabilitar seed demo no Kubernetes e manter seed apenas em desenvolvimento.
- Remover credenciais administrativas fixas de ambientes implantados.
- Remover valores default de secrets em producao.
- Tornar `APP_AUTH_SECRET` e `APP_INTERNAL_SECRET` obrigatorios.
- Rotacionar secrets atuais de JWT, PostgreSQL e RabbitMQ.

Aceite: a aplicacao falha de forma clara quando secrets de producao nao existem e nenhum administrador conhecido e criado automaticamente.

## Prioridade P1

### 3. Tornar checkout seguro e idempotente

Arquivos principais: `CompraService.java`, `Produto.java`, `CompraController.java` e `CarrinhoService.java`.

- Trocar valores monetarios de `Double` para `BigDecimal`.
- Usar `@Version`, lock pessimista ou update condicional para estoque.
- Validar estoque com `@PositiveOrZero` e DTOs com `@Valid`.
- Adicionar `Idempotency-Key` para finalizacao de compra.
- Impedir pedidos duplicados durante retries.
- Testar duas compras concorrentes para o mesmo produto.

Aceite: estoque nunca fica negativo, uma chave idempotente gera uma unica compra e concorrencia nao vende alem do estoque.

### 4. Validar transicoes e cancelamentos

Arquivos principais: `CompraService.java`, `StatusCompra.java` e eventos compartilhados.

- Definir transicoes permitidas de status.
- Impedir cancelamento de pedidos entregues.
- Tornar cancelamento idempotente.
- Publicar evento `compra.cancelada` separado de `compra.criada`.
- Restaurar estoque apenas uma vez.

Aceite: nenhum pedido pode regredir de estado e nenhum cancelamento duplica reposicao de estoque.

### 5. Corrigir elegibilidade de avaliacao

Arquivos principais: `CompraProduto.java`, `CompraProdutoListener.java`, `AvaliacaoController.java` e `ProjectionReconciliationService.java`.

- Persistir `compraId` e status da compra na projecao.
- Suportar varias compras do mesmo produto pelo mesmo usuario.
- Revogar elegibilidade quando todas as compras validas forem canceladas.
- Validar usuario ativo e perfil atual no servico de avaliacoes.

Aceite: apenas compras validas permitem avaliacao, inclusive depois de cancelamentos e reprocessamento de eventos.

## Prioridade P2

### 6. Adotar migrations

- Adicionar Flyway ou Liquibase aos servicos.
- Criar migrations para carrinho, usuarios, compras demonstrativas e projecoes.
- Usar `ddl-auto=validate` em producao.

### 7. Fortalecer RabbitMQ e outbox

- Habilitar publisher confirms e returns.
- Adicionar `eventId` e consumidores idempotentes.
- Configurar retry, DLQ e metricas de backlog.
- Adicionar volume persistente ao RabbitMQ no Kubernetes.

### 8. Melhorar autorizacao e abuso

- Usar tokens curtos com refresh ou introspeccao de sessao.
- Aplicar rate limit no login.
- Limitar tamanho de payloads.
- Proteger historico, actuator e console H2 em producao.
- Considerar cookie `HttpOnly`, `Secure` e `SameSite` em vez de `localStorage`.

### 9. Testes e entrega

- Adicionar testes HTTP de autenticacao e autorizacao.
- Adicionar testes de carrinho, checkout, cancelamento e idempotencia.
- Usar Testcontainers com PostgreSQL e RabbitMQ.
- Adicionar teste de contrato do Nginx.
- Fazer o CD depender do sucesso do CI.
- Fixar imagens por versao ou digest.

## Sequencia de Execucao

1. Corrigir Nginx e validar as rotas do frontend.
2. Remover seed e secrets inseguros de producao.
3. Corrigir estoque, dinheiro e idempotencia do checkout.
4. Implementar maquina de estados e cancelamento correto.
5. Corrigir a projecao de compras e a elegibilidade de avaliacoes.
6. Introduzir migrations.
7. Fortalecer RabbitMQ, observabilidade e pipeline.
8. Completar testes de integracao e concorrencia.

## Regra de Trabalho

Cada etapa deve ser implementada em mudanca pequena, validada por testes e revisada antes de iniciar a seguinte. As alteracoes existentes no workspace devem ser preservadas e nao devem ser revertidas automaticamente.
