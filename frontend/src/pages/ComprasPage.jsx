import '../styles/commerce.css'
import AvaliarCompra from '../AvaliarCompra'

function ComprasPage({ compras, avaliacoes, auth, formatarPreco, onExplorar, onAvaliacaoSalva }) {
  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div><span className="eyebrow">área do cliente</span><h2>Minhas compras</h2></div>
      </div>
      {compras.length === 0 ? <div className="empty-state">
        <span>✦</span>
        <h3>Seu histórico começa aqui</h3>
        <p>Compre um produto para liberar avaliações verificadas e acompanhar seu pedido.</p>
        <button className="btn btn-primario" onClick={onExplorar}>Explorar catálogo</button>
      </div> : <div className="purchase-list">
        {compras.map((compra) => <article className="purchase-card" key={compra.id}>
          <header className="purchase-card-heading">
            <div><span className="eyebrow">compra #{compra.id}</span><small>{new Date(compra.criadaEm).toLocaleString('pt-BR')}</small></div>
            <div className="purchase-side"><span className="purchase-status">{compra.status}</span><strong>{formatarPreco(compra.total)}</strong></div>
          </header>
          <div className="purchase-items">{compra.itens.map((item) => {
            const avaliacao = avaliacoes.find((review) => Number(review.compraId) === Number(compra.id) && Number(review.produtoId) === Number(item.produtoId))
            return <div className="purchase-item-review" key={`${compra.id}-${item.produtoId}`}>
              <div className="purchase-item-name"><strong>{item.nomeProduto}</strong><small>Quantidade: {item.quantidade}</small></div>
              {avaliacao ? <AvaliarCompra compraId={compra.id} produtoId={item.produtoId} auth={auth} avaliacao={avaliacao} onAvaliacaoSalva={onAvaliacaoSalva} /> : compra.status !== 'CANCELADA' ? <AvaliarCompra compraId={compra.id} produtoId={item.produtoId} auth={auth} onAvaliacaoSalva={onAvaliacaoSalva} /> : <span className="review-unavailable">Compra cancelada</span>}
            </div>
          })}</div>
        </article>)}
      </div>}
    </section>
  )
}

export default ComprasPage
