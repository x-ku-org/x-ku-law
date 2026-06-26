// 用 Noto Serif SC（衬线）嵌入 PDF，与网页正文 --serif-body 字体保持一致观感。
import fontTtfUrl from 'virtual:noto-serif-sc-ttf';

const FONT_FILE = 'NotoSerifSC.ttf';
const FONT_FAMILY = 'NotoSerifSC';

type BlockKind = 'h1' | 'h2' | 'h3' | 'p' | 'li' | 'table';

interface MarkdownBlock {
  kind: BlockKind;
  text?: string;
  /** kind === 'table' 时的单元格矩阵，首行为表头。 */
  rows?: string[][];
}

type JsPDFInstance = InstanceType<(typeof import('jspdf'))['jsPDF']>;

let cachedFontBase64: string | null = null;

function sanitizeFilename(name: string) {
  return name.replace(/[\\/:*?"<>|]/g, '_').slice(0, 80);
}

function arrayBufferToBase64(buffer: ArrayBuffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunk));
  }
  return btoa(binary);
}

async function loadChineseFont(doc: JsPDFInstance) {
  if (!cachedFontBase64) {
    const response = await fetch(fontTtfUrl);
    if (!response.ok) {
      throw new Error('font load failed');
    }
    cachedFontBase64 = arrayBufferToBase64(await response.arrayBuffer());
  }
  doc.addFileToVFS(FONT_FILE, cachedFontBase64);
  doc.addFont(FONT_FILE, FONT_FAMILY, 'normal');
  doc.setFont(FONT_FAMILY);
}

function stripInlineMarkdown(text: string) {
  return text
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/\*(.+?)\*/g, '$1')
    .replace(/`(.+?)`/g, '$1')
    .replace(/\[(.+?)\]\(.+?\)/g, '$1')
    .trim();
}

// 不能出现在行首的标点（行首禁则）
const NO_LINE_START = new Set('。．，、；：？！）〕〉》」』】｝〗〙｠’”〞·…々ー～:;,.?!)]}>'.split(''));
// 不能出现在行尾的标点（行尾禁则）
const NO_LINE_END = new Set('（〔〈《「『【｛〖〘｟‘“([{<'.split(''));

// jsPDF 的 splitTextToSize 不懂中文断行规则，会把闭合标点（如 。）」）甩到下一行行首。
// 这里做一次禁则处理：把不该在行首的标点拉回上一行，把不该在行尾的开符号下移到下一行。
function applyCjkLineBreaks(lines: string[]): string[] {
  const out: string[] = [];
  for (const raw of lines) {
    let line = raw;
    while (line && out.length && NO_LINE_START.has(line.charAt(0))) {
      out[out.length - 1] += line.charAt(0);
      line = line.slice(1);
    }
    while (out.length && line && NO_LINE_END.has(out[out.length - 1].slice(-1))) {
      const prev = out[out.length - 1];
      line = prev.slice(-1) + line;
      out[out.length - 1] = prev.slice(0, -1);
      if (!out[out.length - 1]) out.pop();
    }
    if (line) out.push(line);
  }
  return out.length ? out : lines;
}

function isTableSeparator(line: string) {
  return /\|/.test(line) && /-/.test(line) && /^[\s|:-]+$/.test(line);
}
function looksLikeTableRow(line: string) {
  return line.includes('|');
}
function parseTableRow(line: string): string[] {
  let s = line.trim();
  if (s.startsWith('|')) s = s.slice(1);
  if (s.endsWith('|')) s = s.slice(0, -1);
  return s.split('|').map((c) => stripInlineMarkdown(c.trim()));
}

