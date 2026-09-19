import { useCallback, useEffect, useMemo, useState } from 'react'
import { apiFetch } from './api'
import FeedbackToasts from './components/FeedbackToasts'
import SiteFooter from './components/SiteFooter'
import SiteHeader from './components/SiteHeader'
import AutenticacaoPage from './pages/AutenticacaoPage'
import CatalogoPage from './pages/CatalogoPage'
import ComprasPage from './pages/ComprasPage'
import GestaoPage from './pages/GestaoPage'
import { ORDENACOES, STATUS } from './pages/constants'
import './App.css'

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
    abrirAutenticacao('login')
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
    setNome(produto.nome)
    setDescricao(produto.descricao)
    setPreco(produto.preco)
    setEstoque(produto.estoque ?? 0)
    setStatusProduto(produto.status || 'ATIVO')
    setCategoriaProduto(produto.categoria?.id || '')
    setEditandoId(produto.id)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function limparFormulario() {
    setNome('')
    setDescricao('')
    setPreco('')
    setEstoque('10')
    setStatusProduto('ATIVO')
    setCategoriaProduto('')
    setEditandoId(null)
  }

  async function salvarProduto(event) {
    event.preventDefault()
    setErro('')
    try {
      const produto = { nome: nome.trim(), descricao: descricao.trim(), preco: Number(preco), estoque: Number(estoque), status: statusProduto, categoria: categoriaProduto ? { id: Number(categoriaProduto) } : null }
      await apiFetch(editandoId ? `/api/produtos/${editandoId}` : '/api/produtos', { method: editandoId ? 'PUT' : 'POST', body: JSON.stringify(produto) }, auth.token)
      limparFormulario()
      await carregar()
      setAviso('Catálogo atualizado.')
    } catch (error) { setErro(error.message) }
  }

  async function deletarProduto(id) {
    if (!window.confirm('Excluir este produto?')) return
    try { await apiFetch(`/api/produtos/${id}`, { method: 'DELETE' }, auth.token); await carregar(); setAviso('Produto removido.') } catch (error) { setErro(error.message) }
  }

  async function comprar(produto) {
    if (!auth) { abrirAutenticacao('login'); return }
    try {
      await apiFetch('/api/compras', { method: 'POST', body: JSON.stringify({ itens: [{ produtoId: produto.id, quantidade: 1 }] }) }, auth.token)
      await carregar()
      setAviso(`${produto.nome} adicionado às suas compras.`)
    } catch (error) { setErro(error.message) }
  }

  async function criarCategoria(event) {
    event.preventDefault()
    if (!novaCategoria.trim()) return
    try { await apiFetch('/api/categorias', { method: 'POST', body: JSON.stringify({ nome: novaCategoria.trim(), descricao: 'Categoria criada pelo administrador' }) }, auth.token); setNovaCategoria(''); await carregar() } catch (error) { setErro(error.message) }
  }

  function abrirAutenticacao(mode) {
    setAuthMode(mode)
    setMostrarAuth(true)
  }

  function formatarPreco(valor) { return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }
  function statusInfo(produto) { return STATUS[produto.status] || STATUS.ATIVO }
  function limparFiltros() { setBusca(''); setCategoriaId(''); setStatus(''); setPrecoMin(''); setPrecoMax(''); setOrdenacao('recente') }

  function navegar(novaAba) {
    setAba(novaAba)
  }

  function renderPagina() {
    if (mostrarAuth) return <AutenticacaoPage mode={authMode} onModeChange={setAuthMode} onAuthenticated={autenticar} />
    if (aba === 'compras') return <ComprasPage compras={compras} formatarPreco={formatarPreco} onExplorar={() => navegar('catalogo')} />
    if (aba === 'gestao' && isAdmin) return <GestaoPage categorias={categorias} produtos={produtos} status={STATUS} editandoId={editandoId} nome={nome} descricao={descricao} preco={preco} estoque={estoque} statusProduto={statusProduto} categoriaProduto={categoriaProduto} novaCategoria={novaCategoria} onNomeChange={setNome} onDescricaoChange={setDescricao} onPrecoChange={setPreco} onEstoqueChange={setEstoque} onStatusProdutoChange={setStatusProduto} onCategoriaProdutoChange={setCategoriaProduto} onNovaCategoriaChange={setNovaCategoria} onSalvarProduto={salvarProduto} onLimparFormulario={limparFormulario} onCriarCategoria={criarCategoria} statusInfo={statusInfo} />
    return <CatalogoPage produtos={produtos} produtosFiltrados={produtosFiltrados} categorias={categorias} auth={auth} isAdmin={isAdmin} busca={busca} categoriaId={categoriaId} status={status} precoMin={precoMin} precoMax={precoMax} ordenacao={ordenacao} avaliacoesAbertas={avaliacoesAbertas} statusOptions={STATUS} ordenacoes={ORDENACOES} formatarPreco={formatarPreco} statusInfo={statusInfo} onBuscaChange={setBusca} onCategoriaChange={setCategoriaId} onStatusChange={setStatus} onPrecoMinChange={setPrecoMin} onPrecoMaxChange={setPrecoMax} onOrdenacaoChange={setOrdenacao} onLimparFiltros={limparFiltros} onComprar={comprar} onEditar={editarProduto} onDeletar={deletarProduto} onToggleAvaliacoes={(id) => setAvaliacoesAbertas((atual) => ({ ...atual, [id]: !atual[id] }))} onRequireAuth={() => abrirAutenticacao('login')} />
  }

  return (
    <div className="app-shell">
      <SiteHeader auth={auth} isAdmin={isAdmin} aba={aba} onNavigate={navegar} onBrandClick={() => { setAba('catalogo'); setMostrarAuth(false) }} onShowAuth={abrirAutenticacao} onLogout={sair} />
      <main className="page-wrap">
        <FeedbackToasts erro={erro} aviso={aviso} onClearErro={() => setErro('')} onClearAviso={() => setAviso('')} />
        {renderPagina()}
      </main>
      <SiteFooter />
    </div>
  )
}

export default App
