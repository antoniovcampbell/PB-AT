import AuthPanel from '../AuthPanel'

function AutenticacaoPage({ mode, onModeChange, onAuthenticated }) {
  return <AuthPanel mode={mode} onModeChange={onModeChange} onAuthenticated={onAuthenticated} />
}

export default AutenticacaoPage
