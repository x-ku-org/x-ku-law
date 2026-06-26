import { createRequire } from 'node:module';
import { readFile } from 'node:fs/promises';
import { fileURLToPath, URL } from 'node:url';
import vue from '@vitejs/plugin-vue';
import { defineConfig, type Plugin } from 'vite';

const require = createRequire(import.meta.url);

/**
 * jsPDF can only embed raw sfnt/TTF fonts, but `@fontsource/*` packages ship
 * compressed `.woff2` web fonts. Earlier we decompressed woff2 -> ttf in the
 * browser at runtime via the `wawoff2` WebAssembly module. That WASM module
 * silently never finishes initializing once bundled for the browser, so
 * `decompress()` returned a promise that neither resolved nor rejected and the
 * "下载 PDF" button spun forever (the failure could not even be caught).
 *
 * Conversion is reliable in Node, so we do it here at build time and hand the
 * browser a ready-to-embed TTF URL. No WASM ships to the client.
 *
 * Factory so we can expose multiple CJK faces (sans + serif) the same way.
 */
function fontsourceTtf(opts: {
  name: string;
  virtualId: string;
  devPath: string;
  woff2Specifier: string;
  assetName: string;
}): Plugin {
  const RESOLVED_ID = '\0' + opts.virtualId;

  let ttfPromise: Promise<Buffer> | null = null;
  const buildTtf = () => {
    if (!ttfPromise) {
      ttfPromise = (async () => {
        const woff2Path = require.resolve(opts.woff2Specifier);
        const woff2 = await readFile(woff2Path);
        const { decompress } = await import('wawoff2');
        const ttf = await decompress(new Uint8Array(woff2));
        return Buffer.from(ttf.buffer, ttf.byteOffset, ttf.byteLength);
      })();
    }
    return ttfPromise;
  };

  let isBuild = false;

  return {
    name: opts.name,
    configResolved(config) {
      isBuild = config.command === 'build';
    },
    resolveId(id) {
      if (id === opts.virtualId) return RESOLVED_ID;
      return null;
    },
    async load(id) {
      if (id !== RESOLVED_ID) return null;
      if (!isBuild) {
        return `export default ${JSON.stringify(opts.devPath)};`;
      }
      const ttf = await buildTtf();
      const ref = this.emitFile({
        type: 'asset',
        name: opts.assetName,
        source: ttf
      });
      return `export default import.meta.ROLLUP_FILE_URL_${ref};`;
    },
    configureServer(server) {
      server.middlewares.use(opts.devPath, async (_req, res) => {
        try {
          const ttf = await buildTtf();
          res.setHeader('Content-Type', 'font/ttf');
          res.setHeader('Cache-Control', 'no-cache');
          res.end(ttf);
        } catch (err) {
          res.statusCode = 500;
          res.end(String(err));
        }
      });
    }
  };
}

export default defineConfig({
  plugins: [
    vue(),
    fontsourceTtf({
      name: 'noto-sans-sc-ttf',
      virtualId: 'virtual:noto-sans-sc-ttf',
      devPath: '/@noto-sans-sc/font.ttf',
      woff2Specifier: '@fontsource/noto-sans-sc/files/noto-sans-sc-chinese-simplified-400-normal.woff2',
      assetName: 'NotoSansSC.ttf'
    }),
    fontsourceTtf({
      name: 'noto-serif-sc-ttf',
      virtualId: 'virtual:noto-serif-sc-ttf',
      devPath: '/@noto-serif-sc/font.ttf',
      woff2Specifier: '@fontsource/noto-serif-sc/files/noto-serif-sc-chinese-simplified-400-normal.woff2',
      assetName: 'NotoSerifSC.ttf'
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    strictPort: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  optimizeDeps: {
    include: ['jspdf']
  }
});
