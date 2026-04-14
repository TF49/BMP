/**
 * 微信小程序 WebSocket 补丁
 * 必须在所有其他代码之前执行，以拦截 uni-app 框架的 WebSocket 调用
 */

// #ifdef MP-WEIXIN
;(() => {
  try {
    const wxAny = (globalThis as any).wx
    const uniAny = (globalThis as any).uni

    // 1. 修复 closeSocket 的无效关闭码问题
    const patchCloseSocket = (target: any, name: string) => {
      if (target && typeof target.closeSocket === 'function') {
        const original = target.closeSocket
        target.closeSocket = function(options: any = {}) {
          // 如果没有提供 options，创建一个
          if (!options || typeof options !== 'object') {
            options = {}
          }
          
          // 修复无效的关闭码
          if (options.code === 1006 || 
              options.code === undefined || 
              options.code === null ||
              (options.code !== 1000 && (options.code < 3000 || options.code > 4999))) {
            options.code = 1000
          }
          
          return original.call(this, options)
        }
        console.log(`[WebSocket Patch] ${name}.closeSocket patched`)
      }
    }

    // 2. 限制并发 WebSocket 连接数
    let activeSocketCount = 0
    const MAX_SOCKETS = 2
    const socketTasks = new Set<any>()

    const patchConnectSocket = (target: any, name: string) => {
      if (target && typeof target.connectSocket === 'function') {
        const original = target.connectSocket
        target.connectSocket = function(options: any = {}) {
          // 检查是否超过最大连接数
          if (activeSocketCount >= MAX_SOCKETS) {
            console.warn(`[WebSocket Patch] 已达到最大连接数 ${MAX_SOCKETS}，拒绝新连接`)
            
            // 返回一个模拟的 SocketTask 对象
            const mockTask = {
              send: () => {},
              close: () => {},
              onOpen: () => mockTask,
              onClose: () => mockTask,
              onError: () => mockTask,
              onMessage: () => mockTask,
              CONNECTING: 0,
              OPEN: 1,
              CLOSING: 2,
              CLOSED: 3,
              readyState: 3
            }
            
            // 异步触发 error 回调
            setTimeout(() => {
              if (options.fail) {
                options.fail({ errMsg: 'connectSocket:fail exceed max socket count' })
              }
              if (options.complete) {
                options.complete({ errMsg: 'connectSocket:fail exceed max socket count' })
              }
            }, 0)
            
            return mockTask
          }

          activeSocketCount++
          console.log(`[WebSocket Patch] 创建新连接，当前连接数: ${activeSocketCount}`)

          const task = original.call(this, options)
          
          if (task) {
            socketTasks.add(task)
            
            // 包装 onClose 以跟踪连接关闭
            const originalOnClose = task.onClose
            if (typeof originalOnClose === 'function') {
              task.onClose = function(callback: any) {
                return originalOnClose.call(this, function(...args: any[]) {
                  activeSocketCount = Math.max(0, activeSocketCount - 1)
                  socketTasks.delete(task)
                  console.log(`[WebSocket Patch] 连接关闭，当前连接数: ${activeSocketCount}`)
                  if (callback) callback(...args)
                })
              }
            }

            // 包装 onError 以跟踪连接错误
            const originalOnError = task.onError
            if (typeof originalOnError === 'function') {
              task.onError = function(callback: any) {
                return originalOnError.call(this, function(...args: any[]) {
                  activeSocketCount = Math.max(0, activeSocketCount - 1)
                  socketTasks.delete(task)
                  console.log(`[WebSocket Patch] 连接错误，当前连接数: ${activeSocketCount}`)
                  if (callback) callback(...args)
                })
              }
            }

            // 包装 close 方法以确保使用正确的关闭码
            const originalClose = task.close
            if (typeof originalClose === 'function') {
              task.close = function(options: any = {}) {
                if (!options || typeof options !== 'object') {
                  options = {}
                }
                if (options.code === 1006 || 
                    options.code === undefined || 
                    options.code === null ||
                    (options.code !== 1000 && (options.code < 3000 || options.code > 4999))) {
                  options.code = 1000
                }
                return originalClose.call(this, options)
              }
            }
          }

          return task
        }
        console.log(`[WebSocket Patch] ${name}.connectSocket patched`)
      }
    }

    // 3. 过滤控制台错误和警告
    const originalError = console.error
    const originalWarn = console.warn

    console.error = function(...args: any[]) {
      const message = args.map(arg => 
        typeof arg === 'string' ? arg : JSON.stringify(arg)
      ).join(' ')

      const ignoredPatterns = [
        'closeSocket:fail Failed to execute',
        'The code must be either 1000',
        'websocket 错误同时最多发起',
        'WebSocket connection to',
        'SystemError (appServiceSDKScriptError)',
        'routeDone with a webviewId',
        'webapi_getwxaasyncsecinfo',
        'Error: timeout',
        'timeout',
        'WAServiceMainContext.js',
        'at Function.<anonymous>',
        'at p (WAServiceMainContext'
      ]

      if (!ignoredPatterns.some(pattern => message.includes(pattern))) {
        originalError.apply(console, args)
      }
    }

    console.warn = function(...args: any[]) {
      const message = args.map(arg => 
        typeof arg === 'string' ? arg : JSON.stringify(arg)
      ).join(' ')

      const ignoredPatterns = [
        '开发模式下日志通道建立 socket 连接失败',
        '小程序平台，请勾选不校验合法域名配置',
        '如果是运行到真机，请确认手机与电脑处于同一网络'
      ]

      if (!ignoredPatterns.some(pattern => message.includes(pattern))) {
        originalWarn.apply(console, args)
      }
    }

    // 4. 处理未捕获的 Promise 拒绝和错误
    if (wxAny && typeof wxAny.onUnhandledRejection === 'function') {
      wxAny.onUnhandledRejection((event: any) => {
        const reason = event?.reason || event
        const reasonStr = typeof reason === 'string' ? reason : JSON.stringify(reason)
        
        const ignoredPatterns = [
          'closeSocket:fail',
          'websocket 错误',
          'WebSocket connection',
          'timeout',
          'WAServiceMainContext'
        ]
        
        if (!ignoredPatterns.some(pattern => reasonStr.includes(pattern))) {
          console.error('[Unhandled Rejection]', reason)
        }
      })
    }

    // 5. 处理未捕获的错误
    if (wxAny && typeof wxAny.onError === 'function') {
      wxAny.onError((error: any) => {
        const errorStr = typeof error === 'string' ? error : JSON.stringify(error)
        
        const ignoredPatterns = [
          'timeout',
          'WAServiceMainContext',
          'closeSocket:fail',
          'websocket 错误'
        ]
        
        if (!ignoredPatterns.some(pattern => errorStr.includes(pattern))) {
          console.error('[Global Error]', error)
        }
      })
    }

    // 应用补丁
    if (wxAny) {
      patchCloseSocket(wxAny, 'wx')
      patchConnectSocket(wxAny, 'wx')
    }

    // uni 对象可能还未初始化，延迟补丁
    const patchUni = () => {
      const uni = (globalThis as any).uni
      if (uni) {
        patchCloseSocket(uni, 'uni')
        patchConnectSocket(uni, 'uni')
      }
    }

    // 立即尝试补丁 uni
    patchUni()

    // 如果 uni 还未初始化，监听其初始化
    if (!uniAny) {
      Object.defineProperty(globalThis, 'uni', {
        configurable: true,
        enumerable: true,
        get() {
          return (globalThis as any)._uni
        },
        set(value) {
          (globalThis as any)._uni = value
          if (value) {
            patchCloseSocket(value, 'uni')
            patchConnectSocket(value, 'uni')
          }
        }
      })
    }

    console.log('[WebSocket Patch] 微信小程序 WebSocket 补丁已安装')
  } catch (error) {
    console.warn('[WebSocket Patch] 补丁安装失败:', error)
  }
})()
// #endif

export {}
