import { useState } from 'react'
import { apiFetch } from './api'

function AvaliarCompra({ compraId, produtoId, auth, avaliacao, onAvaliacaoSalva }) {
  const [aberto, setAberto] = useState(false)
  const [nota, setNota] = useState(5)
  const [comentario, setComentario] = useState('')
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  async function enviar(event) {
    event.preventDefault()
    setErro('')
    setSalvando(true)
    try {
      await apiFetch('/api/avaliacoes', {
        method: 'POST',
        body: JSON.stringify({ compraId, produtoId, nota, comentario })
      }, auth.token)
      setAberto(false)
      setComentario('')
      await onAvaliacaoSalva()
    } catch (error) {
      setErro(error.message)
    } finally {
      setSalvando(false)
    }
  }

  if (avaliacao) {
    return <div className="purchase-review-done"><span className="estrelas">{'★'.repeat(avaliacao.nota)}{'☆'.repeat(5 - avaliacao.nota)}</span><span>Avaliação enviada para esta compra</span></div>
  }

  return <div className="purchase-review">
    {!aberto ? <button className="btn btn-secundario" onClick={() => setAberto(true)}>Avaliar esta compra</button> : <form onSubmit={enviar}>
      <label className="campo"><span>Sua nota</span><select value={nota} onChange={(event) => setNota(Number(event.target.value))}><option value={5}>5 · Excelente</option><option value={4}>4 · Muito bom</option><option value={3}>3 · Bom</option><option value={2}>2 · Regular</option><option value={1}>1 · Ruim</option></select></label>
      <label className="campo"><span>Comentário (opcional)</span><textarea placeholder="Conte como foi sua experiência" value={comentario} onChange={(event) => setComentario(event.target.value)} maxLength="1000" /></label>
      {erro && <p className="erro">{erro}</p>}
      <div className="form-actions"><button type="submit" className="btn btn-primario" disabled={salvando}>{salvando ? 'Enviando…' : 'Publicar avaliação'}</button><button type="button" className="btn btn-ghost" onClick={() => setAberto(false)}>Cancelar</button></div>
    </form>}
  </div>
}

export default AvaliarCompra
