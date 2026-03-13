import { createRouter, createWebHashHistory } from 'vue-router'
import AdminLayout from '@/layout/AdminLayout.vue'
import UserLayout from '@/layout/UserLayout.vue'
import CoachLayout from '@/layout/CoachLayout.vue'

// 登录页面
const Login = () => import('@/views/Login.vue')
// 注册页面
const Register = () => import('@/views/register.vue')
// 官网布局与页面
const SiteLayout = () => import('@/layout/SiteLayout.vue')
const SiteHome = () => import('@/views/site/Home.vue')
// 仪表盘页面
const Dashboard = () => import('@/views/Dashboard.vue')
// 大屏看板
const BigScreen = () => import('@/views/BigScreen.vue')

const routes = [
  // 根路径强制进官网（路由级重定向，优先于守卫）
  {
    path: '/',
    redirect: '/site'
  },
  // 官网（单页首页 + 锚点，功能/方案/关于/帮助均在首页内）
  {
    path: '/site',
    component: SiteLayout,
    redirect: '/site',
    meta: { hidden: true, public: true },
    children: [
      {
        path: '',
        name: 'SiteHome',
        component: SiteHome,
        meta: { title: '首页', public: true }
      },
      {
        path: 'features',
        redirect: { path: '/site', hash: '#features' }
      },
      {
        path: 'pricing',
        redirect: { path: '/site', hash: '#pricing' }
      },
      {
        path: 'about',
        redirect: { path: '/site', hash: '#about' }
      },
      {
        path: 'help',
        redirect: { path: '/site', hash: '#help' }
      }
    ]
  },
  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { hidden: true }
  },
  // 注册页面
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { hidden: true }
  },

  // 系统首页 - 管理端
  {
    path: '/',
    component: AdminLayout,
    redirect: (to) => {
      // 根据用户角色重定向
      try {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr && userInfoStr !== 'null') {
          const userInfo = JSON.parse(userInfoStr)
          // USER 与 MEMBER 同属用户端角色（MEMBER 为充值达标后升级）
          if (userInfo.role === 'USER' || userInfo.role === 'MEMBER') {
            return '/user/dashboard'
          }
          if (userInfo.role === 'COACH') {
            return '/coach/dashboard'
          }
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
      return '/dashboard'
    },
    meta: { title: '系统首页', icon: 'Odometer', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '系统首页', icon: 'Odometer', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      },
      {
        path: 'screen',
        name: 'BigScreen',
        component: BigScreen,
        meta: { title: '大屏看板', icon: 'Monitor', roles: ['PRESIDENT', 'VENUE_MANAGER'], hidden: true }
      }
    ]
  },
  
  // 教练端路由组（COACH 角色，独立布局）
  {
    path: '/coach',
    component: CoachLayout,
    redirect: '/coach/dashboard',
    meta: { roles: ['COACH'] },
    children: [
      {
        path: 'dashboard',
        name: 'CoachDashboard',
        component: () => import('@/views/coach/Dashboard.vue'),
        meta: { title: '工作台', icon: 'Odometer', roles: ['COACH'] }
      },
      {
        path: 'courses',
        name: 'CoachMyCourses',
        component: () => import('@/views/coach/MyCourses.vue'),
        meta: { title: '我的课程', icon: 'Document', roles: ['COACH'] }
      },
      {
        path: 'schedule',
        name: 'CoachSchedule',
        component: () => import('@/views/coach/Schedule.vue'),
        meta: { title: '我的课表', icon: 'Calendar', roles: ['COACH'] }
      },
      {
        path: 'bookings',
        name: 'CoachBookings',
        component: () => import('@/views/coach/Bookings.vue'),
        meta: { title: '预约明细', icon: 'List', roles: ['COACH'] }
      },
      {
        path: 'profile',
        name: 'CoachProfile',
        component: () => import('@/views/coach/Profile.vue'),
        meta: { title: '我的档案', icon: 'User', roles: ['COACH'] }
      }
    ]
  },

  // 用户端路由组（USER 与 MEMBER 同属用户端，MEMBER 为充值达标后由 USER 升级）
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/dashboard',
    meta: { roles: ['USER', 'MEMBER'] },
    children: [
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: () => import('@/views/user/Dashboard.vue'),
        meta: { title: '首页', icon: 'Odometer', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'booking',
        name: 'UserBooking',
        component: () => import('@/views/user/Booking.vue'),
        meta: { title: '场地预订', icon: 'DataLine', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'recharge',
        name: 'UserRecharge',
        component: () => import('@/views/user/Recharge.vue'),
        meta: { title: '账户充值', icon: 'Money', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'equipment',
        name: 'UserEquipment',
        component: () => import('@/views/user/EquipmentRental.vue'),
        meta: { title: '器材租借', icon: 'ShoppingBag', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'stringing',
        name: 'UserStringing',
        component: () => import('@/views/user/StringingService.vue'),
        meta: { title: '穿线服务', icon: 'Tools', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'course',
        name: 'UserCourse',
        component: () => import('@/views/user/CourseBooking.vue'),
        meta: { title: '课程预约', icon: 'Tickets', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'tournament',
        name: 'UserTournament',
        component: () => import('@/views/user/TournamentRegistration.vue'),
        meta: { title: '赛事报名', icon: 'Trophy', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'search',
        name: 'UserSearch',
        component: () => import('@/views/user/Search.vue'),
        meta: { title: '搜索', icon: 'Search', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'settings',
        name: 'UserSettings',
        component: () => import('@/views/user/Settings.vue'),
        meta: { title: '设置', icon: 'Setting', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'about',
        name: 'UserAbout',
        component: () => import('@/views/About.vue'),
        meta: { title: '关于我们', roles: ['USER', 'MEMBER'] }
      },
      {
        path: 'help',
        name: 'UserHelp',
        component: () => import('@/views/Help.vue'),
        meta: { title: '帮助与反馈', roles: ['USER', 'MEMBER'] }
      }
    ]
  },

  // 场馆管理（会长看全部，场馆管理者仅看自己归属的场馆）
  {
    path: '/venue',
    component: AdminLayout,
    meta: { title: '场馆管理', icon: 'OfficeBuilding', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'VenueManagement',
        component: () => import('@/views/VenueManagement.vue'),
        meta: { title: '场馆管理', icon: 'OfficeBuilding', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },

  // 场地管理
  {
    path: '/court',
    component: AdminLayout,
    meta: { title: '场地管理', icon: 'Grid', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'CourtManagement',
        component: () => import('@/views/CourtManagement.vue'),
        meta: { title: '场地管理', icon: 'Grid', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 会员管理
  {
    path: '/member',
    component: AdminLayout,
    meta: { title: '会员管理', icon: 'User', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'MemberManagement',
        component: () => import('@/views/MemberManagement.vue'),
        meta: { title: '会员管理', icon: 'User', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 充值中心 - 管理端
  {
    path: '/recharge',
    component: AdminLayout,
    meta: { title: '充值中心', icon: 'Money', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: 'index',
        name: 'Recharge',
        component: () => import('@/views/Recharge.vue'),
        meta: { title: '充值中心', icon: 'Money', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },

  // 预约管理 - 管理端
  {
    path: '/booking',
    component: AdminLayout,
    meta: { title: '预约管理', icon: 'DataLine', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'BookingManagement',
        component: () => import('@/views/BookingManagement.vue'),
        meta: { title: '场地预约', icon: 'DataLine', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 器材管理
  {
    path: '/equipment',
    component: AdminLayout,
    meta: { title: '器材管理', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'EquipmentManagement',
        component: () => import('@/views/EquipmentManagement.vue'),
        meta: { title: '器材管理', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'rental',
        name: 'EquipmentRentalManagement',
        component: () => import('@/views/EquipmentRentalManagement.vue'),
        meta: { title: '器材租借', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'stringing',
        name: 'StringingServiceManagement',
        component: () => import('@/views/StringingServiceManagement.vue'),
        meta: { title: '穿线服务', icon: 'Tools', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 教练与课程
  {
    path: '/coach-course',
    component: AdminLayout,
    meta: { title: '教练与课程', icon: 'User', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'coach',
        name: 'CoachManagement',
        component: () => import('@/views/CoachManagement.vue'),
        meta: { title: '教练管理', icon: 'User', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'course',
        name: 'CourseManagement',
        component: () => import('@/views/CourseManagement.vue'),
        meta: { title: '课程管理', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'course-booking',
        name: 'CourseBookingManagement',
        component: () => import('@/views/CourseBookingManagement.vue'),
        meta: { title: '课程预约', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 赛事管理
  {
    path: '/tournament',
    component: AdminLayout,
    meta: { title: '赛事管理', icon: 'DataLine', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'TournamentManagement',
        component: () => import('@/views/TournamentManagement.vue'),
        meta: { title: '赛事管理', icon: 'DataLine', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'registration',
        name: 'TournamentRegistrationManagement',
        component: () => import('@/views/TournamentRegistrationManagement.vue'),
        meta: { title: '赛事报名', icon: 'DataLine', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 财务管理
  {
    path: '/finance',
    component: AdminLayout,
    meta: { title: '财务管理', icon: 'Coin', roles: ['PRESIDENT','VENUE_MANAGER'] },
    children: [
      {
        path: 'management',
        name: 'FinanceManagement',
        component: () => import('@/views/FinanceManagement.vue'),
        meta: { title: '财务管理', icon: 'Coin', roles: ['PRESIDENT','VENUE_MANAGER'] }
      },
      {
        path: 'audit-log',
        name: 'FinanceAuditLog',
        component: () => import('@/views/FinanceAuditLog.vue'),
        meta: { title: '审计日志', icon: 'Document', roles: ['PRESIDENT','VENUE_MANAGER'] }
      }
    ]
  },

  // 用户管理
  {
    path: '/admin/user',
    component: AdminLayout,
    meta: { title: '用户管理', icon: 'User', roles: ['PRESIDENT'] },
    children: [
      {
        path: 'management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['PRESIDENT'] }
      }
    ]
  },

  // 管理端个人中心
  {
    path: '/profile',
    component: AdminLayout,
    meta: { title: '个人中心', icon: 'User', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: '',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },

  // 管理端系统设置
  {
    path: '/settings',
    component: AdminLayout,
    meta: { title: '系统设置', icon: 'Setting', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: '',
        name: 'AdminSettings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },

  // 管理端关于我们 / 帮助与反馈
  {
    path: '/about',
    component: AdminLayout,
    meta: { title: '关于我们', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: '',
        name: 'AdminAbout',
        component: () => import('@/views/About.vue'),
        meta: { title: '关于我们', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },
  {
    path: '/help',
    component: AdminLayout,
    meta: { title: '帮助与反馈', roles: ['PRESIDENT', 'VENUE_MANAGER'] },
    children: [
      {
        path: '',
        name: 'AdminHelp',
        component: () => import('@/views/Help.vue'),
        meta: { title: '帮助与反馈', roles: ['PRESIDENT', 'VENUE_MANAGER'] }
      }
    ]
  },

  // 错误页面
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/403.vue'),
    meta: { hidden: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: { hidden: true }
  },
  // 捕获所有未匹配的路由，重定向到404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（浏览器前进/后退），使用保存的位置
    if (savedPosition) {
      return savedPosition
    }
    // 如果有 hash，滚动到对应元素
    if (to.hash) {
      // 使用 nextTick 确保 DOM 已渲染
      return new Promise((resolve) => {
        setTimeout(() => {
          const el = document.querySelector(to.hash)
          if (el) {
            resolve({ el: to.hash, behavior: 'smooth' })
          } else {
            resolve({ top: 0 })
          }
        }, 100)
      })
    }
    // 默认滚动到顶部
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  try {
    // 强制：访问根路径时一律先进官网首页（不论是否已登录）
    const path = (to.path || '').replace(/\/$/, '') || '/'
    const isRoot = path === '/' || path === ''
    if (isRoot) {
      next({ path: '/site', replace: true })
      return
    }

    const token = localStorage.getItem('token')

    // 公共页面白名单：无需登录（官网/登录/注册/错误页）
    if (
      to.path.startsWith('/site') ||
      to.path === '/login' ||
      to.path === '/register' ||
      to.path === '/403' ||
      to.path === '/404'
    ) {
      next()
      return
    }

    // 如果没有token，跳转到登录页
    if (!token) {
      next('/login')
      return
    }

    // 获取用户角色，处理各种异常情况
    let userInfo = {}
    try {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr && userInfoStr !== 'null') {
        userInfo = JSON.parse(userInfoStr)
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      userInfo = {}
    }
    // 后端可能返回 ADMIN，与前端 PRESIDENT 等价，统一用于权限判断
    const rawRole = userInfo.role || ''
    const userRole = rawRole === 'ADMIN' ? 'PRESIDENT' : rawRole
    const roles = userRole ? [userRole] : []

    // 关闭可能存在的所有弹窗
    if (window.eventBus) {
      window.eventBus.$emit('closeAllModals')
    }

    // 角色路由重定向逻辑
    // 普通用户（USER/MEMBER 同属用户端）访问管理端路由，重定向到用户端
    const isUserSideRole = userRole === 'USER' || userRole === 'MEMBER'
    if (isUserSideRole) {
      if (to.path.startsWith('/admin/') || 
          (to.path.startsWith('/venue') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/court') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/member') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/equipment') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/coach-course') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/tournament') && !to.path.startsWith('/user')) ||
          (to.path.startsWith('/finance') && !to.path.startsWith('/user')) ||
          (to.path === '/dashboard' && !to.path.startsWith('/user')) ||
          (to.path === '/booking/management') ||
          (to.path === '/recharge/index')) {
        // 重定向到用户端对应页面
        if (to.path === '/dashboard' || to.path === '/') {
          next('/user/dashboard')
        } else if (to.path === '/booking/management') {
          next('/user/booking')
        } else if (to.path === '/recharge/index') {
          next('/user/recharge')
        } else {
          next('/user/dashboard')
        }
        return
      }
    }
    
    // 管理角色（含 ADMIN，已统一为 PRESIDENT）访问用户端路由，重定向到管理端
    if ((userRole === 'PRESIDENT' || userRole === 'VENUE_MANAGER') && to.path.startsWith('/user/')) {
      if (to.path === '/user/dashboard') {
        next('/dashboard')
      } else if (to.path === '/user/booking') {
        next('/booking/management')
      } else {
        next('/dashboard')
      }
      return
    }

    // COACH 访问用户端或管理端路由，重定向到教练端
    if (userRole === 'COACH' && (to.path.startsWith('/admin') || to.path.startsWith('/user/') || to.path === '/dashboard' || to.path.startsWith('/venue') || to.path.startsWith('/court') || to.path.startsWith('/member') || to.path.startsWith('/coach-course') || to.path.startsWith('/tournament') || to.path.startsWith('/finance') || to.path.startsWith('/equipment') || to.path === '/booking/management' || to.path === '/recharge/index')) {
      next('/coach/dashboard')
      return
    }

    // 非 COACH 访问教练端路由，重定向到登录或对应首页
    if (to.path.startsWith('/coach/') && userRole !== 'COACH') {
      next('/login')
      return
    }

    // 检查路由是否需要权限
    if (to.meta && to.meta.roles) {
      // 检查用户是否有访问权限
      const hasPermission = roles.some(role => to.meta.roles.includes(role))
      if (hasPermission) {
        next()
      } else {
        // 没有权限，跳转到403页面
        next('/403')
      }
    } else {
      // 不需要权限的路由直接放行
      next()
    }
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 如果发生错误，跳转到登录页
    next('/login')
  }
})

export default router
