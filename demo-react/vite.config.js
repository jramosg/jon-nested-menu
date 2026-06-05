import { defineConfig } from 'vite';
import path from 'node:path';

export default defineConfig({
  define: {
    global: 'globalThis'
  },
  resolve: {
    // Keep linked file: dependencies under node_modules path so
    // Vite's CommonJS transform applies to jon-nested-menu dist output.
    preserveSymlinks: true,
    dedupe: ['react', 'react-dom', '@mui/material', '@emotion/react', '@emotion/styled'],
    alias: {
      react: path.resolve(__dirname, 'node_modules/react'),
      'react-dom': path.resolve(__dirname, 'node_modules/react-dom'),
      '@mui/material': path.resolve(__dirname, 'node_modules/@mui/material'),
      '@emotion/react': path.resolve(__dirname, 'node_modules/@emotion/react'),
      '@emotion/styled': path.resolve(__dirname, 'node_modules/@emotion/styled')
    }
  }
});
