const EVENT_LABELS = {
  MS: '男单',
  WS: '女单',
  MD: '男双',
  WD: '女双',
  XD: '混双'
}

const FORMAT_OPTIONS = ['单败淘汰制', '循环赛制', '双败淘汰制', '瑞士轮制']

export function normalizeTournamentEventType(item) {
  const raw = String(item?.eventType || '').trim().toUpperCase()
  if (EVENT_LABELS[raw]) return raw

  const legacy = String(item?.tournamentType || item?.type || '').trim().toUpperCase()
  if (legacy === 'SINGLE') return 'MS'
  if (legacy === 'DOUBLE') return 'MD'
  if (legacy === 'MIXED') return 'XD'

  const source = `${item?.tournamentName || ''}${item?.description || ''}${item?.tournamentType || ''}${item?.formatType || ''}`
  if (/混双/u.test(source)) return 'XD'
  if (/女双/u.test(source)) return 'WD'
  if (/男双|双打/u.test(source)) return 'MD'
  if (/女单/u.test(source)) return 'WS'
  if (/男单|单打/u.test(source)) return 'MS'
  return 'MS'
}

export function getTournamentEventLabel(item) {
  return EVENT_LABELS[normalizeTournamentEventType(item)] || '男单'
}

export function isTournamentDoubles(itemOrEventType) {
  const eventType = typeof itemOrEventType === 'string'
    ? String(itemOrEventType).trim().toUpperCase()
    : normalizeTournamentEventType(itemOrEventType)
  return eventType === 'MD' || eventType === 'WD' || eventType === 'XD'
}

export function normalizeTournamentFormatType(item) {
  const formatType = String(item?.formatType || '').trim()
  if (FORMAT_OPTIONS.includes(formatType)) return formatType

  const legacy = String(item?.tournamentType || '').trim()
  if (FORMAT_OPTIONS.includes(legacy)) return legacy

  return FORMAT_OPTIONS[0]
}
