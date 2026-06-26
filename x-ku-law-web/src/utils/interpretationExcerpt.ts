/** 去掉常见 Markdown 标记，便于摘要做边注预览。 */
export function stripMarkdown(markdown: string): string {
  return markdown
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/^#{1,6}\s+/gm, '')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/\*([^*]+)\*/g, '$1')
    .replace(/^\s*[-*+]\s+/gm, '')
    .replace(/^\s*\d+\.\s+/gm, '')
    .replace(/\[(.+?)\]\(.+?\)/g, '$1')
    .replace(/\n{2,}/g, '\n')
    .trim();
}

/** 取整篇解读正文最前一段（边注固定摘要，不按条款匹配）。 */
export function excerptOpening(fullText: string, maxLen = 220): string {
  const plain = stripMarkdown(fullText);
  if (!plain) return '';
  if (plain.length <= maxLen) return plain;
  return `${plain.slice(0, maxLen).trim()}…`;
}

/** @deprecated 使用 excerptOpening */
export function excerptSummary(fullText: string, maxLen = 180): string {
  return excerptOpening(fullText, maxLen);
}
