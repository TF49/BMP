# BMP UniApp 项目更新日志

## 2026-03 uView 2.x（Vue3 兼容）接入

### 已完成

1. **新增 UI 依赖** (`package.json`)
   - ✅ 安装 `uview-plus@^3.7.13`

2. **入口接入** (`main.ts`)
   - ✅ 引入 `uview-plus` 与 `uview-plus/index.scss`
   - ✅ 在 `createApp()` 中注册 `app.use(uviewPlus)`

3. **easycom 配置** (`pages.json`, `src/pages.json`)
   - ✅ 新增 `^u-(.*)` 自动按需映射

4. **主题变量入口** (`uni.scss`)
   - ✅ 新增全局主题变量文件
   - ✅ 引入 `uview-plus/theme.scss`

5. **冒烟验证**
   - ✅ 首页新增 `u-input` + `u-button` 验证组件

### 已知问题

1. **微信小程序构建报错（版本矩阵冲突）**
   - 当前 `npm run build:mp-weixin` 报错：`isInSSRComponentSetup is not exported by vue.runtime.esm-bundler.js`
   - 原因：`@dcloudio/uni-app` 与 `vue/@vue/*` 版本未完全对齐
   - 处理建议：统一依赖矩阵后重新安装并构建验证

## 参考CMS小程序的最佳实践

### 已完成的改进

1. **请求封装优化** (`utils/request.ts`)
   - ✅ 添加对登录/注册接口的特殊处理，避免自动跳转
   - ✅ 完善的错误处理机制（502、503、504、500等）
   - ✅ 避免重复跳转登录页
   - ✅ 提供便捷的 get/post/put/del 方法

2. **新增缓存工具** (`utils/cache.ts`)
   - ✅ 支持过期时间的缓存机制
   - ✅ 自动清理过期缓存
   - ✅ 提供完整的缓存操作方法

3. **新增加载提示工具** (`utils/loading.ts`)
   - ✅ 统一的加载提示方法
   - ✅ 成功/错误/普通消息提示

4. **新增防抖节流工具** (`utils/debounce.ts`)
   - ✅ 防抖函数（debounce）
   - ✅ 节流函数（throttle）
   - ✅ TypeScript类型支持

5. **验证工具增强** (`utils/validate.ts`)
   - ✅ 参考CMS的验证规则
   - ✅ 添加更多验证方法（邮箱、整数、日期、必填等）
   - ✅ 提供别名方法

6. **主入口优化** (`main.ts`)
   - ✅ 禁用uni-app的WebSocket日志功能
   - ✅ 避免占用微信小程序的WebSocket连接限制

7. **应用入口优化** (`App.vue`)
   - ✅ 添加全局样式
   - ✅ 添加响应式容器样式
   - ✅ 预留WebSocket初始化位置

8. **构建脚本优化** (`package.json`)
   - ✅ 参考CMS的构建命令格式

### 使用示例

#### 使用缓存
```typescript
import { setCache, getCache, removeCache } from '@/utils/cache'

// 设置缓存（5分钟过期）
setCache('venue_list', venueList)

// 获取缓存
const cached = getCache('venue_list')

// 删除缓存
removeCache('venue_list')
```

#### 使用加载提示
```typescript
import { showLoading, hideLoading, showSuccess, showError } from '@/utils/loading'

showLoading('加载中...')
// ... 执行操作
hideLoading()
showSuccess('操作成功')
```

#### 使用防抖
```typescript
import { debounce } from '@/utils/debounce'

const handleSearch = debounce((keyword: string) => {
  // 搜索逻辑
}, 300)
```

#### 使用新的请求方法
```typescript
import { get, post, put, del } from '@/utils/request'

// GET请求
const data = await get('/venue/list', { page: 1 })

// POST请求
const result = await post('/booking/add', bookingData)
```

### 注意事项

1. **WebSocket连接限制**：微信小程序最多支持2个WebSocket连接，已在main.ts中禁用uni-app的日志连接
2. **认证接口处理**：登录/注册接口不会自动跳转，由页面自行处理错误
3. **缓存过期**：默认缓存5分钟过期，可根据需要调整
4. **错误处理**：所有API错误都会显示友好的提示信息
