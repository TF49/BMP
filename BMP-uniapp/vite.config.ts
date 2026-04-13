import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// https://vite.dev/config/
export default defineConfig({
  plugins: [uni()],
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