function parseMarkdownBlocks(source: string): MarkdownBlock[] {
  const blocks: MarkdownBlock[] = [];
  const lines = source.replace(/\r\n/g, '\n').split('\n');
  let paragraph: string[] = [];

  const flushParagraph = () => {
    const text = paragraph.map(stripInlineMarkdown).join(' ').trim();
    if (text) blocks.push({ kind: 'p', text });
    paragraph = [];
  };

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) {
      flushParagraph();
      continue;
    }

    if (looksLikeTableRow(line) && i + 1 < lines.length && isTableSeparator(lines[i + 1].trim())) {
      flushParagraph();
      const rows: string[][] = [parseTableRow(line)];
      i += 2; // 跳过表头与分隔行
      while (i < lines.length) {
        const next = lines[i].trim();
        if (!next || !looksLikeTableRow(next)) break;
        rows.push(parseTableRow(next));
        i += 1;
      }
      i -= 1; // 回退一格，交还给 for 自增
      blocks.push({ kind: 'table', rows });
      continue;
    }

    if (/^(-{3,}|\*{3,}|_{3,})$/.test(line)) {
      flushParagraph();
      continue;
    }

    const heading = line.match(/^(#{1,6})\s+(.+)/);
    if (heading) {
      flushParagraph();
      const level = heading[1].length;
      const kind: BlockKind = level <= 1 ? 'h1' : level === 2 ? 'h2' : 'h3';
      blocks.push({ kind, text: stripInlineMarkdown(heading[2]) });
      continue;
    }

    const section = line.match(/^[一二三四五六七八九十]+[、.．]\s*(.+)/);
    if (section) {
      flushParagraph();
      blocks.push({ kind: 'h2', text: stripInlineMarkdown(line) });
      continue;
    }

    const bullet = line.match(/^[-*+]\s+(.+)/);
    if (bullet) {
      flushParagraph();
      blocks.push({ kind: 'li', text: stripInlineMarkdown(bullet[1]) });
      continue;
    }

    const numbered = line.match(/^\d+[.)．、]\s+(.+)/);
    if (numbered) {
      flushParagraph();
      blocks.push({ kind: 'li', text: stripInlineMarkdown(numbered[1]) });
      continue;
    }

    paragraph.push(line);
  }

  flushParagraph();
  return blocks;
}

function blockStyle(kind: BlockKind) {
  switch (kind) {
    case 'h1':
      return { fontSize: 16, lineHeight: 7.5, gapBefore: 5, gapAfter: 2, color: [11, 21, 48] as const };
    case 'h2':
      return { fontSize: 14, lineHeight: 7, gapBefore: 4, gapAfter: 1.5, color: [11, 21, 48] as const };
    case 'h3':
      return { fontSize: 12.5, lineHeight: 6.5, gapBefore: 3, gapAfter: 1, color: [11, 21, 48] as const };
    case 'li':
      return { fontSize: 11, lineHeight: 6, gapBefore: 1, gapAfter: 0.5, color: [42, 53, 83] as const };
    default:
      return { fontSize: 11, lineHeight: 6, gapBefore: 1.5, gapAfter: 1, color: [42, 53, 83] as const };
  }
}

/** 附录里的一条被引用法规条款。 */
export interface CitationAppendixItem {
  id: string;
  source?: string;
  article?: string;
  excerpt?: string;
}

interface ExportPdfOptions {
  /** 副标题（标题下方的小字署名），默认 “X-KU 法规智阅 · AI 解读”。 */
  subtitle?: string;
  /** 文件名后缀，默认 “AI解读” → `<title>-AI解读.pdf`。 */
  filenameSuffix?: string;
  /** 被引用的法规条款，作为正文之后的「附录」逐条列出。 */
  appendix?: CitationAppendixItem[];
}

