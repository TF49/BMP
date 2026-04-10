export interface PresidentTabBarItem {
  pagePath: string
  text: string
  iconPath: string
  selectedIconPath: string
  extraIconType?: string
}

export const PRESIDENT_TAB_BAR_LIST: PresidentTabBarItem[] = [
  {
    pagePath: 'pages/index/index',
    text: '工作台',
    iconPath: '□',
    selectedIconPath: '■',
    extraIconType: 'grid'
  },
  {
    pagePath: 'pages/president/venue/list',
    text: '场馆',
    iconPath: '📍',
    selectedIconPath: '📍',
    extraIconType: 'location'
  },
  {
    pagePath: 'pages/president/user/list',
    text: '成员',
    iconPath: '👥',
    selectedIconPath: '👥',
    extraIconType: 'staff'
  },
  {
    pagePath: 'pages/president/finance/list',
    text: '财务',
    iconPath: '💼',
    selectedIconPath: '💼',
    extraIconType: 'wallet'
  },
  {
    pagePath: 'pages/president/profile/index',
    text: '我的',
    iconPath: '👤',
    selectedIconPath: '👤',
    extraIconType: 'person'
  }
]

export const PRESIDENT_TAB_BAR_STYLE = {
  color: '#6b7280',
  selectedColor: '#ea580c',
  backgroundColor: '#ffffff',
  borderStyle: 'black' as const
}

export function isPresidentTabActive(pagePath: string, currentPath: string): boolean {
  const path = pagePath.replace(/^\//, '')
  const cur = currentPath.replace(/^\//, '')

  if (!cur) return false
  if (cur === path) return true

  if (path.includes('index/index')) {
    return cur.includes('pages/index/index') || cur.includes('dashboard/index')
  }

  if (path.includes('venue/list')) {
    return cur.includes('president/venue') || cur.includes('president/court')
  }

  if (path.includes('user/list')) {
    return cur.includes('president/user') || cur.includes('president/member')
  }

  if (path.includes('finance/list')) {
    return cur.includes('president/finance')
  }

  if (path.includes('profile/index')) {
    return cur.includes('president/profile')
  }

  return false
}
