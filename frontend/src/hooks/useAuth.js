import { useState } from 'react'

function recuperarSessao() {
  try { return JSON.parse(localStorage.getItem('pb-market-auth')) } catch { return null }
}

export function useAuth() {
  const [auth, setAuth] = useState(recuperarSessao)
  const [authMode, setAuthMode] = useState('login')
  const [mostrarAuth, setMostrarAuth] = useState(false)

  function autenticar(resposta) {
    setAuth(resposta)
    localStorage.setItem('pb-market-auth', JSON.stringify(resposta))
    setMostrarAuth(false)
  }

  function sair() {
    setAuth(null)
    localStorage.removeItem('pb-market-auth')
  }

  function abrirAutenticacao(mode) {
    setAuthMode(mode)
    setMostrarAuth(true)
  }

  return { auth, autenticar, sair, authMode, setAuthMode, mostrarAuth, setMostrarAuth, abrirAutenticacao }
}
