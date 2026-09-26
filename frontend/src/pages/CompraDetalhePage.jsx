import { STATUS_COMPRA } from './constants'
import '../styles/compra-detail.css'

function CompraDetalhePage({ compra, formatarPreco, onVoltar }) {
  const quantidadeTotal = compra.itens.reduce((total, item) => total + item.quantidade, 0)

  return (
    <section className="dashboard-section purchase-detail-page">
      <button type="button" className="purchase-detail-back" onClick={onVoltar}>← Voltar para a gestão</button>
      <div className="section-heading purchase-detail-heading">
        <div><span className="eyebrow">informações do pedido</span><h2>Compra #{compra.id}</h2></div>
        <span className="purchase-status">{STATUS_COMPRA[compra.status] || compra.status}</span>
      </div>

      <div className="purchase-detail-summary">
        <article><span>Cliente</span><strong>#{compra.usuarioId}</strong></article>
        <article><span>Data da compra</span><strong>{new Date(compra.criadaEm).toLocaleString('pt-BR')}</strong></article>
        <article><span>Unidades compradas</span><strong>{quantidadeTotal}</strong></article>
        <article><span>Total do pedido</span><strong>{formatarPreco(compra.total)}</strong></article>
      </div>

      <section className="admin-box purchase-detail-products">
        <div className="admin-box-heading">
          <div><span className="eyebrow">itens do pedido</span><h3>Produtos da compra</h3></div>
          <small className="admin-result-count">{compra.itens.length} {compra.itens.length === 1 ? 'produto' : 'produtos'}</small>
        </div>
        <div className="purchase-detail-item-list">
          {compra.itens.map((item) => (
            <article className="purchase-detail-item" key={`${compra.id}-${item.produtoId}`}>
              <div className="purchase-detail-product-name">
                <strong>{item.nomeProduto}</strong>
                <small>Produto #{item.produtoId}</small>
              </div>
              <div><span>Preço unitário</span><strong>{formatarPreco(item.precoUnitario)}</strong></div>
              <div><span>Quantidade</span><strong>{item.quantidade}</strong></div>
              <div><span>Subtotal</span><strong>{formatarPreco(Number(item.precoUnitario) * item.quantidade)}</strong></div>
            </article>
          ))}
        </div>
      </section>
    </section>
  )
}

export default CompraDetalhePage
