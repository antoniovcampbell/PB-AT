import { useCallback, useEffect, useRef, useState } from 'react'
import { apiFetch } from '../api'

const CARRINHO_VAZIO = { itens: [], total: 0 }

export function useCommerce(auth, setErro, setAviso) {
  const [compras, setCompras] = useState([])
  const [avaliacoesMinhas, setAvaliacoesMinhas] = useState([])
  const [carrinho, setCarrinho] = useState(CARRINHO_VAZIO)
  const checkoutKey = useRef(null)

  const carregarCompras = useCallback(async () => {
    if (!auth) return
    try {
      const [listaCompras, listaAvaliacoes] = await Promise.all([
        apiFetch('/api/compras/minhas', {}, auth.token),
        apiFetch('/api/avaliacoes/minhas', {}, auth.token)
      ])
      setCompras(listaCompras)
      setAvaliacoesMinhas(listaAvaliacoes)
    } catch (error) { setErro(error.message) }
  }, [auth, setErro])

  const carregarCarrinho = useCallback(async () => {
    if (!auth) return
    try { setCarrinho(await apiFetch('/api/carrinho', {}, auth.token)) } catch (error) { setErro(error.message) }
  }, [auth, setErro])

  useEffect(() => {
    if (auth) {
      // O histórico alimenta a regra visual de elegibilidade das avaliações.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      carregarCompras()
    } else {
      setCompras([])
      setAvaliacoesMinhas([])
      checkoutKey.current = null
    }
  }, [auth, carregarCompras])

  useEffect(() => {
    if (auth) {
      // O carrinho é persistido no catálogo e reidratado após login ou recarga.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      carregarCarrinho()
    } else {
      setCarrinho(CARRINHO_VAZIO)
    }
  }, [auth, carregarCarrinho])

  async function adicionarAoCarrinho(produto, onRequireAuth) {
    if (!auth) { onRequireAuth(); return }
    try {
      const resposta = await apiFetch('/api/carrinho/itens', { method: 'POST', body: JSON.stringify({ produtoId: produto.id, quantidade: 1 }) }, auth.token)
      setCarrinho(resposta)
      setAviso(`${produto.nome} adicionado ao carrinho.`)
    } catch (error) { setErro(error.message) }
  }

  async function atualizarCarrinho(produtoId, quantidade) {
    try { setCarrinho(await apiFetch(`/api/carrinho/itens/${produtoId}`, { method: 'PUT', body: JSON.stringify({ produtoId, quantidade }) }, auth.token)) } catch (error) { setErro(error.message) }
  }

  async function removerCarrinho(produtoId) {
    try { setCarrinho(await apiFetch(`/api/carrinho/itens/${produtoId}`, { method: 'DELETE' }, auth.token)) } catch (error) { setErro(error.message) }
  }

  async function finalizarCarrinho() {
    try {
      checkoutKey.current ||= crypto.randomUUID()
      const compra = await apiFetch('/api/carrinho/finalizar', { method: 'POST', headers: { 'Idempotency-Key': checkoutKey.current } }, auth.token)
      setCarrinho(CARRINHO_VAZIO)
      checkoutKey.current = null
      setAviso(`Compra #${compra.id} criada com sucesso.`)
      await carregarCompras()
    } catch (error) { setErro(error.message) }
  }

  return { compras, avaliacoesMinhas, carrinho, carregarCompras, adicionarAoCarrinho, atualizarCarrinho, removerCarrinho, finalizarCarrinho }
}
