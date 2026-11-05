import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // GitHub Pages serves project sites below /<repository>/, while local Vite
  // development serves from /. Keep asset URLs correct in both environments.
  const { VITE_BASE_PATH = '/' } = loadEnv(mode, '.', 'VITE_');

  return {
    base: VITE_BASE_PATH,
    plugins: [react()],
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/testSetup.ts',
      coverage: { reporter: ['text', 'html'] },
    },
  } as never;
});
