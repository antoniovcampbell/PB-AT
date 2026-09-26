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
        <h2>{mode === 'login' ? 'Volte a comprar com contexto.' : 'Crie seu espaço de compras.'}</h2>
        <p>{mode === 'login' ? 'Entre para acompanhar compras, desbloquear avaliações e continuar de onde parou.' : 'Uma conta simples para comprar, acompanhar pedidos e avaliar apenas o que você recebeu.'}</p>
        <div className="auth-points"><span>● Compras demonstrativas</span><span>● Avaliações verificadas</span><span>● Catálogo em tempo real</span></div>
      </div>
      <form className="auth-card" onSubmit={enviar}>
        <div className="auth-card-header">
          <span className="brand-mark">PB</span>
          <div><strong>{mode === 'login' ? 'Entrar' : 'Criar conta'}</strong><small>PB Market</small></div>
        </div>
        {mode === 'register' && <label className="campo"><span>Nome</span><input value={nome} onChange={(event) => setNome(event.target.value)} placeholder="Como podemos chamar você?" required /></label>}
        <label className="campo"><span>E-mail</span><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="voce@exemplo.com" required /></label>
        <label className="campo"><span>Senha</span><input type="password" minLength="6" value={senha} onChange={(event) => setSenha(event.target.value)} placeholder="Mínimo de 6 caracteres" required /></label>
        {erro && <p className="erro">{erro}</p>}
        <button className="btn btn-primario btn-full" disabled={carregando}>{carregando ? 'Processando...' : mode === 'login' ? 'Entrar na conta' : 'Criar minha conta'}</button>
        <p className="auth-switch">{mode === 'login' ? 'Ainda não tem conta?' : 'Já tem uma conta?'} <button type="button" className="btn-link-inline" onClick={() => { setErro(''); onModeChange(mode === 'login' ? 'register' : 'login') }}>{mode === 'login' ? 'Cadastre-se' : 'Faça login'}</button></p>
        <small className="demo-hint">Demo admin: admin@pbat.local / admin123<br />Demo cliente: user@pbat.local / user123</small>
      </form>
    </section>
  )
}

export default AuthPanel
