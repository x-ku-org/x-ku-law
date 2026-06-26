/** 把 ISO / 后端时间字符串格式化为「YYYY-MM-DD HH:mm」；空值显示 —。 */
export function formatDateTime(value: unknown): string {
  if (value === null || value === undefined || value === '') return '—';
  const s = String(value);
  const m = s.match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2})/);
  if (m) return `${m[1]} ${m[2]}`;
  return s.length > 10 ? s.slice(0, 16).replace('T', ' ') : s;
}

/** 收件箱列表用的紧凑时间：当天显示 HH:mm，否则 MM-DD HH:mm。 */
export function formatInboxTime(value?: string): string {
  if (!value) return '';
  const t = new Date(value);
  if (Number.isNaN(t.getTime())) return value;
  const hm = `${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`;
  const now = new Date();
  const sameDay = t.getFullYear() === now.getFullYear() && t.getMonth() === now.getMonth() && t.getDate() === now.getDate();
  if (sameDay) return hm;
  return `${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')} ${hm}`;
}
