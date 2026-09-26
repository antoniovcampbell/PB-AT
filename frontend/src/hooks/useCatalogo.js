import { useCallback, useEffect, useMemo, useState } from 'react'
import { apiFetch } from '../api'
import { STATUS } from '../pages/constants'

export function useCatalogo(setErro) {
  const [produtos, setProdutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [busca, setBusca] = useState('')
  const [categoriaId, setCategoriaId] = useState('')
  const [status, setStatus] = useState('')
  const [precoMin, setPrecoMin] = useState('')
  const [precoMax, setPrecoMax] = useState('')
  const [ordenacao, setOrdenacao] = useState('recente')

  const carregar = useCallback(async () => {
    try {
      setErro('')
      const [lista, categoriasLista] = await Promise.all([apiFetch('/api/produtos'), apiFetch('/api/categorias')])
      setProdutos(lista)
      setCategorias(categoriasLista)
    } catch (error) { setErro(error.message) }
  }, [setErro])

  useEffect(() => {
    // A primeira leitura sincroniza a tela com os serviços externos.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar()
  }, [carregar])

  const produtosFiltrados = useMemo(() => produtos.filter((produto) => {
    const termo = busca.trim().toLowerCase()
    const correspondeTexto = !termo || [produto.nome, produto.descricao, produto.categoria?.nome].filter(Boolean).some((campo) => String(campo).toLowerCase().includes(termo))
    const correspondeCategoria = !categoriaId || String(produto.categoria?.id) === String(categoriaId)
    const correspondeStatus = !status || (produto.status || 'ATIVO') === status
    const correspondeMin = !precoMin || Number(produto.preco) >= Number(precoMin)
    const correspondeMax = !precoMax || Number(produto.preco) <= Number(precoMax)
    return correspondeTexto && correspondeCategoria && correspondeStatus && correspondeMin && correspondeMax
  }).sort((a, b) => {
    if (ordenacao === 'nome') return a.nome.localeCompare(b.nome)
    if (ordenacao === 'maiorPreco') return Number(b.preco) - Number(a.preco)
    if (ordenacao === 'menorPreco') return Number(a.preco) - Number(b.preco)
    return Number(b.id) - Number(a.id)
  }), [produtos, busca, categoriaId, status, precoMin, precoMax, ordenacao])

  function limparFiltros() {
    setBusca('')
    setCategoriaId('')
    setStatus('')
    setPrecoMin('')
    setPrecoMax('')
    setOrdenacao('recente')
  }

  return {
    produtos, categorias, produtosFiltrados, carregar,
    busca, categoriaId, status, precoMin, precoMax, ordenacao, setBusca, setCategoriaId, setStatus,
    setPrecoMin, setPrecoMax, setOrdenacao, limparFiltros,
    statusInfo: (produto) => STATUS[produto.status] || STATUS.ATIVO,
  }
}
