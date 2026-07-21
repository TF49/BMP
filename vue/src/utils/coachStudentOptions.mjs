export function extractPagedRows(response) {
  return Array.isArray(response?.data?.data) ? response.data.data : []
}

export function buildCoachOptionParams(keyword = '', venueId = null) {
  const params = { page: 1, size: 100 }
  const value = String(keyword || '').trim()
  if (value) params.keyword = value
  if (venueId != null) params.venueId = venueId
  return params
}

export function buildMemberOptionParams(keyword = '') {
  const params = { page: 1, size: 100 }
  const value = String(keyword || '').trim()
  if (value) {
    if (/^\d+$/.test(value)) params.phone = value
    else params.memberName = value
  }
  return params
}
