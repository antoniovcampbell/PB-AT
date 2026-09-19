function ComprasPage({ compras, formatarPreco, onExplorar }) {
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
          <div>
            <span className="eyebrow">compra #{compra.id}</span>
            <h3>{compra.itens.map((item) => item.nomeProduto).join(' · ')}</h3>
            <small>{new Date(compra.criadaEm).toLocaleString('pt-BR')}</small>
          </div>
          <div className="purchase-side"><span className="purchase-status">{compra.status}</span><strong>{formatarPreco(compra.total)}</strong></div>
        </article>)}
      </div>}
    </section>
  )
}

export default ComprasPage
