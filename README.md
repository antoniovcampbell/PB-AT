# PB-AT

Marketplace demonstrativo com catálogo, autenticação, compras e avaliações verificadas, integrado por eventos JSON via RabbitMQ.

## Arquitetura

| Componente | Porta | Responsabilidade |
|---|---:|---|
| `catalogo` | 8090 | Produtos, categorias, autenticação, compras e publicação de eventos |
| `avaliacoes` | 8081 | Avaliações, projeção de produtos e validação de compras |
| `frontend` | 5173 | Interface React servida pelo Nginx |
| PostgreSQL | 5432 | Persistência dos microsserviços |
| RabbitMQ | 5672 | Mensageria entre os microsserviços |

O módulo `shared` concentra DTOs, contratos de mensagens e o serviço comum de tokens assinados. Os serviços utilizam H2 em desenvolvimento e PostgreSQL nos ambientes distribuídos.

## Funcionalidades

- Catálogo com busca, filtros por categoria, estado e preço, além de ordenação.
- Categorias e estoque associados aos produtos.
- Estados de produto: `ATIVO`, `ESTOQUE_BAIXO`, `ESGOTADO` e `INATIVO`.
- Registro e login com perfis `USER` e `ADMIN`.
- Compras persistidas com itens, total e estado de processamento.
- Avaliações associadas a usuários e produtos comprados.
- Operações administrativas para produtos, categorias e compras.
- Seed demonstrativo com 12 produtos em 5 categorias.

## Frontend

O frontend apresenta o catálogo, o fluxo de autenticação, a área de compras, as avaliações e o painel administrativo. O Nginx encaminha as rotas de produtos, categorias, autenticação, compras e avaliações para os respectivos microsserviços.

## APIs

O serviço `catalogo` disponibiliza recursos de autenticação, produtos, categorias e compras. O serviço `avaliacoes` disponibiliza listagem, médias e gerenciamento de avaliações. Operações protegidas utilizam tokens Bearer e autorização baseada no perfil do usuário.

## Mensageria

O catálogo publica eventos de criação, atualização e exclusão de produtos. Compras publicam eventos próprios e atualizações de estoque. O serviço de avaliações consome esses eventos para manter suas projeções e verificar a elegibilidade de avaliações.

## Testes

O backend possui testes de repositórios, serviços, controladores e contexto de aplicação. O frontend possui validação de lint e build de produção. Os testes de backend usam H2 e os ambientes distribuídos utilizam PostgreSQL e RabbitMQ.

## Observabilidade

Os microsserviços expõem healthchecks, probes de disponibilidade, métricas Prometheus, traces OTLP e logs estruturados. Prometheus, Grafana, Loki, Promtail e Jaeger integram a camada de monitoramento e diagnóstico distribuído.

## Kubernetes

Os manifests em `k8s/` descrevem a execução distribuída no namespace `pb-at`.

- `Namespace` fornece isolamento lógico dos recursos.
- `ConfigMap` concentra configurações não sensíveis.
- `Secret` fornece credenciais e a assinatura dos tokens.
- `Deployment` representa os componentes stateless da aplicação.
- `Service` fornece descoberta e comunicação interna.
- `StatefulSet` representa a persistência do PostgreSQL.
- `Ingress` expõe o frontend e integra o roteamento externo.
- `HorizontalPodAutoscaler` representa o escalonamento horizontal dos microsserviços.
- Probes representam os estados de inicialização, atividade e prontidão dos pods.
- Kustomize reúne os recursos do ambiente Kubernetes.

## CI/CD

Os workflows do GitHub Actions cobrem testes Maven, lint, build do frontend e publicação das imagens dos serviços no GitHub Container Registry. O conjunto de manifests Kubernetes representa o ambiente de execução da aplicação.

## Execução Automatizada

```bash
docker compose up --build -d
```

### Usuários e Senhas Demonstrativos

| Acesso | Usuário | Senha |
|---|---|---|
| Administrador da aplicação | `admin@pbat.local` | `admin123` |
| Usuário da aplicação | `user@pbat.local` | `user123` |
| RabbitMQ | `antonio` | `admin123` |
| PostgreSQL | `antonio` | `admin123` |

## Stack

- Java 21, Spring Boot 4, Spring Data JPA, Hibernate Envers e Spring AMQP.
- PostgreSQL, H2 e RabbitMQ.
- React 19, Vite e Nginx.
- Docker Compose e Kubernetes.
- Prometheus, Grafana, Loki, Promtail e Jaeger.
