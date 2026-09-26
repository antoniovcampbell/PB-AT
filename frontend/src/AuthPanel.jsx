import { useState } from 'react'
import { apiFetch } from './api'
import './styles/auth.css'

function AuthPanel({ mode, onModeChange, onAuthenticated }) {
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  async function enviar(event) {
    event.preventDefault()
    setErro('')
    setCarregando(true)
    try {
      const resposta = await apiFetch(`/api/auth/${mode === 'login' ? 'login' : 'register'}`, {
        method: 'POST',
        body: JSON.stringify(mode === 'login' ? { email, senha } : { nome, email, senha })
      })
      onAuthenticated(resposta)
    } catch (error) {
      setErro(error.message)
    } finally {
      setCarregando(false)
    }
  }

  return (
    <section className="auth-screen">
      <div className="auth-copy">
        <span className="eyebrow">PB Market / acesso seguro</span>
         <h2>{mode === 'login' ? 'Acompanhe seus pedidos e avaliações.' : 'Crie sua conta para comprar e acompanhar pedidos.'}</h2>
         <p>{mode === 'login' ? 'Entre para consultar seu histórico, ver o status das compras e avaliar os produtos que comprou.' : 'Com sua conta, você pode guardar produtos no carrinho, acompanhar pedidos e publicar avaliações vinculadas às compras.'}</p>
         <div className="auth-points"><span>● Histórico de compras</span><span>● Avaliações vinculadas a pedidos</span><span>● Carrinho salvo na conta</span></div>
      </div>
      <form className="auth-card" onSubmit={enviar}>
        <div className="auth-card-header">
          <span className="brand-mark">PB</span>
          <div><strong>{mode === 'login' ? 'Entrar' : 'Criar conta'}</strong><small>PB Market</small></div>
        </div>
        {mode === 'register' && <label className="campo"><span>Nome</span><input value={nome} onChange={(event) => setNome(event.target.value)} placeholder="Como podemos chamar você?" required /></label>}
        <label className="campo"><span>E-mail</span><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="seuemail@exemplo.com" required /></label>
        <label className="campo"><span>Senha</span><input type="password" minLength="6" value={senha} onChange={(event) => setSenha(event.target.value)} placeholder="Pelo menos 6 caracteres" required /></label>
        {erro && <p className="erro">{erro}</p>}
        <button className="btn btn-primario btn-full" disabled={carregando}>{carregando ? (mode === 'login' ? 'Acessando sua conta…' : 'Criando sua conta…') : mode === 'login' ? 'Entrar na conta' : 'Criar minha conta'}</button>
        <p className="auth-switch">{mode === 'login' ? 'Ainda não tem conta?' : 'Já tem uma conta?'} <button type="button" className="btn-link-inline" onClick={() => { setErro(''); onModeChange(mode === 'login' ? 'register' : 'login') }}>{mode === 'login' ? 'Cadastre-se' : 'Faça login'}</button></p>
        <small className="demo-hint">Acessos de demonstração:<br />Administrador: admin@pbat.local / admin123<br />Cliente: user@pbat.local / user123</small>
      </form>
    </section>
  )
}

export default AuthPanel
