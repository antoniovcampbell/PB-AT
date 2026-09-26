import '../styles/commerce.css'

function CarrinhoPage({ carrinho, formatarPreco, onAtualizar, onRemover, onFinalizar, onExplorar }) {
  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div><span className="eyebrow">sua seleção</span><h2>Carrinho</h2></div>
        <span className="result-count">{carrinho.itens.length} itens</span>
      </div>
      {carrinho.itens.length === 0 ? <div className="empty-state">
        <span>＋</span><h3>Seu carrinho está vazio</h3><p>Adicione produtos enquanto explora o catálogo e finalize tudo de uma vez.</p>
        <button className="btn btn-primario" onClick={onExplorar}>Explorar catálogo</button>
      </div> : <>
        <div className="cart-list">{carrinho.itens.map((item) => <article className="cart-item" key={item.produtoId}>
          <div><span className="eyebrow">produto #{item.produtoId}</span><h3>{item.nomeProduto}</h3><strong>{formatarPreco(item.preco)}</strong></div>
          <div className="cart-controls"><label>Quantidade <input type="number" min="1" max={item.estoque} value={item.quantidade} onChange={(event) => onAtualizar(item.produtoId, Number(event.target.value))} /></label><button className="btn-remover" onClick={() => onRemover(item.produtoId)}>Remover</button></div>
        </article>)}</div>
        <div className="cart-summary"><span>Total <strong>{formatarPreco(carrinho.total)}</strong></span><button className="btn btn-primario" onClick={onFinalizar}>Finalizar compra</button></div>
      </>}
    </section>
  )
}

export default CarrinhoPage
