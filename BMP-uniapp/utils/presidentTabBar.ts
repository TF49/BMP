/**
 * 协会会长端 tabBar 配置
 * 与 pages.json 中用户端 tabBar 分离，运行时根据角色切换
 */

export interface PresidentTabBarItem {
  pagePath: string
  text: string
  iconPath: string
  selectedIconPath: string
}

export const PRESIDENT_TAB_BAR_LIST: PresidentTabBarItem[] = [
  {
    pagePath: 'pages/president/dashboard/index',
    text: '看板',
    iconPath: '/static/tabbar/tab-home.png',
    selectedIconPath: '/static/tabbar/tab-home-active.png'
  },
  {
    pagePath: 'pages/president/user/list',
    text: '用户',
    iconPath: '/static/tabbar/tab-venue.png',
    selectedIconPath: '/static/tabbar/tab-venue-active.png'
  },
  {
    pagePath: 'pages/president/venue/list',
    text: '场馆',
    iconPath: '/static/tabbar/tab-course.png',
    selectedIconPath: '/static/tabbar/tab-course-active.png'
  },
  {
    pagePath: 'pages/president/finance/list',
    text: '财务',
    iconPath: '/static/tabbar/tab-tournament.png',
    selectedIconPath: '/static/tabbar/tab-tournament-active.png'
  },
  {
    pagePath: 'pages/president/profile/index',
    text: '我的',
    iconPath: '/static/tabbar/tab-profile.png',
    selectedIconPath: '/static/tabbar/tab-profile-active.png'
  }
]

export const PRESIDENT_TAB_BAR_STYLE = {
  color: '#475569',
  selectedColor: '#3cc51f',
  backgroundColor: '#ffffff',
  borderStyle: 'black' as const
}
