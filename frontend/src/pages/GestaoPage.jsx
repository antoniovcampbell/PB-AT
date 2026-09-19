function GestaoPage({
  categorias,
  produtos,
  status,
  editandoId,
  nome,
  descricao,
  preco,
  estoque,
  statusProduto,
  categoriaProduto,
  novaCategoria,
  onNomeChange,
  onDescricaoChange,
  onPrecoChange,
  onEstoqueChange,
  onStatusProdutoChange,
  onCategoriaProdutoChange,
  onNovaCategoriaChange,
  onSalvarProduto,
  onLimparFormulario,
  onCriarCategoria,
  statusInfo
}) {
  return (
    <section className="dashboard-section">
      <div className="section-heading">
        <div><span className="eyebrow">área administrativa</span><h2>Operação do catálogo</h2></div>
      </div>
      <div className="admin-layout">
        <form className="admin-form" onSubmit={onSalvarProduto}>
          <div className="form-heading"><span className="eyebrow">{editandoId ? `edição #${editandoId}` : 'novo registro'}</span><h3>{editandoId ? 'Atualizar produto' : 'Adicionar produto'}</h3></div>
          <label className="campo"><span>Nome</span><input value={nome} onChange={(event) => onNomeChange(event.target.value)} required /></label>
          <label className="campo"><span>Descrição</span><textarea value={descricao} onChange={(event) => onDescricaoChange(event.target.value)} required /></label>
          <div className="form-line">
            <label className="campo"><span>Preço</span><input type="number" min="0.01" step="0.01" value={preco} onChange={(event) => onPrecoChange(event.target.value)} required /></label>
            <label className="campo"><span>Estoque</span><input type="number" min="0" value={estoque} onChange={(event) => onEstoqueChange(event.target.value)} required /></label>
          </div>
          <div className="form-line">
            <label className="campo"><span>Categoria</span><select value={categoriaProduto} onChange={(event) => onCategoriaProdutoChange(event.target.value)}><option value="">Sem categoria</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select></label>
            <label className="campo"><span>Estado</span><select value={statusProduto} onChange={(event) => onStatusProdutoChange(event.target.value)}>{Object.entries(status).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select></label>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primario">{editandoId ? 'Salvar alterações' : 'Cadastrar produto'}</button>
            {editandoId && <button type="button" className="btn btn-ghost" onClick={onLimparFormulario}>Cancelar</button>}
          </div>
        </form>
        <div className="admin-side">
          <div className="admin-box">
            <span className="eyebrow">taxonomia</span><h3>Categorias</h3><p>Organize o catálogo por universos que ajudam a escolher.</p>
            <form className="inline-form" onSubmit={onCriarCategoria}><input value={novaCategoria} onChange={(event) => onNovaCategoriaChange(event.target.value)} placeholder="Nova categoria" /><button className="btn btn-secundario">Adicionar</button></form>
            <div className="category-list">{categorias.map((categoria) => <span key={categoria.id}>{categoria.nome}<small>{produtos.filter((produto) => produto.categoria?.id === categoria.id).length}</small></span>)}</div>
          </div>
          <div className="admin-box state-legend">
            <span className="eyebrow">estados</span><h3>Leitura rápida</h3>
            {Object.values(status).map((info) => <div key={info.label}><span className={`status-dot ${info.className}`} />{info.label}<strong>{produtos.filter((produto) => statusInfo(produto).label === info.label).length}</strong></div>)}
          </div>
        </div>
      </div>
    </section>
  )
}

export default GestaoPage
