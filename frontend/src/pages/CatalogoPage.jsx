import '../styles/catalog.css'

function CatalogoPage({
  produtos,
  produtosFiltrados,
  categorias,
  auth,
  isAdmin,
  busca,
  categoriaId,
  status,
  precoMin,
  precoMax,
  ordenacao,
  statusOptions,
  ordenacoes,
  formatarPreco,
  statusInfo,
  onBuscaChange,
  onCategoriaChange,
  onStatusChange,
  onPrecoMinChange,
  onPrecoMaxChange,
  onOrdenacaoChange,
  onLimparFiltros,
  onComprar,
  onSelecionarProduto,
  onEditar,
  onDeletar,
}) {
  return <>
     <section className="hero-section"><div><span className="eyebrow">PB Market · catálogo</span><h1>Encontre o que<br /><em>faz parte da sua rotina.</em></h1><p>Compare preços, confira a disponibilidade e leia avaliações vinculadas a compras.</p><button className="hero-link" onClick={() => document.getElementById('catalogo')?.scrollIntoView({ behavior: 'smooth' })}>Explorar produtos <span>↓</span></button></div></section>
      <section className="insight-row"><div><span className="insight-number">{produtos.length}</span><span>produtos cadastrados</span></div><div><span className="insight-number">{produtos.filter((produto) => produto.status === 'ATIVO').length}</span><span>produtos disponíveis</span></div><div><span className="insight-number">{categorias.length}</span><span>categorias no catálogo</span></div><div className="insight-note">Consulte preço e estoque<br /><strong>● dados do catálogo</strong></div></section>
    <section id="catalogo" className="catalog-section">
       <div className="section-heading"><div><span className="eyebrow">catálogo</span><h2>Encontre produtos para sua rotina</h2></div><span className="result-count">{produtosFiltrados.length} {produtosFiltrados.length === 1 ? 'produto' : 'produtos'}</span></div>
      <div className="filter-panel">
        <div className="search-field"><span>⌕</span><input value={busca} onChange={(event) => onBuscaChange(event.target.value)} placeholder="Buscar por nome, descrição ou categoria" /></div>
        <select value={categoriaId} onChange={(event) => onCategoriaChange(event.target.value)}><option value="">Todas as categorias</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select>
        <select value={status} onChange={(event) => onStatusChange(event.target.value)}><option value="">Todos os estados</option>{Object.entries(statusOptions).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select>
        <div className="price-range"><input type="number" min="0" placeholder="R$ mínimo" value={precoMin} onChange={(event) => onPrecoMinChange(event.target.value)} /><span>até</span><input type="number" min="0" placeholder="R$ máximo" value={precoMax} onChange={(event) => onPrecoMaxChange(event.target.value)} /></div>
        <select value={ordenacao} onChange={(event) => onOrdenacaoChange(event.target.value)}>{Object.entries(ordenacoes).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
        <button className="filter-clear" onClick={onLimparFiltros}>Limpar</button>
      </div>
      <div className="product-grid">
         {produtosFiltrados.length === 0 ? <div className="empty-state"><span>◌</span><h3>Nenhum produto encontrado</h3><p>Troque o termo de busca ou remova filtros para ver outros produtos.</p></div> : produtosFiltrados.map((produto) => {
          const info = statusInfo(produto)
           return <article className="product-card" key={produto.id} role="button" tabIndex="0" onClick={(event) => { if (!event.target.closest('button, input, select, textarea')) onSelecionarProduto(produto) }} onKeyDown={(event) => { if ((event.key === 'Enter' || event.key === ' ') && event.target === event.currentTarget) { event.preventDefault(); onSelecionarProduto(produto) } }}>
           <div className="product-visual"><span className="product-id">#{String(produto.id).padStart(2, '0')}</span><span className={`status-pill ${info.className}`}>{info.label}</span><div className="product-glyph">{produto.nome.charAt(0)}</div><span className="product-category">{produto.categoria?.nome || 'Sem categoria'}</span></div>
              <div className="product-content"><h3>{produto.nome}</h3><p>{produto.descricao}</p><div className="product-bottom"><div><strong>{formatarPreco(produto.preco)}</strong><small>{produto.estoque > 0 ? `${produto.estoque} em estoque` : 'Sem estoque'}</small></div><button className="buy-button" disabled={info.className !== 'status-ativo' && info.className !== 'status-baixo'} onClick={() => onComprar(produto)}>{auth ? 'Adicionar ao carrinho' : 'Entre para adicionar'} <span>↗</span></button></div></div>
             {isAdmin && <div className="product-actions"><button onClick={() => onEditar(produto)}>Editar</button><button onClick={() => onDeletar(produto.id)}>Excluir</button></div>}
          </article>
        })}
      </div>
    </section>
  </>
}

export default CatalogoPage
