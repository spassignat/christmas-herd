import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/front/',
  plugins: [vue()],
  server: {
    proxy: {
      '/api': process.env.API_TARGET || 'http://localhost:8080'
    }
  }
})
