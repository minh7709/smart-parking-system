import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/camera-in-stream': {
        target: 'http://192.168.1.67:8080', // IP hiện tại của iPhone bạn
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/camera-in-stream/, '/stream.mjpg'),
      },
      '/camera-out-stream': {
        target: 'http://192.168.1.67:8080', // IP hiện tại của iPhone bạn
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/camera-out-stream/, '/stream.mjpg'),
      },
    },
  },
})
