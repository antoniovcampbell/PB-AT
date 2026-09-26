import '../styles/feedback.css'

function FeedbackToasts({ erro, aviso, onClearErro, onClearAviso }) {
  return <>
    {erro && <div className="toast toast-error">{erro}<button onClick={onClearErro}>×</button></div>}
    {aviso && <div className="toast toast-success">{aviso}<button onClick={onClearAviso}>×</button></div>}
  </>
}

export default FeedbackToasts
