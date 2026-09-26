# PB-AT

Marketplace demonstrativo com catálogo, autenticação, carrinho e compras; clientes podem publicar avaliações verificadas para cada compra. Os microsserviços se integram por eventos JSON via RabbitMQ.

## Arquitetura

| Componente | Porta | Responsabilidade |
|---|---:|---|
| `catalogo` | 8090 | Produtos, categorias, autenticação, compras e publicação de eventos |
| `avaliacoes` | 8081 | Avaliações, projeção de produtos e validação de compras |
| `frontend` | 5173 | Interface React servida pelo Nginx |
| PostgreSQL | 5432 (rede Docker) | Persistência dos microsserviços; não publicado no host pelo Compose |
| RabbitMQ | 5672 (rede Docker), 15672 (host) | Mensageria; console de gerenciamento em `http://localhost:15672` |

O módulo `shared` concentra DTOs, contratos de mensagens e o serviço comum de tokens assinados. Em execução local direta, os serviços usam H2 por padrão; o Docker Compose e os ambientes Kubernetes usam PostgreSQL. No Compose, os serviços se conectam ao banco e ao RabbitMQ pelos nomes internos `postgres` e `rabbitmq`.

## Funcionalidades

- Catálogo com busca, filtros por categoria, estado e preço, além de ordenação.
- Categorias e estoque associados aos produtos.
- Estados de produto: `ATIVO`, `ESTOQUE_BAIXO`, `ESGOTADO` e `INATIVO`.
- Registro e login com perfis `USER` e `ADMIN`.
- Catálogo demonstrativo com 50 produtos, distribuídos em 5 categorias (10 por categoria).
- Compras idempotentes com controle de estoque e valores monetários em `BigDecimal`.
- Migrations versionadas com Flyway; o perfil `production` valida o schema com `ddl-auto=validate`.
- Uma avaliação por produto de cada compra; o formulário fica em “Minhas compras”.
- A página de produto apresenta as avaliações com filtro por nota, busca textual e lista com rolagem.
- No ambiente demo, cada produto recebe de 5 a 15 avaliações demonstrativas, com notas variadas de 1 a 5 estrelas.
- Painel administrativo com indicadores e gestão de produtos, categorias, usuários, compras e avaliações.

## Frontend

O frontend apresenta o catálogo, autenticação, carrinho, “Minhas compras”, páginas de produto com avaliações e painel administrativo. As avaliações são publicadas a partir da compra correspondente em “Minhas compras”. O Nginx encaminha `/api/produtos`, `/api/categorias`, `/api/auth`, `/api/compras`, `/api/carrinho` e `/api/usuarios` para `catalogo`, e `/api/avaliacoes` para `avaliacoes`. A resolução dinâmica de DNS do Docker permite que o proxy acompanhe a recriação dos containers.

## APIs

O serviço `catalogo` disponibiliza autenticação, produtos, categorias, compras, carrinho e usuários. O serviço `avaliacoes` disponibiliza avaliações públicas por produto, médias e avaliações do usuário autenticado. A criação de uma avaliação exige `compraId` e `produtoId` de uma compra ativa pertencente ao usuário; uma segunda avaliação do mesmo produto na mesma compra retorna HTTP `409`. Operações protegidas utilizam tokens Bearer e autorização baseada no perfil do usuário.

## Mensageria

O catálogo usa outbox com publisher confirms e publica eventos de criação, atualização e exclusão de produtos, além de compras e atualizações de estoque. O serviço `avaliacoes` consome esses eventos para manter suas projeções, atualizar nomes dos compradores e verificar a elegibilidade por compra. Consumidores têm retry e filas de dead letter; a reconciliação periódica sincroniza as projeções com o catálogo.

## Testes

O backend possui testes de repositórios, serviços, controladores, idempotência, concorrência de estoque e consumidores de eventos. Os testes usam H2. O frontend tem lint, build de produção e verificação das rotas do Nginx.

```bash
./mvnw -q verify
cd frontend
npm ci
npm run lint
npm run check:nginx
npm run build
```

O ciclo `verify` aplica o limite mínimo de 80% de cobertura de linhas por módulo e gera relatórios JaCoCo em `shared/target/site/jacoco/`, `catalogo/target/site/jacoco/` e `avaliacoes/target/site/jacoco/`. A métrica exclui configuração/bootstrap, seed demonstrativo e classes aninhadas usadas como DTOs.

## Observabilidade

Os microsserviços expõem healthchecks (`/actuator/health`), probes de disponibilidade, métricas Prometheus, traces OTLP e logs estruturados. Prometheus, Grafana, Loki, Promtail e Jaeger integram a camada de monitoramento e diagnóstico distribuído.

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

Nas implantações de produção, use o perfil `production` e forneça `APP_AUTH_SECRET` e `APP_INTERNAL_SECRET` por meio de secrets reais; o seed demonstrativo fica desabilitado nesse perfil. O Docker Compose local usa intencionalmente o seed demonstrativo.

## CI/CD

Os workflows do GitHub Actions cobrem testes Maven, lint, build do frontend e publicação das imagens dos serviços no GitHub Container Registry. O conjunto de manifests Kubernetes representa o ambiente de execução da aplicação.

## Execução Automatizada

```bash
docker compose up --build -d
```

URLs locais: frontend `http://localhost:5173`, catálogo/API `http://localhost:8090`, avaliações/API `http://localhost:8081`, Prometheus `http://localhost:9090`, Grafana `http://localhost:3000` e Jaeger `http://localhost:16686`. O PostgreSQL e a porta AMQP do RabbitMQ ficam acessíveis aos serviços dentro da rede Docker; o console RabbitMQ fica em `http://localhost:15672`.

### Usuários e Senhas Demonstrativos

| Acesso | Usuário | Senha |
|---|---|---|
| Administrador da aplicação | `admin@pbat.local` | `admin123` |
| Usuário da aplicação (Antonio Campbell) | `user@pbat.local` | `user123` |
| RabbitMQ | `antonio` | `admin123` |
| PostgreSQL | `antonio` | `admin123` |

## Stack

- Java 21, Spring Boot 4, Spring Data JPA, Hibernate Envers e Spring AMQP.
- PostgreSQL, H2 e RabbitMQ.
- React 19, Vite e Nginx.
- Docker Compose e Kubernetes.
- Prometheus, Grafana, Loki, Promtail e Jaeger.
