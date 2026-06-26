/** 收件箱按相对日期分组（今天 / 昨天 / 本周 / 更早），用于预警与消息中心。 */

export interface DateGroup<T> {
  key: 'today' | 'yesterday' | 'week' | 'earlier';
  label: string;
  items: T[];
}

const ORDER: { key: DateGroup<unknown>['key']; label: string }[] = [
  { key: 'today', label: '今天' },
  { key: 'yesterday', label: '昨天' },
  { key: 'week', label: '本周' },
  { key: 'earlier', label: '更早' }
];

function startOfDay(d: Date): number {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
}

function bucketOf(time?: string): DateGroup<unknown>['key'] {
  if (!time) return 'earlier';
  const t = new Date(time);
  if (Number.isNaN(t.getTime())) return 'earlier';
  const today = startOfDay(new Date());
  const day = startOfDay(t);
  const diffDays = Math.round((today - day) / 86_400_000);
  if (diffDays <= 0) return 'today';
  if (diffDays === 1) return 'yesterday';
  if (diffDays <= 7) return 'week';
  return 'earlier';
}

/**
 * 按相对日期分组，保留输入顺序（后端已按时间倒序）。仅返回非空分组。
 */
export function groupByDate<T>(list: T[], getTime: (item: T) => string | undefined): DateGroup<T>[] {
  const map = new Map<DateGroup<T>['key'], T[]>();
  for (const item of list) {
    const key = bucketOf(getTime(item));
    if (!map.has(key)) map.set(key, []);
    map.get(key)!.push(item);
  }
  return ORDER.filter((g) => map.has(g.key)).map((g) => ({ key: g.key, label: g.label, items: map.get(g.key)! }));
}
