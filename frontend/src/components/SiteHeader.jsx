function SiteHeader({ auth, isAdmin, aba, onNavigate, onBrandClick, onShowAuth, onLogout }) {
  return (
    <header className="topbar">
      <button className="brand" onClick={onBrandClick}>
        <span className="brand-mark">PB</span>
        <span><strong>market</strong><small>curadoria para o cotidiano</small></span>
      </button>
      <nav className="main-nav">
        <button className={aba === 'catalogo' ? 'nav-active' : ''} onClick={() => onNavigate('catalogo')}>Explorar</button>
        {auth && <button className={aba === 'compras' ? 'nav-active' : ''} onClick={() => onNavigate('compras')}>Minhas compras</button>}
        {isAdmin && <button className={aba === 'gestao' ? 'nav-active' : ''} onClick={() => onNavigate('gestao')}>Gestão</button>}
      </nav>
      <div className="account-area">
        {auth ? <>
          <span className="account-avatar">{auth.usuario.nome.charAt(0)}</span>
          <span className="account-name"><strong>{auth.usuario.nome}</strong><small>{isAdmin ? 'Administrador' : 'Cliente'}</small></span>
          <button className="btn btn-ghost btn-small" onClick={onLogout}>Sair</button>
        </> : <>
          <button className="btn btn-ghost btn-small" onClick={() => onShowAuth('login')}>Entrar</button>
          <button className="btn btn-primario btn-small" onClick={() => onShowAuth('register')}>Criar conta</button>
        </>}
      </div>
    </header>
  )
}

export default SiteHeader
