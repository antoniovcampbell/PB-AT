import fs from 'node:fs'

const config = fs.readFileSync(new URL('../nginx.conf', import.meta.url), 'utf8')
const requiredRoutes = [
  { path: '/api/carrinho', upstream: '$catalogo_upstream' },
  { path: '/api/usuarios', upstream: '$catalogo_upstream' },
  { path: '/api/avaliacoes', upstream: '$avaliacoes_upstream' },
]

if (!config.includes('resolver 127.0.0.11 valid=10s')) {
  throw new Error('O proxy precisa resolver os nomes DNS dinamicamente pelo DNS do Docker.')
}

for (const { path, upstream } of requiredRoutes) {
  const start = config.indexOf(`location ${path} {`)
  const nextLocation = config.indexOf('\n    location ', start + 1)
  const block = config.slice(start, nextLocation === -1 ? config.length : nextLocation)

  if (start === -1 || !block.includes('rewrite ^/api/(.*)$ /$1 break;') || !block.includes(`proxy_pass http://${upstream};`)) {
    throw new Error(`Rota Nginx inválida ou ausente: ${path}`)
  }
}

console.log(`Rotas Nginx validadas: ${requiredRoutes.map(({ path }) => path).join(', ')}`)
