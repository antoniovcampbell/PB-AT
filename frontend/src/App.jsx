import { useCallback, useEffect, useMemo, useState } from 'react'
import AvaliacoesPorProduto from './AvaliacoesPorProduto'
import AuthPanel from './AuthPanel'
import { apiFetch } from './api'
import './App.css'

const STATUS = {
  ATIVO: { label: 'Disponível', className: 'status-ativo' },
  ESTOQUE_BAIXO: { label: 'Últimas unidades', className: 'status-baixo' },
  ESGOTADO: { label: 'Esgotado', className: 'status-esgotado' },
  INATIVO: { label: 'Indisponível', className: 'status-inativo' }
}

const ORDENACOES = { recente: 'Mais recentes', nome: 'Nome A-Z', maiorPreco: 'Maior preço', menorPreco: 'Menor preço' }

function App() {
  const [auth, setAuth] = useState(() => {
    try { return JSON.parse(localStorage.getItem('pb-market-auth')) } catch { return null }
  })
  const [produtos, setProdutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [compras, setCompras] = useState([])
  const [busca, setBusca] = useState('')
  const [categoriaId, setCategoriaId] = useState('')
  const [status, setStatus] = useState('')
  const [precoMin, setPrecoMin] = useState('')
  const [precoMax, setPrecoMax] = useState('')
  const [ordenacao, setOrdenacao] = useState('recente')
  const [aba, setAba] = useState('catalogo')
  const [authMode, setAuthMode] = useState('login')
  const [mostrarAuth, setMostrarAuth] = useState(false)
  const [avaliacoesAbertas, setAvaliacoesAbertas] = useState({})
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [editandoId, setEditandoId] = useState(null)
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [preco, setPreco] = useState('')
  const [estoque, setEstoque] = useState('10')
  const [statusProduto, setStatusProduto] = useState('ATIVO')
  const [categoriaProduto, setCategoriaProduto] = useState('')
  const [novaCategoria, setNovaCategoria] = useState('')

  const isAdmin = auth?.usuario?.perfil === 'ADMIN'

  async function carregar() {
    try {
      setErro('')
      const [lista, categoriasLista] = await Promise.all([apiFetch('/api/produtos'), apiFetch('/api/categorias')])
      setProdutos(lista)
      setCategorias(categoriasLista)
    } catch (error) { setErro(error.message) }
  }

  useEffect(() => {
    // A primeira leitura sincroniza a tela com os serviços externos.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    carregar()
  }, [])

  const produtosFiltrados = useMemo(() => produtos.filter((produto) => {
    const termo = busca.trim().toLowerCase()
    const correspondeTexto = !termo || [produto.nome, produto.descricao, produto.categoria?.nome].filter(Boolean).some((campo) => String(campo).toLowerCase().includes(termo))
    const correspondeCategoria = !categoriaId || String(produto.categoria?.id) === String(categoriaId)
    const correspondeStatus = !status || (produto.status || 'ATIVO') === status
    const correspondeMin = !precoMin || Number(produto.preco) >= Number(precoMin)
    const correspondeMax = !precoMax || Number(produto.preco) <= Number(precoMax)
    return correspondeTexto && correspondeCategoria && correspondeStatus && correspondeMin && correspondeMax
  }).sort((a, b) => {
    if (ordenacao === 'nome') return a.nome.localeCompare(b.nome)
    if (ordenacao === 'maiorPreco') return Number(b.preco) - Number(a.preco)
    if (ordenacao === 'menorPreco') return Number(a.preco) - Number(b.preco)
    return Number(b.id) - Number(a.id)
  }), [produtos, busca, categoriaId, status, precoMin, precoMax, ordenacao])

  function autenticar(resposta) {
    setAuth(resposta)
    localStorage.setItem('pb-market-auth', JSON.stringify(resposta))
    setMostrarAuth(false)
    setAviso(`Olá, ${resposta.usuario.nome.split(' ')[0]}!`)
  }

  function sair() {
    setAuth(null)
    localStorage.removeItem('pb-market-auth')
    setCompras([])
    setAba('catalogo')
  }

  const carregarCompras = useCallback(async () => {
    if (!auth) return
    try { setCompras(await apiFetch('/api/compras/minhas', {}, auth.token)) } catch (error) { setErro(error.message) }
  }, [auth])

  useEffect(() => {
    if (aba === 'compras' && auth) {
      // A aba de compras depende da sessão persistida e de uma fonte externa.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      carregarCompras()
    }
  }, [aba, auth, carregarCompras])

  function editarProduto(produto) {
    setNome(produto.nome); setDescricao(produto.descricao); setPreco(produto.preco); setEstoque(produto.estoque ?? 0); setStatusProduto(produto.status || 'ATIVO'); setCategoriaProduto(produto.categoria?.id || ''); setEditandoId(produto.id); window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function limparFormulario() {
    setNome(''); setDescricao(''); setPreco(''); setEstoque('10'); setStatusProduto('ATIVO'); setCategoriaProduto(''); setEditandoId(null)
  }

  async function salvarProduto(event) {
    event.preventDefault(); setErro('')
    try {
      const produto = { nome: nome.trim(), descricao: descricao.trim(), preco: Number(preco), estoque: Number(estoque), status: statusProduto, categoria: categoriaProduto ? { id: Number(categoriaProduto) } : null }
      await apiFetch(editandoId ? `/api/produtos/${editandoId}` : '/api/produtos', { method: editandoId ? 'PUT' : 'POST', body: JSON.stringify(produto) }, auth.token)
      limparFormulario(); await carregar(); setAviso('Catálogo atualizado.')
    } catch (error) { setErro(error.message) }
  }

  async function deletarProduto(id) {
    if (!window.confirm('Excluir este produto?')) return
    try { await apiFetch(`/api/produtos/${id}`, { method: 'DELETE' }, auth.token); await carregar(); setAviso('Produto removido.') } catch (error) { setErro(error.message) }
  }

  async function comprar(produto) {
    if (!auth) { setAuthMode('login'); setMostrarAuth(true); return }
    try {
      await apiFetch('/api/compras', { method: 'POST', body: JSON.stringify({ itens: [{ produtoId: produto.id, quantidade: 1 }] }) }, auth.token)
      await carregar(); setAviso(`${produto.nome} adicionado às suas compras.`)
    } catch (error) { setErro(error.message) }
  }

  async function criarCategoria(event) {
    event.preventDefault()
    if (!novaCategoria.trim()) return
    try { await apiFetch('/api/categorias', { method: 'POST', body: JSON.stringify({ nome: novaCategoria.trim(), descricao: 'Categoria criada pelo administrador' }) }, auth.token); setNovaCategoria(''); await carregar() } catch (error) { setErro(error.message) }
  }

  function formatarPreco(valor) { return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }
  function statusInfo(produto) { return STATUS[produto.status] || STATUS.ATIVO }
  function limparFiltros() { setBusca(''); setCategoriaId(''); setStatus(''); setPrecoMin(''); setPrecoMax(''); setOrdenacao('recente') }

  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand" onClick={() => { setAba('catalogo'); setMostrarAuth(false) }}><span className="brand-mark">PB</span><span><strong>market</strong><small>curadoria para o cotidiano</small></span></button>
        <nav className="main-nav"><button className={aba === 'catalogo' ? 'nav-active' : ''} onClick={() => setAba('catalogo')}>Explorar</button>{auth && <button className={aba === 'compras' ? 'nav-active' : ''} onClick={() => setAba('compras')}>Minhas compras</button>}{isAdmin && <button className={aba === 'gestao' ? 'nav-active' : ''} onClick={() => setAba('gestao')}>Gestão</button>}</nav>
        <div className="account-area">{auth ? <><span className="account-avatar">{auth.usuario.nome.charAt(0)}</span><span className="account-name"><strong>{auth.usuario.nome}</strong><small>{isAdmin ? 'Administrador' : 'Cliente'}</small></span><button className="btn btn-ghost btn-small" onClick={sair}>Sair</button></> : <><button className="btn btn-ghost btn-small" onClick={() => { setAuthMode('login'); setMostrarAuth(true) }}>Entrar</button><button className="btn btn-primario btn-small" onClick={() => { setAuthMode('register'); setMostrarAuth(true) }}>Criar conta</button></>}</div>
      </header>

      <main className="page-wrap">
        {erro && <div className="toast toast-error">{erro}<button onClick={() => setErro('')}>×</button></div>}
        {aviso && <div className="toast toast-success">{aviso}<button onClick={() => setAviso('')}>×</button></div>}
        {mostrarAuth ? <AuthPanel mode={authMode} onModeChange={setAuthMode} onAuthenticated={autenticar} /> : (
          <>
            {aba === 'catalogo' && <>
               <section className="hero-section"><div><span className="eyebrow">coleção de setembro · 2026</span><h1>Escolhas boas para<br /><em>dias mais leves.</em></h1><p>Produtos selecionados, preços claros e avaliações de quem realmente comprou.</p><button className="hero-link" onClick={() => document.getElementById('catalogo')?.scrollIntoView({ behavior: 'smooth' })}>Ver coleção <span>↓</span></button></div></section>
              <section className="insight-row"><div><span className="insight-number">{produtos.length}</span><span>itens no catálogo</span></div><div><span className="insight-number">{produtos.filter((produto) => produto.status === 'ATIVO').length}</span><span>prontos para envio</span></div><div><span className="insight-number">{categorias.length}</span><span>universos para explorar</span></div><div className="insight-note">Atualizado em tempo real<br /><strong>● sistema online</strong></div></section>
              <section id="catalogo" className="catalog-section"><div className="section-heading"><div><span className="eyebrow">catálogo</span><h2>Encontre seu próximo favorito</h2></div><span className="result-count">{produtosFiltrados.length} resultados</span></div>
                <div className="filter-panel"><div className="search-field"><span>⌕</span><input value={busca} onChange={(event) => setBusca(event.target.value)} placeholder="Buscar por nome, descrição ou categoria" /></div><select value={categoriaId} onChange={(event) => setCategoriaId(event.target.value)}><option value="">Todas as categorias</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select><select value={status} onChange={(event) => setStatus(event.target.value)}><option value="">Todos os estados</option>{Object.entries(STATUS).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select><div className="price-range"><input type="number" min="0" placeholder="R$ mínimo" value={precoMin} onChange={(event) => setPrecoMin(event.target.value)} /><span>até</span><input type="number" min="0" placeholder="R$ máximo" value={precoMax} onChange={(event) => setPrecoMax(event.target.value)} /></div><select value={ordenacao} onChange={(event) => setOrdenacao(event.target.value)}>{Object.entries(ORDENACOES).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select><button className="filter-clear" onClick={limparFiltros}>Limpar</button></div>
                <div className="product-grid">{produtosFiltrados.length === 0 ? <div className="empty-state"><span>◌</span><h3>Nenhuma escolha por aqui</h3><p>Remova alguns filtros para abrir o catálogo.</p></div> : produtosFiltrados.map((produto) => { const info = statusInfo(produto); return <article className="product-card" key={produto.id}><div className="product-visual"><span className="product-id">#{String(produto.id).padStart(2, '0')}</span><span className={`status-pill ${info.className}`}>{info.label}</span><div className="product-glyph">{produto.nome.charAt(0)}</div><span className="product-category">{produto.categoria?.nome || 'Coleção PB'}</span></div><div className="product-content"><h3>{produto.nome}</h3><p>{produto.descricao}</p><div className="product-bottom"><div><strong>{formatarPreco(produto.preco)}</strong><small>{produto.estoque > 0 ? `${produto.estoque} em estoque` : 'Sem estoque'}</small></div><button className="buy-button" disabled={info.className !== 'status-ativo' && info.className !== 'status-baixo'} onClick={() => comprar(produto)}>{auth ? 'Comprar' : 'Entrar para comprar'} <span>↗</span></button></div></div><div className="product-actions">{isAdmin && <><button onClick={() => editarProduto(produto)}>Editar</button><button onClick={() => deletarProduto(produto.id)}>Excluir</button></>}<button onClick={() => setAvaliacoesAbertas((atual) => ({ ...atual, [produto.id]: !atual[produto.id] }))}>{avaliacoesAbertas[produto.id] ? 'Fechar avaliações' : 'Ver avaliações'}</button></div>{avaliacoesAbertas[produto.id] && <AvaliacoesPorProduto produtoId={produto.id} auth={auth} onRequireAuth={() => { setAuthMode('login'); setMostrarAuth(true) }} />}</article> })}</div>
              </section>
            </>}
            {aba === 'compras' && <section className="dashboard-section"><div className="section-heading"><div><span className="eyebrow">área do cliente</span><h2>Minhas compras</h2></div></div>{compras.length === 0 ? <div className="empty-state"><span>✦</span><h3>Seu histórico começa aqui</h3><p>Compre um produto para liberar avaliações verificadas e acompanhar seu pedido.</p><button className="btn btn-primario" onClick={() => setAba('catalogo')}>Explorar catálogo</button></div> : <div className="purchase-list">{compras.map((compra) => <article className="purchase-card" key={compra.id}><div><span className="eyebrow">compra #{compra.id}</span><h3>{compra.itens.map((item) => item.nomeProduto).join(' · ')}</h3><small>{new Date(compra.criadaEm).toLocaleString('pt-BR')}</small></div><div className="purchase-side"><span className="purchase-status">{compra.status}</span><strong>{formatarPreco(compra.total)}</strong></div></article>)}</div>}</section>}
            {aba === 'gestao' && isAdmin && <section className="dashboard-section"><div className="section-heading"><div><span className="eyebrow">área administrativa</span><h2>Operação do catálogo</h2></div></div><div className="admin-layout"><form className="admin-form" onSubmit={salvarProduto}><div className="form-heading"><span className="eyebrow">{editandoId ? `edição #${editandoId}` : 'novo registro'}</span><h3>{editandoId ? 'Atualizar produto' : 'Adicionar produto'}</h3></div><label className="campo"><span>Nome</span><input value={nome} onChange={(event) => setNome(event.target.value)} required /></label><label className="campo"><span>Descrição</span><textarea value={descricao} onChange={(event) => setDescricao(event.target.value)} required /></label><div className="form-line"><label className="campo"><span>Preço</span><input type="number" min="0.01" step="0.01" value={preco} onChange={(event) => setPreco(event.target.value)} required /></label><label className="campo"><span>Estoque</span><input type="number" min="0" value={estoque} onChange={(event) => setEstoque(event.target.value)} required /></label></div><div className="form-line"><label className="campo"><span>Categoria</span><select value={categoriaProduto} onChange={(event) => setCategoriaProduto(event.target.value)}><option value="">Sem categoria</option>{categorias.map((categoria) => <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>)}</select></label><label className="campo"><span>Estado</span><select value={statusProduto} onChange={(event) => setStatusProduto(event.target.value)}>{Object.entries(STATUS).map(([value, info]) => <option key={value} value={value}>{info.label}</option>)}</select></label></div><div className="form-actions"><button type="submit" className="btn btn-primario">{editandoId ? 'Salvar alterações' : 'Cadastrar produto'}</button>{editandoId && <button type="button" className="btn btn-ghost" onClick={limparFormulario}>Cancelar</button>}</div></form><div className="admin-side"><div className="admin-box"><span className="eyebrow">taxonomia</span><h3>Categorias</h3><p>Organize o catálogo por universos que ajudam a escolher.</p><form className="inline-form" onSubmit={criarCategoria}><input value={novaCategoria} onChange={(event) => setNovaCategoria(event.target.value)} placeholder="Nova categoria" /><button className="btn btn-secundario">Adicionar</button></form><div className="category-list">{categorias.map((categoria) => <span key={categoria.id}>{categoria.nome}<small>{produtos.filter((produto) => produto.categoria?.id === categoria.id).length}</small></span>)}</div></div><div className="admin-box state-legend"><span className="eyebrow">estados</span><h3>Leitura rápida</h3>{Object.values(STATUS).map((info) => <div key={info.label}><span className={`status-dot ${info.className}`} />{info.label}<strong>{produtos.filter((produto) => statusInfo(produto).label === info.label).length}</strong></div>)}</div></div></div></section>}
          </>
        )}
      </main>
      <footer className="footer"><span>PB market</span><span>Compra consciente, experiência simples.</span><span>© 2026</span></footer>
    </div>
  )
}

export default App
