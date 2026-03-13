/**
 * 主题配置文件
 * 定义系统所有可用的主题配色方案
 */

export interface Theme {
  name: string
  displayName: string
  icon: string // 主题图标（emoji）
  primary: string
  primaryLight: string
  primaryDark: string
  secondary: string
  accent: string
  success: string
  warning: string
  danger: string
  info: string
  background: string
  cardBg: string
  cardBgHover: string
  textPrimary: string
  textSecondary: string
  textMuted: string
  border: string
  borderHover: string
  shadow: string
  shadowHover: string
  // 统计卡片专用渐变色
  gradients: {
    members: string
    courts: string
    bookings: string
    revenue: string
  }
}

export const themes: Record<string, Theme> = {
  'ocean-blue': {
    name: 'ocean-blue',
    displayName: '清爽蓝色',
    icon: '🌊',
    primary: '#2563EB',
    primaryLight: '#3B82F6',
    primaryDark: '#1E40AF',
    secondary: '#3B82F6',
    accent: '#F97316',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#F8FAFC',
    cardBg: '#FFFFFF',
    cardBgHover: '#FAFBFC',
    textPrimary: '#1E293B',
    textSecondary: '#64748B',
    textMuted: '#94A3B8',
    border: '#E2E8F0',
    borderHover: '#CBD5E1',
    shadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
    shadowHover: '0 4px 12px rgba(0, 0, 0, 0.08)',
    gradients: {
      members: 'linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%)',
      courts: 'linear-gradient(135deg, #FCE7F3 0%, #FDF2F8 100%)',
      bookings: 'linear-gradient(135deg, #CFFAFE 0%, #ECFDF5 100%)',
      revenue: 'linear-gradient(135deg, #DCFCE7 0%, #F0FDF4 100%)'
    }
  },

  'energetic-orange': {
    name: 'energetic-orange',
    displayName: '活力橙蓝',
    icon: '🔥',
    primary: '#F97316',
    primaryLight: '#FB923C',
    primaryDark: '#EA580C',
    secondary: '#FB923C',
    accent: '#2563EB',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#FFF7ED',
    cardBg: '#FFFFFF',
    cardBgHover: '#FFFBF5',
    textPrimary: '#1E293B',
    textSecondary: '#64748B',
    textMuted: '#94A3B8',
    border: '#FFEDD5',
    borderHover: '#FED7AA',
    shadow: '0 1px 3px rgba(249, 115, 22, 0.08)',
    shadowHover: '0 4px 12px rgba(249, 115, 22, 0.15)',
    gradients: {
      members: 'linear-gradient(135deg, #FFEDD5 0%, #FFF7ED 100%)',
      courts: 'linear-gradient(135deg, #FED7AA 0%, #FFEDD5 100%)',
      bookings: 'linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%)',
      revenue: 'linear-gradient(135deg, #DCFCE7 0%, #F0FDF4 100%)'
    }
  },

  'sport-green': {
    name: 'sport-green',
    displayName: '深绿运动',
    icon: '🏸',
    primary: '#047857',
    primaryLight: '#059669',
    primaryDark: '#065F46',
    secondary: '#059669',
    accent: '#F59E0B',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#F0FDF4',
    cardBg: '#FFFFFF',
    cardBgHover: '#F7FEF9',
    textPrimary: '#1E293B',
    textSecondary: '#64748B',
    textMuted: '#94A3B8',
    border: '#D1FAE5',
    borderHover: '#A7F3D0',
    shadow: '0 1px 3px rgba(4, 120, 87, 0.08)',
    shadowHover: '0 4px 12px rgba(4, 120, 87, 0.15)',
    gradients: {
      members: 'linear-gradient(135deg, #D1FAE5 0%, #ECFDF5 100%)',
      courts: 'linear-gradient(135deg, #A7F3D0 0%, #D1FAE5 100%)',
      bookings: 'linear-gradient(135deg, #FEF3C7 0%, #FEF9C3 100%)',
      revenue: 'linear-gradient(135deg, #DCFCE7 0%, #F0FDF4 100%)'
    }
  },

  'elegant-purple': {
    name: 'elegant-purple',
    displayName: '紫色优雅',
    icon: '💜',
    primary: '#7C3AED',
    primaryLight: '#8B5CF6',
    primaryDark: '#6D28D9',
    secondary: '#8B5CF6',
    accent: '#EC4899',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#FAF5FF',
    cardBg: '#FFFFFF',
    cardBgHover: '#FCF9FF',
    textPrimary: '#1E293B',
    textSecondary: '#64748B',
    textMuted: '#94A3B8',
    border: '#E9D5FF',
    borderHover: '#D8B4FE',
    shadow: '0 1px 3px rgba(124, 58, 237, 0.08)',
    shadowHover: '0 4px 12px rgba(124, 58, 237, 0.15)',
    gradients: {
      members: 'linear-gradient(135deg, #E9D5FF 0%, #F3E8FF 100%)',
      courts: 'linear-gradient(135deg, #FCE7F3 0%, #FDF2F8 100%)',
      bookings: 'linear-gradient(135deg, #D8B4FE 0%, #E9D5FF 100%)',
      revenue: 'linear-gradient(135deg, #DCFCE7 0%, #F0FDF4 100%)'
    }
  },

  'dark-mode': {
    name: 'dark-mode',
    displayName: '暗夜模式',
    icon: '🌙',
    primary: '#3B82F6',
    primaryLight: '#60A5FA',
    primaryDark: '#2563EB',
    secondary: '#60A5FA',
    accent: '#F97316',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#0F172A',
    cardBg: '#1E293B',
    cardBgHover: '#334155',
    textPrimary: '#F1F5F9',
    textSecondary: '#94A3B8',
    textMuted: '#64748B',
    border: '#334155',
    borderHover: '#475569',
    shadow: '0 1px 3px rgba(0, 0, 0, 0.3)',
    shadowHover: '0 4px 12px rgba(0, 0, 0, 0.5)',
    gradients: {
      members: 'linear-gradient(135deg, #1E3A8A 0%, #1E40AF 100%)',
      courts: 'linear-gradient(135deg, #831843 0%, #9F1239 100%)',
      bookings: 'linear-gradient(135deg, #155E75 0%, #0E7490 100%)',
      revenue: 'linear-gradient(135deg, #065F46 0%, #047857 100%)'
    }
  },

  'sunset-pink': {
    name: 'sunset-pink',
    displayName: '日落粉红',
    icon: '🌸',
    primary: '#EC4899',
    primaryLight: '#F472B6',
    primaryDark: '#DB2777',
    secondary: '#F472B6',
    accent: '#8B5CF6',
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#06B6D4',
    background: '#FFF1F2',
    cardBg: '#FFFFFF',
    cardBgHover: '#FFF7F8',
    textPrimary: '#1E293B',
    textSecondary: '#64748B',
    textMuted: '#94A3B8',
    border: '#FECDD3',
    borderHover: '#FBBF24',
    shadow: '0 1px 3px rgba(236, 72, 153, 0.08)',
    shadowHover: '0 4px 12px rgba(236, 72, 153, 0.15)',
    gradients: {
      members: 'linear-gradient(135deg, #FECDD3 0%, #FFF1F2 100%)',
      courts: 'linear-gradient(135deg, #FBE7F3 0%, #FCE7F3 100%)',
      bookings: 'linear-gradient(135deg, #E9D5FF 0%, #F3E8FF 100%)',
      revenue: 'linear-gradient(135deg, #DCFCE7 0%, #F0FDF4 100%)'
    }
  }
}

export const defaultTheme = 'ocean-blue'

// 获取所有主题列表
export const getThemeList = (): Theme[] => {
  return Object.values(themes)
}

// 根据名称获取主题
export const getTheme = (name: string): Theme => {
  return themes[name] || themes[defaultTheme]
}
