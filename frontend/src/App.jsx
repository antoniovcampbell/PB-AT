import { useState } from 'react'
import FeedbackToasts from './components/FeedbackToasts'
import SiteFooter from './components/SiteFooter'
import SiteHeader from './components/SiteHeader'
import { useAuth } from './hooks/useAuth'
import { useCatalogo } from './hooks/useCatalogo'
import { useCommerce } from './hooks/useCommerce'
import { useGestao } from './hooks/useGestao'
import { useProdutoForm } from './hooks/useProdutoForm'
import AutenticacaoPage from './pages/AutenticacaoPage'
import CarrinhoPage from './pages/CarrinhoPage'
import CatalogoPage from './pages/CatalogoPage'
import ComprasPage from './pages/ComprasPage'
import GestaoPage from './pages/GestaoPage'
import ProdutoDetalhePage from './pages/ProdutoDetalhePage'
import { ORDENACOES, STATUS } from './pages/constants'

function App() {
  const [aba, setAba] = useState('catalogo')
  const [produtoSelecionado, setProdutoSelecionado] = useState(null)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const { auth, autenticar: salvarSessao, sair, authMode, setAuthMode, mostrarAuth, setMostrarAuth, abrirAutenticacao } = useAuth()
  const isAdmin = auth?.usuario?.perfil === 'ADMIN'
  const catalogo = useCatalogo(setErro)
  const produtoForm = useProdutoForm(auth, catalogo.carregar, setErro, setAviso)
  const commerce = useCommerce(auth, setErro, setAviso)
  const gestao = useGestao(auth, isAdmin, aba, setErro, setAviso, catalogo.carregar)

  function autenticar(resposta) {
    salvarSessao(resposta)
    setAviso(`Olá, ${resposta.usuario.nome.split(' ')[0]}!`)
  }

  function logout() {
    sair()
    setAba('catalogo')
    setProdutoSelecionado(null)
    abrirAutenticacao('login')
  }

  function formatarPreco(valor) {
    return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
  }

  function navegar(novaAba) {
    setAba(novaAba)
    setProdutoSelecionado(null)
    setMostrarAuth(false)
  }

  function renderPagina() {
    if (mostrarAuth) return <AutenticacaoPage mode={authMode} onModeChange={setAuthMode} onAuthenticated={autenticar} />
    if (produtoSelecionado) return <ProdutoDetalhePage produto={produtoSelecionado} auth={auth} formatarPreco={formatarPreco} statusInfo={catalogo.statusInfo} onVoltar={() => setProdutoSelecionado(null)} onComprar={(produto) => commerce.adicionarAoCarrinho(produto, () => abrirAutenticacao('login'))} />
    if (aba === 'compras') return <ComprasPage compras={commerce.compras} avaliacoes={commerce.avaliacoesMinhas} auth={auth} formatarPreco={formatarPreco} onExplorar={() => navegar('catalogo')} onAvaliacaoSalva={commerce.carregarCompras} />
    if (aba === 'carrinho') return <CarrinhoPage carrinho={commerce.carrinho} formatarPreco={formatarPreco} onAtualizar={commerce.atualizarCarrinho} onRemover={commerce.removerCarrinho} onFinalizar={commerce.finalizarCarrinho} onExplorar={() => navegar('catalogo')} />
    if (aba === 'gestao' && isAdmin) return <GestaoPage categorias={catalogo.categorias} produtos={catalogo.produtos} avaliacoes={gestao.avaliacoes} usuarios={gestao.usuarios} compras={gestao.comprasAdmin} status={STATUS} {...produtoForm} onNomeChange={produtoForm.setNome} onDescricaoChange={produtoForm.setDescricao} onPrecoChange={produtoForm.setPreco} onEstoqueChange={produtoForm.setEstoque} onStatusProdutoChange={produtoForm.setStatusProduto} onCategoriaProdutoChange={produtoForm.setCategoriaProduto} onNovaCategoriaChange={produtoForm.setNovaCategoria} onSalvarProduto={produtoForm.salvarProduto} onLimparFormulario={produtoForm.limparFormulario} onCriarCategoria={produtoForm.criarCategoria} onAtualizarUsuario={gestao.atualizarUsuario} onCancelarCompra={gestao.cancelarCompra} statusInfo={catalogo.statusInfo} />
    return <CatalogoPage produtos={catalogo.produtos} produtosFiltrados={catalogo.produtosFiltrados} categorias={catalogo.categorias} auth={auth} isAdmin={isAdmin} busca={catalogo.busca} categoriaId={catalogo.categoriaId} status={catalogo.status} precoMin={catalogo.precoMin} precoMax={catalogo.precoMax} ordenacao={catalogo.ordenacao} statusOptions={STATUS} ordenacoes={ORDENACOES} formatarPreco={formatarPreco} statusInfo={catalogo.statusInfo} onBuscaChange={catalogo.setBusca} onCategoriaChange={catalogo.setCategoriaId} onStatusChange={catalogo.setStatus} onPrecoMinChange={catalogo.setPrecoMin} onPrecoMaxChange={catalogo.setPrecoMax} onOrdenacaoChange={catalogo.setOrdenacao} onLimparFiltros={catalogo.limparFiltros} onComprar={(produto) => commerce.adicionarAoCarrinho(produto, () => abrirAutenticacao('login'))} onSelecionarProduto={setProdutoSelecionado} onEditar={produtoForm.editarProduto} onDeletar={produtoForm.deletarProduto} />
  }

  return (
    <div className="app-shell">
      <SiteHeader auth={auth} isAdmin={isAdmin} aba={aba} onNavigate={navegar} onBrandClick={() => navegar('catalogo')} onShowAuth={abrirAutenticacao} onLogout={logout} />
      <main className="page-wrap">
        <FeedbackToasts erro={erro} aviso={aviso} onClearErro={() => setErro('')} onClearAviso={() => setAviso('')} />
        {renderPagina()}
      </main>
      <SiteFooter />
    </div>
  )
}

export default App
