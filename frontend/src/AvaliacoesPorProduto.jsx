import { useCallback, useEffect, useState } from 'react'
import { apiFetch } from './api'

function AvaliacoesPorProduto({ produtoId, aberto = true, auth, onRequireAuth }) {
  const [avaliacoes, setAvaliacoes] = useState([])
  const [media, setMedia] = useState(null)
  const [nota, setNota] = useState(5)
  const [comentario, setComentario] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  const carregarAvaliacoes = useCallback(async () => {
    try {
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

  async function salvarAvaliacao(event) {
    event.preventDefault()
    if (!auth) return onRequireAuth()
    setErro('')
    setCarregando(true)
    try {
      await apiFetch('/api/avaliacoes', {
        method: 'POST',
        body: JSON.stringify({ produtoId, nomeUsuario: auth.usuario.nome, nota, comentario })
      }, auth.token)
      setNota(5)
      setComentario('')
      await carregarAvaliacoes()
    } catch (error) {
      setErro(error.message)
    } finally {
      setCarregando(false)
    }
  }

  async function deletarAvaliacao(id) {
    try {
      await apiFetch(`/api/avaliacoes/${id}`, { method: 'DELETE' }, auth.token)
      await carregarAvaliacoes()
    } catch (error) {
      setErro(error.message)
    }
  }

  const mediaFormatada = media?.total > 0 ? `${Number(media.media).toLocaleString('pt-BR')} / 5` : 'Sem avaliações'

  return (
    <div className="avaliacoes">
      <div className="avaliacoes-header">
        <div><span className="eyebrow">feedback real</span><h3>Avaliações</h3><p className="avaliacoes-subtitulo">Notas e comentários de quem já comprou.</p></div>
        <span className="media"><strong>★ {mediaFormatada}</strong><small>{media?.total || 0} avaliações</small></span>
      </div>
      {erro && <p className="erro">{erro}</p>}
      {auth ? (
        <form onSubmit={salvarAvaliacao} className="avaliacoes-form">
          <label className="campo"><span>Sua nota</span><select value={nota} onChange={(event) => setNota(Number(event.target.value))}><option value={5}>5 · Excelente</option><option value={4}>4 · Muito bom</option><option value={3}>3 · Bom</option><option value={2}>2 · Regular</option><option value={1}>1 · Ruim</option></select></label>
          <label className="campo campo-full"><span>Seu comentário</span><textarea placeholder="O que você achou do produto?" value={comentario} onChange={(event) => setComentario(event.target.value)} maxLength="1000" /></label>
          <button type="submit" className="btn btn-avaliar" disabled={carregando}>{carregando ? 'Enviando...' : 'Avaliar compra'}</button>
        </form>
      ) : <div className="login-callout"><span>🔒</span><p>Comprou este produto? Entre para publicar sua avaliação.</p><button type="button" className="btn btn-secundario" onClick={onRequireAuth}>Entrar para avaliar</button></div>}
      <div className="lista-avaliacoes">
        {avaliacoes.length === 0 ? <p className="sem-avaliacao">Nenhuma avaliação ainda.</p> : avaliacoes.map((avaliacao) => (
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
