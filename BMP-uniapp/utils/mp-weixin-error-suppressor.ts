/**
 * 微信小程序错误抑制器
 * 在最底层拦截并过滤框架内部的 timeout 错误
 * 必须在所有代码之前执行
 */

// #ifdef MP-WEIXIN
;(() => {
  try {
    const wxAny = (globalThis as any).wx
    
    if (!wxAny) {
      console.warn('[Error Suppressor] wx 对象未找到')
      return
    }

    // 1. 最激进的方案：完全重写 console.error，不给任何 timeout 错误显示的机会
    const originalConsoleError = console.error
    const originalConsoleWarn = console.warn
    const originalConsoleLog = console.log

    // 创建一个标记，防止递归
    let isFiltering = false

    console.error = function(...args: any[]) {
      if (isFiltering) {
        return originalConsoleError.apply(console, args)
      }

      isFiltering = true
      
      try {
        // 将所有参数转换为字符串
        const message = args.map(arg => {
          if (typeof arg === 'string') return arg
          if (arg instanceof Error) return arg.message + '\n' + arg.stack
          try {
            return JSON.stringify(arg)
          } catch {
            return String(arg)
          }
        }).join(' ')

        // 超级严格的过滤规则
        const ignoredPatterns = [
          'Error: timeout',
          'timeout',
          'WAServiceMainContext',
          'at Function.<anonymous>',
          'at p (WAServiceMainContext',
          'at WAServiceMainContext.js',
          'closeSocket:fail',
          'websocket 错误',
          'SystemError',
          'routeDone with a webviewId',
          'webapi_getwxaasyncsecinfo'
        ]

        // 如果消息包含任何被忽略的模式，完全静默
        if (ignoredPatterns.some(pattern => message.includes(pattern))) {
          // 完全不输出
          return
        }

        // 否则正常输出
        originalConsoleError.apply(console, args)
      } finally {
        isFiltering = false
      }
    }

    console.warn = function(...args: any[]) {
      if (isFiltering) {
        return originalConsoleWarn.apply(console, args)
      }

      isFiltering = true
      
      try {
        const message = args.map(arg => 
          typeof arg === 'string' ? arg : JSON.stringify(arg)
        ).join(' ')

        const ignoredPatterns = [
          '开发模式下日志通道建立 socket 连接失败',
          '小程序平台，请勾选不校验合法域名配置',
          'timeout',
          'WAServiceMainContext'
        ]

        if (ignoredPatterns.some(pattern => message.includes(pattern))) {
          return
        }

        originalConsoleWarn.apply(console, args)
      } finally {
        isFiltering = false
      }
    }

    // 2. 拦截 wx.onError - 这是微信小程序的全局错误处理
    if (typeof wxAny.onError === 'function') {
      const originalOnError = wxAny.onError
      
      // 保存所有注册的处理器
      const errorHandlers: Array<(error: any) => void> = []
      
      // 重写 onError 方法
      wxAny.onError = function(handler: (error: any) => void) {
        // 包装用户的错误处理器
        const wrappedHandler = function(error: any) {
          const errorStr = typeof error === 'string' ? error : JSON.stringify(error)
          
          // 过滤框架内部错误
          const ignoredPatterns = [
            'Error: timeout',
            'timeout',
            'WAServiceMainContext',
            'at Function.<anonymous>',
            'at p (WAServiceMainContext',
            'at WAServiceMainContext.js'
          ]
          
          // 如果是需要过滤的错误，直接返回，不调用用户的处理器
          if (ignoredPatterns.some(pattern => errorStr.includes(pattern))) {
            // 完全静默
            return
          }
          
          // 否则调用用户的处理器
          if (typeof handler === 'function') {
            handler(error)
          }
        }
        
        errorHandlers.push(wrappedHandler)
        
        // 调用原始的 onError
        if (typeof originalOnError === 'function') {
          return originalOnError.call(wxAny, wrappedHandler)
        }
      }
      
      console.log('[Error Suppressor] wx.onError 已拦截')
    }

    // 3. 拦截 wx.onUnhandledRejection
    if (typeof wxAny.onUnhandledRejection === 'function') {
      const originalOnUnhandledRejection = wxAny.onUnhandledRejection
      
      wxAny.onUnhandledRejection = function(handler: (event: any) => void) {
        const wrappedHandler = function(event: any) {
          const reason = event?.reason || event
          const reasonStr = typeof reason === 'string' ? reason : JSON.stringify(reason)
          
          const ignoredPatterns = [
            'Error: timeout',
            'timeout',
            'WAServiceMainContext',
            'at Function.<anonymous>',
            'at p (WAServiceMainContext'
          ]
          
          if (ignoredPatterns.some(pattern => reasonStr.includes(pattern))) {
            return
          }
          
          if (typeof handler === 'function') {
            handler(event)
          }
        }
        
        if (typeof originalOnUnhandledRejection === 'function') {
          return originalOnUnhandledRejection.call(wxAny, wrappedHandler)
        }
      }
      
      console.log('[Error Suppressor] wx.onUnhandledRejection 已拦截')
    }

    // 4. 尝试拦截 Error 对象的 toString 方法（最激进）
    const OriginalError = Error
    ;(globalThis as any).Error = function(this: any, message?: string) {
      // 如果错误消息包含 timeout，修改消息
      if (message && (message.includes('timeout') || message.includes('WAServiceMainContext'))) {
        // 创建一个空错误，不显示任何内容
        const err = new OriginalError('')
        err.message = ''
        err.stack = ''
        return err
      }
      
      // 否则正常创建错误
      return new OriginalError(message)
    }
    
    // 保持原型链
    ;(globalThis as any).Error.prototype = OriginalError.prototype

    console.log('[Error Suppressor] 微信小程序错误抑制器已安装')
  } catch (error) {
    console.warn('[Error Suppressor] 安装失败:', error)
  }
})()
// #endif

export {}
