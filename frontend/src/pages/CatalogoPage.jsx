import AvaliacoesPorProduto from '../AvaliacoesPorProduto'

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
  avaliacoesAbertas,
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
  onEditar,
  onDeletar,
  onToggleAvaliacoes,
  onRequireAuth
}) {
  return <>
    <section className="hero-section"><div><span className="eyebrow">coleção de setembro · 2026</span><h1>Escolhas boas para<br /><em>dias mais leves.</em></h1><p>Produtos selecionados, preços claros e avaliações de quem realmente comprou.</p><button className="hero-link" onClick={() => document.getElementById('catalogo')?.scrollIntoView({ behavior: 'smooth' })}>Ver coleção <span>↓</span></button></div></section>
     <section className="insight-row"><div><span className="insight-number">{produtos.length}</span><span>itens no catálogo</span></div><div><span className="insight-number">{produtos.filter((produto) => produto.status === 'ATIVO').length}</span><span>prontos para envio</span></div><div><span className="insight-number">{categorias.length}</span><span>categorias disponíveis</span></div><div className="insight-note">Atualizado em tempo real<br /><strong>● sistema online</strong></div></section>
    <section id="catalogo" className="catalog-section">
      <div className="section-heading"><div><span className="eyebrow">catálogo</span><h2>Encontre seu próximo favorito</h2></div><span className="result-count">{produtosFiltrados.length} resultados</span></div>
      <div className="filter-panel">
        <div className="search-field"><span>⌕</span><input value={busca} onChange={(event) => onBuscaChange(event.target.value)} placeholder="Buscar por nome, descrição ou categoria" /></div>
        <select value={categoriaId} onChange={(event) => onCategoriaChange(event.target.value)}><option value="">Todas as categorias</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select>
        <select value={status} onChange={(event) => onStatusChange(event.target.value)}><option value="">Todos os estados</option>{Object.entries(statusOptions).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select>
        <div className="price-range"><input type="number" min="0" placeholder="R$ mínimo" value={precoMin} onChange={(event) => onPrecoMinChange(event.target.value)} /><span>até</span><input type="number" min="0" placeholder="R$ máximo" value={precoMax} onChange={(event) => onPrecoMaxChange(event.target.value)} /></div>
        <select value={ordenacao} onChange={(event) => onOrdenacaoChange(event.target.value)}>{Object.entries(ordenacoes).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
        <button className="filter-clear" onClick={onLimparFiltros}>Limpar</button>
      </div>
      <div className="product-grid">
        {produtosFiltrados.length === 0 ? <div className="empty-state"><span>◌</span><h3>Nenhuma escolha por aqui</h3><p>Remova alguns filtros para abrir o catálogo.</p></div> : produtosFiltrados.map((produto) => {
          const info = statusInfo(produto)
          return <article className="product-card" key={produto.id}>
            <div className="product-visual"><span className="product-id">#{String(produto.id).padStart(2, '0')}</span><span className={`status-pill ${info.className}`}>{info.label}</span><div className="product-glyph">{produto.nome.charAt(0)}</div><span className="product-category">{produto.categoria?.nome || 'Coleção PB'}</span></div>
            <div className="product-content"><h3>{produto.nome}</h3><p>{produto.descricao}</p><div className="product-bottom"><div><strong>{formatarPreco(produto.preco)}</strong><small>{produto.estoque > 0 ? `${produto.estoque} em estoque` : 'Sem estoque'}</small></div><button className="buy-button" disabled={info.className !== 'status-ativo' && info.className !== 'status-baixo'} onClick={() => onComprar(produto)}>{auth ? 'Comprar' : 'Entrar para comprar'} <span>↗</span></button></div></div>
            <div className="product-actions">{isAdmin && <><button onClick={() => onEditar(produto)}>Editar</button><button onClick={() => onDeletar(produto.id)}>Excluir</button></>}<button onClick={() => onToggleAvaliacoes(produto.id)}>{avaliacoesAbertas[produto.id] ? 'Fechar avaliações' : 'Ver avaliações'}</button></div>
            {avaliacoesAbertas[produto.id] && <AvaliacoesPorProduto produtoId={produto.id} auth={auth} onRequireAuth={onRequireAuth} />}
          </article>
        })}
      </div>
    </section>
  </>
}

export default CatalogoPage
