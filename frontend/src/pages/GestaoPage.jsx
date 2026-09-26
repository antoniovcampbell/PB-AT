import { useMemo, useState } from 'react'
import '../styles/admin.css'

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
  const [buscaUsuarios, setBuscaUsuarios] = useState('')
  const [buscaCompras, setBuscaCompras] = useState('')
  const produtosFiltrados = useMemo(() => produtos.filter((produto) =>
    `${produto.nome} ${produto.categoria?.nome || ''}`.toLocaleLowerCase('pt-BR').includes(buscaProdutos.toLocaleLowerCase('pt-BR'))), [produtos, buscaProdutos])
  const usuariosFiltrados = useMemo(() => usuarios.filter((usuario) =>
    `${usuario.nome} ${usuario.email} ${usuario.perfil}`.toLocaleLowerCase('pt-BR').includes(buscaUsuarios.toLocaleLowerCase('pt-BR'))), [usuarios, buscaUsuarios])
  const comprasFiltradas = useMemo(() => compras.filter((compra) =>
    `#${compra.id} ${compra.itens.map((item) => item.nomeProduto).join(' ')} ${compra.status}`.toLocaleLowerCase('pt-BR').includes(buscaCompras.toLocaleLowerCase('pt-BR'))), [compras, buscaCompras])

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
        <div className="admin-box-heading"><div><span className="eyebrow">estoque</span><h3>Produtos do catálogo</h3></div><label className="admin-search"><span>⌕</span><input value={buscaProdutos} onChange={(event) => setBuscaProdutos(event.target.value)} placeholder="Buscar produtos" /></label></div>
        <div className="admin-table admin-product-table">
          <div className="admin-table-head"><span>Produto</span><span>Categoria</span><span>Preço</span><span>Estoque / estado</span><span>Ações</span></div>
          {produtosFiltrados.map((produto) => <div className="admin-product-row" key={produto.id}>
            <strong>{produto.nome}</strong><span>{produto.categoria?.nome || 'Sem categoria'}</span><span>{Number(produto.preco).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</span><span>{produto.estoque} · {statusInfo(produto).label}</span><span className="admin-product-actions"><button className="btn btn-small btn-ghost" onClick={() => onEditar(produto)}>Editar</button><button className="btn btn-small btn-ghost" onClick={() => onDeletar(produto.id)}>Excluir</button></span>
          </div>)}
          {produtosFiltrados.length === 0 && <p className="admin-empty">Nenhum produto corresponde à busca.</p>}
        </div>
      </div>
      <div className="admin-box admin-reviews">
        <span className="eyebrow">reputação</span><h3>Avaliações dos clientes</h3>
        {avaliacoes.length === 0 ? <p>Nenhuma avaliação publicada ainda.</p> : <div className="admin-review-list">{avaliacoes.map((avaliacao) => <article className="admin-review" key={avaliacao.id}>
          <div><strong>{avaliacao.nomeUsuario}</strong><small>Produto #{avaliacao.produtoId}</small></div>
          <span className="estrelas">{'★'.repeat(avaliacao.nota)}{'☆'.repeat(5 - avaliacao.nota)}</span>
          {avaliacao.comentario && <p>{avaliacao.comentario}</p>}
        </article>)}</div>}
      </div>
      <div className="admin-columns">
        <div className="admin-box">
          <div className="admin-box-heading"><div><span className="eyebrow">contas</span><h3>Usuários</h3></div><label className="admin-search"><span>⌕</span><input value={buscaUsuarios} onChange={(event) => setBuscaUsuarios(event.target.value)} placeholder="Nome, e-mail ou perfil" /></label></div>
          <div className="admin-table">{usuariosFiltrados.map((usuario) => <div className="admin-row" key={usuario.id}><span><strong>{usuario.nome}</strong><small>{usuario.email} · {usuario.perfil} · {usuario.ativo ? 'Ativo' : 'Inativo'}</small></span><button className="btn btn-small btn-ghost" disabled={usuario.perfil === 'ADMIN'} onClick={() => onAtualizarUsuario(usuario.id, !usuario.ativo)}>{usuario.ativo ? 'Desativar' : 'Ativar'}</button></div>)}{usuariosFiltrados.length === 0 && <p className="admin-empty">Nenhum usuário encontrado.</p>}</div>
        </div>
        <div className="admin-box">
          <div className="admin-box-heading"><div><span className="eyebrow">pedidos</span><h3>Compras</h3></div><label className="admin-search"><span>⌕</span><input value={buscaCompras} onChange={(event) => setBuscaCompras(event.target.value)} placeholder="Buscar pedidos" /></label></div>
          <div className="admin-table">{comprasFiltradas.map((compra) => <div className="admin-row" key={compra.id}><span><strong>#{compra.id} · {compra.itens.map((item) => item.nomeProduto).join(', ')}</strong><small>Usuário #{compra.usuarioId} · {compra.status} · {Number(compra.total).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</small></span>{compra.status !== 'CANCELADA' && compra.status !== 'ENTREGUE' && <button className="btn btn-small btn-ghost" onClick={() => onCancelarCompra(compra.id)}>Cancelar</button>}</div>)}{comprasFiltradas.length === 0 && <p className="admin-empty">Nenhuma compra encontrada.</p>}</div>
        </div>
      </div>
    </section>
  )
}

export default GestaoPage
