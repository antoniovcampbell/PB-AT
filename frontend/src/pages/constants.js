export const STATUS = {
  ATIVO: { label: 'Disponível', className: 'status-ativo' },
  ESTOQUE_BAIXO: { label: 'Últimas unidades', className: 'status-baixo' },
  ESGOTADO: { label: 'Esgotado', className: 'status-esgotado' },
  INATIVO: { label: 'Indisponível', className: 'status-inativo' }
}

export const ORDENACOES = {
  recente: 'Mais recentes',
  nome: 'Nome A-Z',
  maiorPreco: 'Maior preço',
  menorPreco: 'Menor preço'
}

export const STATUS_COMPRA = {
  CRIADA: 'Criada',
  PAGA: 'Paga',
  ENVIADA: 'Enviada',
  ENTREGUE: 'Entregue',
  CANCELADA: 'Cancelada'
}

export const TRANSICOES_STATUS_COMPRA = {
  CRIADA: ['CRIADA', 'PAGA', 'ENVIADA', 'ENTREGUE'],
  PAGA: ['PAGA', 'ENVIADA', 'ENTREGUE'],
  ENVIADA: ['ENVIADA', 'ENTREGUE'],
  ENTREGUE: ['ENTREGUE', 'ENVIADA'],
  CANCELADA: ['CANCELADA', 'CRIADA']
}
