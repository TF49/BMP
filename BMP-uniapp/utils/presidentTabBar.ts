/**
 * 协会会长端 tabBar 配置
 * 与 pages.json 中用户端 tabBar 分离，运行时根据角色切换
 */

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
    iconPath: '⊞',
    selectedIconPath: '⊞',
    extraIconType: 'grid'
  },
  {
    pagePath: 'pages/president/venue/list',
    text: '场馆',
    iconPath: '🏋️',
    selectedIconPath: '🏋️',
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
    pagePath: 'pages/president/profile/index',
    text: '个人中心',
    iconPath: '👤',
    selectedIconPath: '👤',
    extraIconType: 'person'
  }
]

export const PRESIDENT_TAB_BAR_STYLE = {
  color: '#475569',
  selectedColor: '#3cc51f',
  backgroundColor: '#ffffff',
  borderStyle: 'black' as const
}
