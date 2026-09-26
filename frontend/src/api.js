export async function apiFetch(path, options = {}, token = '') {
  const headers = new Headers(options.headers || {})
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let response
  try {
    response = await fetch(path, { ...options, headers })
  } catch {
    throw new Error('Não foi possível conectar à loja. Verifique sua conexão e tente novamente.')
  }
  const text = await response.text()
  let data
  try {
    data = text ? JSON.parse(text) : null
  } catch {
    data = text
  }
  if (!response.ok) {
    const mensagemPorStatus = {
      401: 'Não foi possível autenticar. Confira seu e-mail e senha ou entre novamente.',
      403: 'Sua conta não tem permissão para realizar esta ação.',
      404: 'O item solicitado não foi encontrado. Atualize a página e tente novamente.',
      409: 'Esta ação não pode ser concluída porque os dados foram alterados. Atualize a página.',
      429: 'Muitas solicitações em pouco tempo. Aguarde um instante e tente novamente.',
    }
    const mensagem = data?.detail || data?.message || data?.error
      || (typeof data === 'string' && data.trim() ? data : null)
      || mensagemPorStatus[response.status]
      || 'A loja não conseguiu concluir esta solicitação. Tente novamente em instantes.'
    throw new Error(mensagem)
  }
  return data
}
