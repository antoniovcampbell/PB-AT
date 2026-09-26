# Frontend PB Market

Interface React/Vite do marketplace demonstrativo. O catálogo é servido pelo Nginx em `http://localhost:5173` e usa o proxy `/api` para os microsserviços.

## Funcionalidades

- Catálogo visual com busca, categorias, estados, preços e ordenação.
- Login e registro de usuários.
- Área de compras do cliente.
- Avaliações limitadas a uma por produto em cada compra, publicadas em “Minhas compras”.
- Página do produto com avaliações roláveis, filtro por estrelas e busca textual.
- Painel administrativo para produtos, categorias, usuários, compras e avaliações.
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
npm run check:nginx
npm run build
```

Em Docker, o `Dockerfile` gera o build Vite e o Nginx encaminha `/api/auth`, `/api/categorias`, `/api/compras`, `/api/carrinho`, `/api/usuarios`, `/api/produtos` e `/api/avaliacoes`. O proxy usa o DNS interno do Docker para acompanhar a recriação dos serviços.
