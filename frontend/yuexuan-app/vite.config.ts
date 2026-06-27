import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': '/src' }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/recommend': {
        target: 'https://65a8-113-66-97-102.ngrok-free.app',
        changeOrigin: true
      },
      '/qa': {
        target: 'http://localhost:8131',
        changeOrigin: true
      },
      '/yolo': {
        target: 'https://65a8-113-66-97-102.ngrok-free.app',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/yolo/, '')
      }
    }
  }
})
