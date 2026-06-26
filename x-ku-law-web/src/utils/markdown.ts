import DOMPurify from 'dompurify';
import { marked } from 'marked';

marked.setOptions({
  breaks: true,
  gfm: true
});

/** 将 AI / 解读类 Markdown 转为可安全插入 DOM 的 HTML。 */
export function renderMarkdown(source: string): string {
  const text = source?.trim();
  if (!text) return '';
  const html = marked.parse(text, { async: false }) as string;
  return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } });
}
