import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// app-back (tomcat-app exposé sur 9082 par docker-compose), surchargeable via API_TARGET
const apiTarget = process.env.API_TARGET || 'http://localhost:9082'
// contexte du war app-back : /api/* -> /app-back/api/*
const apiContext = '/app-back'

export default defineConfig(({ command, isPreview }) => {
  // reverse proxy uniquement pour `vite` (dev) ; en prod c'est nginx qui route /api
  const isDev = command === 'serve' && !isPreview

  return {
    base: '/front/',
    plugins: [vue()],
    build: {
      outDir: '../artifact/front',
      emptyOutDir: true
    },
    server: isDev
      ? {
          proxy: {
            '/api': {
              target: apiTarget,
              changeOrigin: true,
              rewrite: path => apiContext + path
            }
          }
        }
      : {},
    // `vite preview` hérite sinon de server.proxy : on le désactive explicitement
    preview: { proxy: {} }
  }
})
