import { useCallback, useEffect, useState } from 'react'
import { apiFetch } from '../api'

export function useGestao(auth, isAdmin, aba, setErro, setAviso, carregarCatalogo) {
  const [avaliacoes, setAvaliacoes] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [comprasAdmin, setComprasAdmin] = useState([])

  const carregarGestao = useCallback(async () => {
    if (!isAdmin) return
    try {
      const [listaAvaliacoes, listaUsuarios, listaCompras] = await Promise.all([
        apiFetch('/api/avaliacoes', {}, auth.token),
        apiFetch('/api/usuarios', {}, auth.token),
        apiFetch('/api/compras', {}, auth.token),
      ])
      setAvaliacoes(listaAvaliacoes)
      setUsuarios(listaUsuarios)
      setComprasAdmin(listaCompras)
    } catch (error) { setErro(error.message) }
  }, [auth, isAdmin, setErro])

  useEffect(() => {
    if (aba === 'gestao' && isAdmin) {
      // O painel administrativo consulta as fontes de gestão ao ser aberto.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      carregarGestao()
    }
  }, [aba, isAdmin, carregarGestao])

  async function atualizarUsuario(id, ativo) {
    try {
      const usuario = await apiFetch(`/api/usuarios/${id}/ativo?ativo=${ativo}`, { method: 'PUT' }, auth.token)
      setUsuarios((atual) => atual.map((item) => item.id === id ? usuario : item))
      setAviso('Usuário atualizado.')
    } catch (error) { setErro(error.message) }
  }

  async function cancelarCompra(id) {
    if (!window.confirm('Cancelar esta compra e devolver os itens ao estoque?')) return
    try {
      const compra = await apiFetch(`/api/compras/${id}`, { method: 'DELETE' }, auth.token)
      setComprasAdmin((atual) => atual.map((item) => item.id === id ? compra : item))
      await carregarCatalogo()
      setAviso('Compra cancelada.')
    } catch (error) { setErro(error.message) }
  }

  return { avaliacoes, usuarios, comprasAdmin, atualizarUsuario, cancelarCompra }
}
