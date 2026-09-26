import { useMemo, useState } from 'react'
import '../styles/admin.css'

const normalizar = (valor) => String(valor || '').toLocaleLowerCase('pt-BR')

function GestaoPage({
  categorias,
  produtos,
  avaliacoes,
  usuarios,
  compras,
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
  onAtualizarUsuario,
  onCancelarCompra,
  statusInfo,
  onEditar,
  onDeletar
}) {
  const [buscaProdutos, setBuscaProdutos] = useState('')
  const [categoriaFiltroProduto, setCategoriaFiltroProduto] = useState('')
  const [estadoFiltroProduto, setEstadoFiltroProduto] = useState('')
  const [buscaUsuarios, setBuscaUsuarios] = useState('')
  const [perfilFiltroUsuario, setPerfilFiltroUsuario] = useState('')
  const [estadoFiltroUsuario, setEstadoFiltroUsuario] = useState('')
  const [buscaCompras, setBuscaCompras] = useState('')
  const [estadoFiltroCompra, setEstadoFiltroCompra] = useState('')
  const [buscaAvaliacoes, setBuscaAvaliacoes] = useState('')
  const [notaFiltroAvaliacao, setNotaFiltroAvaliacao] = useState('')
  const produtosFiltrados = useMemo(() => produtos.filter((produto) => {
    const textoCorresponde = normalizar(`${produto.nome} ${produto.categoria?.nome || ''}`).includes(normalizar(buscaProdutos))
    const categoriaCorresponde = categoriaFiltroProduto === '' || String(produto.categoria?.id) === categoriaFiltroProduto
    const estadoCorresponde = estadoFiltroProduto === '' || produto.status === estadoFiltroProduto
    return textoCorresponde && categoriaCorresponde && estadoCorresponde
  }), [produtos, buscaProdutos, categoriaFiltroProduto, estadoFiltroProduto])
  const usuariosFiltrados = useMemo(() => usuarios.filter((usuario) => {
    const textoCorresponde = normalizar(`${usuario.nome} ${usuario.email} ${usuario.perfil}`).includes(normalizar(buscaUsuarios))
    const perfilCorresponde = perfilFiltroUsuario === '' || usuario.perfil === perfilFiltroUsuario
    const estadoCorresponde = estadoFiltroUsuario === '' || String(usuario.ativo) === estadoFiltroUsuario
    return textoCorresponde && perfilCorresponde && estadoCorresponde
  }), [usuarios, buscaUsuarios, perfilFiltroUsuario, estadoFiltroUsuario])
  const comprasFiltradas = useMemo(() => compras.filter((compra) => {
    const textoCorresponde = normalizar(`#${compra.id} ${compra.itens.map((item) => item.nomeProduto).join(' ')}`).includes(normalizar(buscaCompras))
    return textoCorresponde && (estadoFiltroCompra === '' || compra.status === estadoFiltroCompra)
  }), [compras, buscaCompras, estadoFiltroCompra])
  const avaliacoesFiltradas = useMemo(() => avaliacoes.filter((avaliacao) => {
    const textoCorresponde = normalizar(`${avaliacao.nomeUsuario} ${avaliacao.produtoId} ${avaliacao.comentario}`).includes(normalizar(buscaAvaliacoes))
    return textoCorresponde && (notaFiltroAvaliacao === '' || String(avaliacao.nota) === notaFiltroAvaliacao)
  }), [avaliacoes, buscaAvaliacoes, notaFiltroAvaliacao])

  return (
    <section className="dashboard-section">
      <div className="section-heading admin-title">
        <div><span className="eyebrow">central de operação</span><h2>Painel de gestão</h2><p>Catálogo, contas, pedidos e avaliações em um só lugar.</p></div>
      </div>
      <div className="admin-metrics">
        <article><span>Produtos</span><strong>{produtos.length}</strong><small>{produtos.filter((produto) => produto.estoque > 0).length} com estoque</small></article>
        <article><span>Categorias</span><strong>{categorias.length}</strong><small>{categorias.filter((categoria) => produtos.some((produto) => produto.categoria?.id === categoria.id)).length} em uso</small></article>
        <article><span>Usuários</span><strong>{usuarios.length}</strong><small>{usuarios.filter((usuario) => usuario.ativo).length} contas ativas</small></article>
        <article><span>Pedidos</span><strong>{compras.length}</strong><small>{compras.filter((compra) => compra.status === 'CRIADA' || compra.status === 'PROCESSANDO').length} em andamento</small></article>
        <article><span>Avaliações</span><strong>{avaliacoes.length}</strong><small>publicadas pelos clientes</small></article>
      </div>
      <div className="admin-layout">
        <form className="admin-form" onSubmit={onSalvarProduto}>
          <div className="form-heading"><span className="eyebrow">inventário</span><h3>{editandoId ? `Editar produto #${editandoId}` : 'Cadastrar produto'}</h3><p>Inclua informações claras para manter o catálogo atualizado.</p></div>
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
            <span className="eyebrow">organização</span><h3>Categorias</h3><p>Organize o catálogo por grupos de produtos.</p>
            <form className="inline-form" onSubmit={onCriarCategoria}><input value={novaCategoria} onChange={(event) => onNovaCategoriaChange(event.target.value)} placeholder="Nova categoria" /><button className="btn btn-secundario">Adicionar</button></form>
            <div className="category-list">{categorias.map((categoria) => <span key={categoria.id}>{categoria.nome}<small>{produtos.filter((produto) => produto.categoria?.id === categoria.id).length}</small></span>)}</div>
          </div>
          <div className="admin-box state-legend">
            <span className="eyebrow">estados</span><h3>Leitura rápida</h3>
            {Object.values(status).map((info) => <div key={info.label}><span className={`status-dot ${info.className}`} />{info.label}<strong>{produtos.filter((produto) => statusInfo(produto).label === info.label).length}</strong></div>)}
          </div>
        </div>
      </div>
      <div className="admin-box admin-inventory">
        <div className="admin-box-heading"><div><span className="eyebrow">estoque</span><h3>Produtos do catálogo</h3></div><small className="admin-result-count">{produtosFiltrados.length} de {produtos.length} produtos</small></div>
        <div className="admin-filterbar">
          <label className="admin-search"><span>⌕</span><input value={buscaProdutos} onChange={(event) => setBuscaProdutos(event.target.value)} placeholder="Nome ou categoria" /></label>
          <label className="admin-filter"><span>Categoria</span><select value={categoriaFiltroProduto} onChange={(event) => setCategoriaFiltroProduto(event.target.value)}><option value="">Todas</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select></label>
          <label className="admin-filter"><span>Estado</span><select value={estadoFiltroProduto} onChange={(event) => setEstadoFiltroProduto(event.target.value)}><option value="">Todos</option>{Object.entries(status).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select></label>
        </div>
        <div className="admin-product-table">
          <div className="admin-table-head"><span>Produto</span><span>Categoria</span><span>Preço</span><span>Estoque / estado</span><span>Ações</span></div>
          <div className="admin-scroll-list admin-product-scroll">
            {produtosFiltrados.map((produto) => <div className="admin-product-row" key={produto.id}>
              <strong>{produto.nome}</strong><span>{produto.categoria?.nome || 'Sem categoria'}</span><span>{Number(produto.preco).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</span><span>{produto.estoque} · {statusInfo(produto).label}</span><span className="admin-product-actions"><button className="btn btn-small btn-ghost" onClick={() => onEditar(produto)}>Editar</button><button className="btn btn-small btn-ghost" onClick={() => onDeletar(produto.id)}>Excluir</button></span>
            </div>)}
            {produtosFiltrados.length === 0 && <p className="admin-empty">Nenhum produto corresponde aos filtros.</p>}
          </div>
        </div>
      </div>
      <div className="admin-box admin-reviews">
        <div className="admin-box-heading"><div><span className="eyebrow">reputação</span><h3>Avaliações dos clientes</h3></div><small className="admin-result-count">{avaliacoesFiltradas.length} de {avaliacoes.length} avaliações</small></div>
        <div className="admin-filterbar admin-review-filters">
          <label className="admin-search"><span>⌕</span><input value={buscaAvaliacoes} onChange={(event) => setBuscaAvaliacoes(event.target.value)} placeholder="Nome, comentário ou produto" /></label>
          <label className="admin-filter"><span>Nota</span><select value={notaFiltroAvaliacao} onChange={(event) => setNotaFiltroAvaliacao(event.target.value)}><option value="">Todas</option>{[5, 4, 3, 2, 1].map((nota) => <option key={nota} value={nota}>{nota} estrelas</option>)}</select></label>
        </div>
        {avaliacoes.length === 0 ? <p>Nenhuma avaliação publicada ainda.</p> : <div className="admin-review-list admin-scroll-list">{avaliacoesFiltradas.map((avaliacao) => <article className="admin-review" key={avaliacao.id}>
          <div><strong>{avaliacao.nomeUsuario}</strong><small>Produto #{avaliacao.produtoId}</small></div>
          <span className="estrelas">{'★'.repeat(avaliacao.nota)}{'☆'.repeat(5 - avaliacao.nota)}</span>
          {avaliacao.comentario && <p>{avaliacao.comentario}</p>}
        </article>)}{avaliacoesFiltradas.length === 0 && <p className="admin-empty">Nenhuma avaliação corresponde aos filtros.</p>}</div>}
      </div>
      <div className="admin-columns">
        <div className="admin-box">
          <div className="admin-box-heading"><div><span className="eyebrow">contas</span><h3>Usuários</h3></div><small className="admin-result-count">{usuariosFiltrados.length} de {usuarios.length} usuários</small></div>
          <div className="admin-filterbar admin-people-filters">
            <label className="admin-search"><span>⌕</span><input value={buscaUsuarios} onChange={(event) => setBuscaUsuarios(event.target.value)} placeholder="Nome ou e-mail" /></label>
            <label className="admin-filter"><span>Perfil</span><select value={perfilFiltroUsuario} onChange={(event) => setPerfilFiltroUsuario(event.target.value)}><option value="">Todos</option><option value="ADMIN">Administrador</option><option value="USER">Cliente</option></select></label>
            <label className="admin-filter"><span>Situação</span><select value={estadoFiltroUsuario} onChange={(event) => setEstadoFiltroUsuario(event.target.value)}><option value="">Todas</option><option value="true">Ativos</option><option value="false">Inativos</option></select></label>
          </div>
          <div className="admin-table admin-scroll-list">{usuariosFiltrados.map((usuario) => <div className="admin-row" key={usuario.id}><span><strong>{usuario.nome}</strong><small>{usuario.email} · {usuario.perfil} · {usuario.ativo ? 'Ativo' : 'Inativo'}</small></span><button className="btn btn-small btn-ghost" disabled={usuario.perfil === 'ADMIN'} onClick={() => onAtualizarUsuario(usuario.id, !usuario.ativo)}>{usuario.ativo ? 'Desativar' : 'Ativar'}</button></div>)}{usuariosFiltrados.length === 0 && <p className="admin-empty">Nenhum usuário corresponde aos filtros.</p>}</div>
        </div>
        <div className="admin-box">
          <div className="admin-box-heading"><div><span className="eyebrow">pedidos</span><h3>Compras</h3></div><small className="admin-result-count">{comprasFiltradas.length} de {compras.length} compras</small></div>
          <div className="admin-filterbar admin-order-filters">
            <label className="admin-search"><span>⌕</span><input value={buscaCompras} onChange={(event) => setBuscaCompras(event.target.value)} placeholder="Número ou produto" /></label>
            <label className="admin-filter"><span>Situação</span><select value={estadoFiltroCompra} onChange={(event) => setEstadoFiltroCompra(event.target.value)}><option value="">Todas</option>{['CRIADA', 'PAGA', 'ENVIADA', 'ENTREGUE', 'CANCELADA'].map((estado) => <option key={estado} value={estado}>{estado}</option>)}</select></label>
          </div>
          <div className="admin-table admin-scroll-list">{comprasFiltradas.map((compra) => <div className="admin-row" key={compra.id}><span><strong>#{compra.id} · {compra.itens.map((item) => item.nomeProduto).join(', ')}</strong><small>Usuário #{compra.usuarioId} · {compra.status} · {Number(compra.total).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</small></span>{compra.status !== 'CANCELADA' && compra.status !== 'ENTREGUE' && <button className="btn btn-small btn-ghost" onClick={() => onCancelarCompra(compra.id)}>Cancelar</button>}</div>)}{comprasFiltradas.length === 0 && <p className="admin-empty">Nenhuma compra corresponde aos filtros.</p>}</div>
        </div>
      </div>
    </section>
  )
}

export default GestaoPage
