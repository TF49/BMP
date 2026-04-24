import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const UNI_APP_ESM_PATH = '/@dcloudio/uni-app/dist/uni-app.es.js'

function uniAppVue35CompatPlugin() {
  return {
    name: 'bmp-uni-app-vue35-compat',
    enforce: 'pre' as const,
    transform(code: string, id: string) {
      const normalizedId = id.replace(/\\/g, '/')
      if (!normalizedId.includes(UNI_APP_ESM_PATH)) {
        return null
      }

      let transformed = code
      transformed = transformed.replace(
        "import { shallowRef, ref, getCurrentInstance, isInSSRComponentSetup, injectHook } from 'vue';",
        "import { shallowRef, ref, getCurrentInstance } from 'vue';\nconst isInSSRComponentSetup = false;\nconst injectHook = (lifecycle, hook, target = getCurrentInstance()) => {\n  if (!target) {\n    return;\n  }\n  const hooks = target[lifecycle] || (target[lifecycle] = []);\n  hooks.push(hook);\n  return hook;\n};"
      )

      if (transformed === code) {
        return null
      }

      return {
        code: transformed,
        map: null
      }
    }
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [uniAppVue35CompatPlugin(), uni()],
  define: {
    'process.env.UNI_SOCKET_HOSTS': JSON.stringify(''),
    'process.env.UNI_SOCKET_PORT': JSON.stringify(''),
    'process.env.UNI_SOCKET_ID': JSON.stringify(''),
    'process.env.UNI_CONSOLE_WEBVIEW_EVAL_JS_CODE': JSON.stringify(''),
    'process.env.UNI_DEBUG': JSON.stringify('')
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './'),
    },
  },
})
