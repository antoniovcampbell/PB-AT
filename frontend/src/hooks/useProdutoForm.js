import { useState } from 'react'
import { apiFetch } from '../api'

export function useProdutoForm(auth, carregarCatalogo, setErro, setAviso) {
  const [editandoId, setEditandoId] = useState(null)
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [preco, setPreco] = useState('')
  const [estoque, setEstoque] = useState('10')
  const [statusProduto, setStatusProduto] = useState('ATIVO')
  const [categoriaProduto, setCategoriaProduto] = useState('')
  const [novaCategoria, setNovaCategoria] = useState('')

  function editarProduto(produto) {
    setNome(produto.nome)
    setDescricao(produto.descricao)
    setPreco(produto.preco)
    setEstoque(produto.estoque ?? 0)
    setStatusProduto(produto.status || 'ATIVO')
    setCategoriaProduto(produto.categoria?.id || '')
    setEditandoId(produto.id)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function limparFormulario() {
    setNome('')
    setDescricao('')
    setPreco('')
    setEstoque('10')
    setStatusProduto('ATIVO')
    setCategoriaProduto('')
    setEditandoId(null)
  }

  async function salvarProduto(event) {
    event.preventDefault()
    setErro('')
    try {
      const produto = { nome: nome.trim(), descricao: descricao.trim(), preco: Number(preco), estoque: Number(estoque), status: statusProduto, categoria: categoriaProduto ? { id: Number(categoriaProduto) } : null }
      await apiFetch(editandoId ? `/api/produtos/${editandoId}` : '/api/produtos', { method: editandoId ? 'PUT' : 'POST', body: JSON.stringify(produto) }, auth.token)
      limparFormulario()
      await carregarCatalogo()
       setAviso(editandoId ? `Produto #${editandoId} atualizado no catálogo.` : 'Produto cadastrado no catálogo.')
    } catch (error) { setErro(error.message) }
  }

  async function deletarProduto(id) {
    if (!window.confirm(`Excluir o produto #${id} do catálogo?`)) return
    try { await apiFetch(`/api/produtos/${id}`, { method: 'DELETE' }, auth.token); await carregarCatalogo(); setAviso(`Produto #${id} removido do catálogo.`) } catch (error) { setErro(error.message) }
  }

  async function criarCategoria(event) {
    event.preventDefault()
    if (!novaCategoria.trim()) return
    try { await apiFetch('/api/categorias', { method: 'POST', body: JSON.stringify({ nome: novaCategoria.trim(), descricao: 'Categoria para organizar os produtos do catálogo.' }) }, auth.token); setNovaCategoria(''); await carregarCatalogo(); setAviso('Categoria adicionada ao catálogo.') } catch (error) { setErro(error.message) }
  }

  async function deletarCategoria(categoria) {
    if (!window.confirm(`Remover a categoria "${categoria.nome}"?`)) return
    try {
      await apiFetch(`/api/categorias/${categoria.id}`, { method: 'DELETE' }, auth.token)
      if (String(categoriaProduto) === String(categoria.id)) setCategoriaProduto('')
      await carregarCatalogo()
      setAviso(`Categoria "${categoria.nome}" removida.`)
    } catch (error) { setErro(error.message) }
  }

  return { editandoId, nome, descricao, preco, estoque, statusProduto, categoriaProduto, novaCategoria,
    setNome, setDescricao, setPreco, setEstoque, setStatusProduto, setCategoriaProduto, setNovaCategoria,
    editarProduto, deletarProduto, salvarProduto, limparFormulario, criarCategoria, deletarCategoria }
}
