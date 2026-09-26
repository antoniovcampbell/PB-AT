import { useCallback, useEffect, useMemo, useState } from 'react'
import { apiFetch } from './api'
import './styles/reviews.css'

function AvaliacoesPorProduto({ produtoId, aberto = true, auth }) {
  const [avaliacoes, setAvaliacoes] = useState([])
  const [media, setMedia] = useState(null)
  const [erro, setErro] = useState('')
  const [filtroNota, setFiltroNota] = useState('')
  const [buscaReview, setBuscaReview] = useState('')

  const carregarAvaliacoes = useCallback(async () => {
    try {
      setErro('')
      const [lista, mediaResposta] = await Promise.all([
        apiFetch(`/api/avaliacoes/produtos/${produtoId}`),
        apiFetch(`/api/avaliacoes/produtos/${produtoId}/media`)
      ])
      setAvaliacoes(lista)
      setMedia(mediaResposta)
    } catch (error) {
      setErro(error.message)
    }
  }, [produtoId])

  useEffect(() => {
    if (produtoId && aberto) {
      // A lista é uma sincronização com o serviço de avaliações.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      carregarAvaliacoes()
    }
  }, [produtoId, aberto, carregarAvaliacoes])

  async function deletarAvaliacao(id) {
    try {
      await apiFetch(`/api/avaliacoes/${id}`, { method: 'DELETE' }, auth.token)
      await carregarAvaliacoes()
    } catch (error) {
      setErro(error.message)
    }
  }

  const mediaFormatada = media?.total > 0 ? `${Number(media.media).toLocaleString('pt-BR')} / 5` : 'Sem avaliações'
  const avaliacoesFiltradas = useMemo(() => avaliacoes.filter((avaliacao) => {
    const notaCorresponde = filtroNota === '' || Number(avaliacao.nota) === Number(filtroNota)
    const texto = `${avaliacao.nomeUsuario} ${avaliacao.comentario || ''}`.toLocaleLowerCase('pt-BR')
    return notaCorresponde && texto.includes(buscaReview.toLocaleLowerCase('pt-BR'))
  }), [avaliacoes, filtroNota, buscaReview])

  return (
    <div className="avaliacoes">
      <div className="avaliacoes-header">
        <div><span className="eyebrow">feedback real</span><h3>Avaliações</h3><p className="avaliacoes-subtitulo">Notas e comentários de quem já comprou.</p></div>
        <span className="media"><strong>★ {mediaFormatada}</strong><small>{media?.total || 0} avaliações</small></span>
      </div>
      {erro && <p className="erro">{erro}</p>}
      <div className="reviews-tools">
        <label><span>Filtrar por nota</span><select value={filtroNota} onChange={(event) => setFiltroNota(event.target.value)}><option value="">Todas as notas</option><option value="5">5 estrelas</option><option value="4">4 estrelas</option><option value="3">3 estrelas</option><option value="2">2 estrelas</option><option value="1">1 estrela</option></select></label>
        <label className="reviews-search"><span>Buscar avaliação</span><input value={buscaReview} onChange={(event) => setBuscaReview(event.target.value)} placeholder="Nome ou comentário" /></label>
        <small>{avaliacoesFiltradas.length} de {avaliacoes.length}</small>
      </div>
      <div className="lista-avaliacoes">
        {avaliacoes.length === 0 ? <p className="sem-avaliacao">Nenhuma avaliação ainda.</p> : avaliacoesFiltradas.length === 0 ? <p className="sem-avaliacao">Nenhuma avaliação corresponde ao filtro.</p> : avaliacoesFiltradas.map((avaliacao) => (
          <article className="avaliacao" key={avaliacao.id}>
            <div className="avaliacao-cabecalho"><div><strong>{avaliacao.nomeUsuario}</strong><p className="avaliacao-data">{avaliacao.dataCriacao ? new Date(avaliacao.dataCriacao).toLocaleDateString('pt-BR') : ''}</p></div><span className="estrelas">{'★'.repeat(avaliacao.nota)}{'☆'.repeat(5 - avaliacao.nota)}</span></div>
            {avaliacao.comentario && <p>{avaliacao.comentario}</p>}
            {auth?.usuario.perfil === 'ADMIN' && <button type="button" className="btn-remover" onClick={() => deletarAvaliacao(avaliacao.id)}>Remover</button>}
          </article>
        ))}
      </div>
    </div>
  )
}

export default AvaliacoesPorProduto
