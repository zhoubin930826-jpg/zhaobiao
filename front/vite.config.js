import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  base: '/ztbfb/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    open: true,
    proxy: {
      '/api': {
        target: process.env.VITE_PROXY_TARGET || 'https://xiazhiyong.vip',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