export async function downloadInterpretationPdf(
  title: string,
  markdown: string,
  options: ExportPdfOptions = {}
): Promise<boolean> {
  const content = markdown.trim();
  if (!content) return false;

  const subtitle = options.subtitle ?? 'X-KU 法规智阅 · AI 解读';
  const filenameSuffix = options.filenameSuffix ?? 'AI解读';
  const appendix = options.appendix ?? [];

  try {
    const { jsPDF } = await import('jspdf');
    const doc = new jsPDF({ unit: 'mm', format: 'a4', orientation: 'portrait' });
    await loadChineseFont(doc);

    const margin = 16;
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const maxWidth = pageWidth - margin * 2;
    let y = margin;

    const ensureSpace = (height: number) => {
      if (y + height > pageHeight - margin) {
        doc.addPage();
        y = margin;
      }
    };

    const writeLines = (lines: string[], style: ReturnType<typeof blockStyle>, prefix = '') => {
      doc.setFontSize(style.fontSize);
      doc.setTextColor(style.color[0], style.color[1], style.color[2]);
      for (const line of lines) {
        ensureSpace(style.lineHeight);
        doc.text(`${prefix}${line}`, margin, y);
        y += style.lineHeight;
      }
    };

    const renderTable = (rawRows: string[][]) => {
      const fontSize = 8.5;
      const lineHeight = 4.2;
      const padX = 1.6;
      const padY = 1.4;
      doc.setFontSize(fontSize);

      const colCount = Math.max(...rawRows.map((r) => r.length));
      const rows = rawRows.map((r) => {
        const a = r.slice(0, colCount);
        while (a.length < colCount) a.push('');
        return a;
      });

      const natural = new Array<number>(colCount).fill(8);
      for (const r of rows) {
        for (let c = 0; c < colCount; c++) {
          const w = doc.getTextWidth(r[c]) + padX * 2;
          if (w > natural[c]) natural[c] = w;
        }
      }
      const totalNat = natural.reduce((a, b) => a + b, 0) || maxWidth;
      const scale = maxWidth / totalNat;
      const widths = natural.map((w) => w * scale);

      doc.setDrawColor(206, 211, 222);
      doc.setLineWidth(0.2);

      const drawRow = (cells: string[], isHeader: boolean) => {
        const wrapped = cells.map((cell, c) =>
          applyCjkLineBreaks(doc.splitTextToSize(cell, widths[c] - padX * 2) as string[])
        );
        const maxLines = Math.max(1, ...wrapped.map((w) => w.length));
        const rowH = maxLines * lineHeight + padY * 2;
        ensureSpace(rowH);
        let x = margin;
        for (let c = 0; c < colCount; c++) {
          if (isHeader) {
            doc.setFillColor(241, 243, 247);
            doc.rect(x, y, widths[c], rowH, 'FD');
            doc.setTextColor(11, 21, 48);
          } else {
            doc.rect(x, y, widths[c], rowH, 'S');
            doc.setTextColor(42, 53, 83);
          }
          let ty = y + padY + lineHeight - 1.1;
          for (const ln of wrapped[c]) {
            doc.text(ln, x + padX, ty);
            ty += lineHeight;
          }
          x += widths[c];
        }
        y += rowH;
      };

      rows.forEach((row, idx) => drawRow(row, idx === 0));
    };

    doc.setFontSize(18);
    doc.setTextColor(11, 21, 48);
    for (const line of applyCjkLineBreaks(doc.splitTextToSize(title.trim(), maxWidth))) {
      ensureSpace(8);
      doc.text(line, margin, y);
      y += 8;
    }

    y += 2;
    doc.setFontSize(11);
    doc.setTextColor(74, 83, 111);
    ensureSpace(6);
    doc.text(subtitle, margin, y);
    y += 8;

    for (const block of parseMarkdownBlocks(content)) {
      if (block.kind === 'table' && block.rows?.length) {
        y += 2;
        renderTable(block.rows);
        y += 3;
        continue;
      }
      const style = blockStyle(block.kind);
      y += style.gapBefore;
      const prefix = block.kind === 'li' ? '• ' : '';
      const textWidth = block.kind === 'li' ? maxWidth - 4 : maxWidth;
      const lines = applyCjkLineBreaks(doc.splitTextToSize(block.text ?? '', textWidth) as string[]);
      writeLines(lines, style, prefix);
      y += style.gapAfter;
    }

    // 附录：被引用的法规条款，逐条「[n] 法源 · 条号」+ 原文摘录。
    if (appendix.length) {
      const h2 = blockStyle('h2');
      y += 6;
      ensureSpace(h2.lineHeight + 2);
      // 分隔线，区隔正文与附录
      doc.setDrawColor(206, 211, 222);
      doc.setLineWidth(0.3);
      doc.line(margin, y, pageWidth - margin, y);
      y += 5;
      writeLines(applyCjkLineBreaks(doc.splitTextToSize('附录 · 引用法规条款', maxWidth)), h2);
      y += h2.gapAfter;

      const h3 = blockStyle('h3');
      const p = blockStyle('p');
      for (const cite of appendix) {
        const head = [cite.id, [cite.source, cite.article].filter(Boolean).join(' · ')]
          .filter(Boolean)
          .join('  ');
        y += h3.gapBefore;
        writeLines(applyCjkLineBreaks(doc.splitTextToSize(head, maxWidth)), h3);
        if (cite.excerpt) {
          writeLines(applyCjkLineBreaks(doc.splitTextToSize(cite.excerpt, maxWidth)), p);
        }
        y += h3.gapAfter;
      }
    }

    doc.save(`${sanitizeFilename(title)}-${filenameSuffix}.pdf`);
    return true;
  } catch {
    return false;
  }
}
