export type DiffSegment = { type: 'same' | 'add' | 'del'; text: string };

/** Character-level LCS diff for palimpsest display. */
export function diffSegments(left: string, right: string): DiffSegment[] {
  const a = left || '';
  const b = right || '';
  if (!a && !b) return [];
  if (!a) return [{ type: 'add', text: b }];
  if (!b) return [{ type: 'del', text: a }];

  const m = a.length;
  const n = b.length;
  const dp: number[][] = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      dp[i][j] = a[i - 1] === b[j - 1] ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
    }
  }

  const raw: Array<{ type: 'same' | 'add' | 'del'; char: string }> = [];
  let i = m;
  let j = n;
  while (i > 0 || j > 0) {
    if (i > 0 && j > 0 && a[i - 1] === b[j - 1]) {
      raw.push({ type: 'same', char: a[i - 1] });
      i--;
      j--;
    } else if (j > 0 && (i === 0 || dp[i][j - 1] >= dp[i - 1][j])) {
      raw.push({ type: 'add', char: b[j - 1] });
      j--;
    } else {
      raw.push({ type: 'del', char: a[i - 1] });
      i--;
    }
  }
  raw.reverse();

  const merged: DiffSegment[] = [];
  for (const item of raw) {
    const last = merged[merged.length - 1];
    if (last && last.type === item.type) {
      last.text += item.char;
    } else {
      merged.push({ type: item.type, text: item.char });
    }
  }
  return merged;
}
