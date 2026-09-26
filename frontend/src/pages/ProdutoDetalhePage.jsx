import AvaliacoesPorProduto from '../AvaliacoesPorProduto'
import '../styles/product-detail.css'

function ProdutoDetalhePage({ produto, auth, formatarPreco, statusInfo, onVoltar, onComprar }) {
  const info = statusInfo(produto)
  const disponivel = info.className === 'status-ativo' || info.className === 'status-baixo'

  return (
    <section className="product-detail-section">
      <button type="button" className="detail-back" onClick={onVoltar}>← Voltar ao catálogo</button>
      <div className="product-detail-grid">
        <div className="product-detail-visual">
          <span className="product-id">#{String(produto.id).padStart(2, '0')}</span>
          <span className={`status-pill ${info.className}`}>{info.label}</span>
          <div className="product-detail-glyph">{produto.nome.charAt(0)}</div>
          <span className="product-category">{produto.categoria?.nome || 'Coleção PB'}</span>
        </div>
        <div className="product-detail-copy">
          <span className="eyebrow">detalhe do produto</span>
          <h1>{produto.nome}</h1>
          <p className="product-detail-description">{produto.descricao}</p>
          <div className="product-detail-purchase">
            <div><strong>{formatarPreco(produto.preco)}</strong><small>{produto.estoque > 0 ? `${produto.estoque} em estoque` : 'Sem estoque'}</small></div>
            <button className="btn btn-primario" disabled={!disponivel} onClick={() => onComprar(produto)}>{auth ? 'Adicionar ao carrinho' : 'Entrar para comprar'} <span>↗</span></button>
          </div>
          <dl className="product-detail-facts">
            <div><dt>Categoria</dt><dd>{produto.categoria?.nome || 'Coleção PB'}</dd></div>
            <div><dt>Disponibilidade</dt><dd>{info.label}</dd></div>
            <div><dt>Identificação</dt><dd>Produto #{produto.id}</dd></div>
          </dl>
        </div>
      </div>
      <div className="product-detail-reviews">
        <AvaliacoesPorProduto produtoId={produto.id} auth={auth} />
      </div>
    </section>
  )
}

export default ProdutoDetalhePage
