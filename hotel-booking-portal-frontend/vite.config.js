import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Any request starting with /api is forwarded to the Spring Boot backend,
// so the browser sees one origin and no CORS setup is needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});