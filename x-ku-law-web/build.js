// Build script — concatenates JSX files + styles into a self-contained index.html
// Run via: run_script tool
const FILES = [
  'src/data.jsx',
  'src/brand.jsx',
  'src/shell.jsx',
  'src/pages/Home.jsx',
  'src/pages/Search.jsx',
  'src/pages/LawDetail.jsx',
  'src/pages/Compare.jsx',
  'src/pages/AIChat.jsx',
  'src/pages/Compliance.jsx',
  'src/pages/Task.jsx',
  'tweaks-panel.jsx',
  'src/mount.jsx',
];
let bundle = '';
for (const f of FILES) {
  const c = await readFile(f);
  bundle += `\n// === ${f} ===\n(function(){\n${c}\n})();\n`;
}
const css = await readFile('styles.css');

const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>X · KU 法规智询</title>

  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Source+Serif+4:ital,wght@0,300..700;1,300..700&family=Noto+Serif+SC:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet" />

  <style>
${css}
  </style>

  <script src="https://unpkg.com/react@18.3.1/umd/react.development.js" integrity="sha384-hD6/rw4ppMLGNu3tX5cjIb+uRZ7UkRJ6BPkLpg4hAu/6onKUg4lLsHAs9EBPT82L" crossorigin="anonymous"></script>
  <script src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.development.js" integrity="sha384-u6aeetuaXnQ38mYT8rp6sbXaQe3NL9t+IBXmnYxwkUI2Hw4bsp2Wvmx4yRQF1uAm" crossorigin="anonymous"></script>
  <script src="https://unpkg.com/@babel/standalone@7.29.0/babel.min.js" integrity="sha384-m08KidiNqLdpJqLq95G/LEi8Qvjl/xUYll3QILypMoQ65QorJ9Lvtp2RXYGBFj1y" crossorigin="anonymous"></script>
</head>
<body>
  <template id="__bundler_thumbnail" data-bg-color="#FFFFFF">
    <svg viewBox="0 0 1200 800" xmlns="http://www.w3.org/2000/svg">
      <rect width="1200" height="800" fill="#FFFFFF"/>
      <g transform="translate(600 400)">
        <g transform="scale(8)">
          <path d="M0,-40 L34.6,-20 L34.6,20 L0,40 L-34.6,20 L-34.6,-20 Z" fill="none" stroke="#0B1530" stroke-width="0.6" opacity="0.4"/>
          <path d="M-22,-12 L0,-24 L22,-12 L0,0 Z" fill="#0B1530" opacity="0.95"/>
          <path d="M-22,-12 L-22,12 L0,24 L0,0 Z" fill="#0B1530" opacity="0.78"/>
          <path d="M22,-12 L22,12 L0,24 L0,0 Z" fill="#0B1530" opacity="0.6"/>
          <path d="M0,4 L9,9 L0,14 L-9,9 Z" fill="#1E5BFF"/>
        </g>
      </g>
      <text x="600" y="700" text-anchor="middle" font-family="Georgia, serif" font-size="34" font-style="italic" fill="#0B1530" letter-spacing="0.04em">X · KU</text>
      <text x="600" y="740" text-anchor="middle" font-family="-apple-system, sans-serif" font-size="13" fill="#71758A" letter-spacing="0.14em">法规智询 · LOADING</text>
    </svg>
  </template>

  <div id="root"></div>

  <script type="text/babel" data-presets="react">
${bundle}
  </script>
</body>
</html>
`;
await saveFile('index.html', html);
log('Built index.html — ' + html.length + ' chars; bundle ' + bundle.length);
