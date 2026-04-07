import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 将所有以 /api 开头的请求转发到后端
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        // 注意：不要再写 rewrite，因为后端的 @RequestMapping 包含了 /api
      },
      // 将上传文件的访问请求转发到后端
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      }
    }
  }
})