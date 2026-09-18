# Frontend PB Market

Interface React/Vite do marketplace demonstrativo. O catálogo é servido pelo Nginx em `http://localhost:5173` e usa o proxy `/api` para os microsserviços.

## Funcionalidades

- Catálogo visual com busca, categorias, estados, preços e ordenação.
- Login e registro de usuários.
- Área de compras do cliente.
- Avaliações vinculadas a compras realizadas.
- Painel administrativo para produtos e categorias.
- Layout responsivo para desktop e mobile.

## Desenvolvimento

```bash
npm ci
npm run dev
```

O proxy do Vite espera `catalogo` em `localhost:8090` e `avaliacoes` em `localhost:8081`.

## Verificação

```bash
npm run lint
npm run build
```

Em Docker, o `Dockerfile` gera o build Vite e o Nginx encaminha `/api/auth`, `/api/categorias`, `/api/compras`, `/api/produtos` e `/api/avaliacoes`.
