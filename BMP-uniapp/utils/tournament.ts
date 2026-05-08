import type { TournamentItem } from '@/api/internal/tournament'

export type TournamentEventType = 'MS' | 'WS' | 'MD' | 'WD' | 'XD'

const EVENT_LABEL_MAP: Record<TournamentEventType, string> = {
  MS: '男单',
  WS: '女单',
  MD: '男双',
  WD: '女双',
  XD: '混双'
}

const FORMAT_OPTIONS = ['单败淘汰制', '循环赛制', '双败淘汰制', '瑞士轮制'] as const

export function normalizeTournamentEventType(item?: Partial<TournamentItem> | null): TournamentEventType {
  const raw = String(item?.eventType || '').trim().toUpperCase()
  if (raw === 'MS' || raw === 'WS' || raw === 'MD' || raw === 'WD' || raw === 'XD') {
    return raw
  }
  const legacy = String(item?.tournamentType || '').trim().toUpperCase()
  if (legacy === 'SINGLE') return 'MS'
  if (legacy === 'DOUBLE') return 'MD'
  if (legacy === 'MIXED') return 'XD'

  const source = `${item?.tournamentName || ''}${item?.description || ''}${item?.tournamentType || ''}`
  if (/混双/u.test(source)) return 'XD'
  if (/女双/u.test(source)) return 'WD'
  if (/男双|双打/u.test(source)) return 'MD'
  if (/女单/u.test(source)) return 'WS'
  return 'MS'
}

export function getTournamentEventLabel(item?: Partial<TournamentItem> | null): string {
  return EVENT_LABEL_MAP[normalizeTournamentEventType(item)]
}

export function isTournamentDoubles(item?: Partial<TournamentItem> | null): boolean {
  const eventType = normalizeTournamentEventType(item)
  return eventType === 'MD' || eventType === 'WD' || eventType === 'XD'
}

export function normalizeTournamentFormatType(item?: Partial<TournamentItem> | null): string {
  const value = String(item?.formatType || '').trim()
  if (FORMAT_OPTIONS.includes(value as (typeof FORMAT_OPTIONS)[number])) {
    return value
  }
  const legacy = String(item?.tournamentType || '').trim()
  if (FORMAT_OPTIONS.includes(legacy as (typeof FORMAT_OPTIONS)[number])) {
    return legacy
  }
  return '单败淘汰制'
}
