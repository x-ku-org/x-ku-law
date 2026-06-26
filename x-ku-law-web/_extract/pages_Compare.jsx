// === src/pages/Compare.jsx ===
(function(){
// Version compare — palimpsest diff
const { useState: useStateC, useMemo: useMemoC } = React;

function ComparePage({ go }) {
  const { VERSION_DIFF, MOCK_LAW } = window.XKU_DATA;
  const [focusIdx, setFocusIdx] = useStateC(0);

  // diff stats
  const stats = useMemoC(() => {
    let added = 0, modified = 0, removed = 0;
    VERSION_DIFF.forEach(d => {
      if (d.change === 'added') added++;
      else if (d.change === 'modified') modified++;
      else if (d.change === 'removed') removed++;
    });
    return { added, modified, removed };
  }, []);

  return (
    <div className="page-enter" style={{ padding: '32px 48px 48px', maxWidth: 1480, margin: '0 auto' }}>

      {/* === Editorial header === */}
      <header style={{ marginBottom: 32 }}>
        <div className="t-meta-cap" style={{ marginBottom: 14 }}>
          <span className="section-mark">§</span> 版本对比 · DIFF
        </div>
        <h1 style={{
          fontFamily: 'var(--serif-display)',
          fontSize: 56,
          lineHeight: 1.05,
          letterSpacing: '-0.02em',
          fontWeight: 400,
          margin: '0 0 14px',
        }}>
          <em>{MOCK_LAW.title}</em>
        </h1>
        <div className="flex items-center gap-3" style={{ flexWrap: 'wrap' }}>
          <span className="chip chip-outline" style={{ height: 26, fontSize: 12 }}>v1 · 2021-09-01</span>
          <svg width="20" height="14" viewBox="0 0 20 14" fill="none"><path d="M2 7 H17 M14 4 L17 7 L14 10" stroke="var(--ink)" strokeWidth="1.2"/></svg>
          <span className="chip chip-accent" style={{ height: 26, fontSize: 12 }}>v2 · 2024-09-01</span>
          <span style={{ flex: 1 }}></span>
          <button className="btn btn-sm">
            <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M2 8 V10 H10 V8 M6 2 V8 M3 5 L6 2 L9 5"/></svg>
            导出对比报告
          </button>
        </div>
      </header>

      {/* === Diff overview strip === */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: '4fr 5fr',
        gap: 32,
        marginBottom: 40,
      }}>
        {/* counts */}
        <div className="card" style={{ padding: '22px 26px' }}>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>差异摘要 · SUMMARY</div>
          <div className="flex items-baseline justify-between">
            <DiffStat n={stats.added}    label="新增" color="var(--moss)" />
            <div style={{ width: 1, alignSelf: 'stretch', background: 'var(--rule)' }}></div>
            <DiffStat n={stats.modified} label="修订" color="var(--gold)" />
            <div style={{ width: 1, alignSelf: 'stretch', background: 'var(--rule)' }}></div>
            <DiffStat n={stats.removed}  label="删除" color="var(--rose)" />
            <div style={{ width: 1, alignSelf: 'stretch', background: 'var(--rule)' }}></div>
            <DiffStat n={55}            label="总条款数" color="var(--ink)" />
          </div>
        </div>

        {/* editorial summary */}
        <div style={{ padding: '4px 0' }}>
          <div className="t-meta-cap" style={{ marginBottom: 10 }}>编辑摘要 · EDITORIAL NOTE</div>
          <p style={{
            fontFamily: 'var(--serif-display)',
            fontStyle: 'italic',
            fontSize: 22,
            lineHeight: 1.35,
            color: 'var(--ink-2)',
            margin: 0,
          }}>
            "v2 在数据分类分级与重要数据出境两条核心制度上做了实质性扩张；
            <span style={{ color: 'var(--accent)' }}>首次写入"数据要素市场"。</span>"
          </p>
          <div className="t-mono" style={{ marginTop: 14, color: 'var(--muted)' }}>
            — X-KU 编辑部 · 2024-09-02
          </div>
        </div>
      </section>

      {/* === Article navigator strip === */}
      <section style={{ marginBottom: 18 }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 14 }}>
          <div className="t-meta-cap">
            <span className="section-mark">§</span> 条款差异 · ARTICLE BY ARTICLE
          </div>
          <div className="t-mono" style={{ color: 'var(--muted)' }}>滑动查看 ←→</div>
        </div>

        <div style={{
          display: 'flex',
          gap: 0,
          borderTop: '1px solid var(--ink)',
          borderBottom: '1px solid var(--rule)',
        }}>
          {VERSION_DIFF.map((d, i) => (
            <button
              key={d.no}
              onClick={() => setFocusIdx(i)}
              style={{
                flex: 1,
                padding: '14px 16px 12px',
                textAlign: 'left',
                background: focusIdx === i ? 'var(--paper-2)' : 'transparent',
                border: 0,
                borderRight: i < VERSION_DIFF.length - 1 ? '1px solid var(--rule)' : 'none',
                cursor: 'pointer',
                fontFamily: 'inherit',
                color: 'inherit',
                position: 'relative',
              }}
            >
              {focusIdx === i && (
                <span style={{
                  position: 'absolute', top: -1, left: 0, right: 0,
                  height: 2, background: 'var(--accent)',
                }}></span>
              )}
              <div className="flex items-center gap-2" style={{ marginBottom: 6 }}>
                <ChangeChip change={d.change} />
                <span className="t-mono" style={{ color: 'var(--muted)' }}>{d.no}</span>
              </div>
              <div style={{
                fontFamily: 'var(--serif-display)',
                fontStyle: 'italic',
                fontSize: 16,
                color: 'var(--ink)',
              }}>{d.title}</div>
            </button>
          ))}
        </div>
      </section>

      {/* === Palimpsest diff === */}
      <DiffSpread item={VERSION_DIFF[focusIdx]} />

      {/* === Other diffs preview === */}
      <section style={{ marginTop: 56 }}>
        <div className="t-meta-cap" style={{ marginBottom: 16 }}>
          其他差异预览 · NEXT IN SERIES
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 18 }}>
          {VERSION_DIFF.map((d, i) => i !== focusIdx && (
            <button
              key={d.no}
              onClick={() => setFocusIdx(i)}
              className="card"
              style={{
                padding: '18px 18px 16px',
                cursor: 'pointer',
                textAlign: 'left',
                fontFamily: 'inherit',
                background: 'var(--paper-card)',
                color: 'inherit',
                transition: 'transform 0.15s var(--ease), border-color 0.15s var(--ease)',
              }}
              onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--ink)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
              onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--rule)'; e.currentTarget.style.transform = 'translateY(0)'; }}
            >
              <div className="flex items-center gap-2" style={{ marginBottom: 8 }}>
                <ChangeChip change={d.change} />
                <span className="t-mono" style={{ color: 'var(--muted)' }}>{d.no}</span>
              </div>
              <div style={{
                fontFamily: 'var(--serif-display)',
                fontStyle: 'italic',
                fontSize: 18,
                color: 'var(--ink)',
                marginBottom: 8,
              }}>{d.title}</div>
              <div className="marginalia" style={{ borderLeft: 'none', paddingLeft: 0, fontSize: 12 }}>
                {d.note}
              </div>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}

function DiffStat({ n, label, color }) {
  return (
    <div style={{ textAlign: 'center', flex: 1, padding: '0 16px' }}>
      <div className="num" style={{ fontSize: 42, fontStyle: 'italic', lineHeight: 1, color }}>{n}</div>
      <div className="t-meta-cap" style={{ marginTop: 6 }}>{label}</div>
    </div>
  );
}

function ChangeChip({ change }) {
  const map = {
    added:    { c: 'chip-moss', t: '新增' },
    modified: { c: 'chip-gold', t: '修订' },
    removed:  { c: 'chip-rose', t: '删除' },
  };
  const m = map[change] || { c: 'chip-outline', t: change };
  return <span className={`chip ${m.c}`} style={{ height: 18, fontSize: 10 }}>{m.t}</span>;
}

function DiffSpread({ item }) {
  if (!item) return null;
  const segs = useMemoC(() => diffSegments(item.left.text, item.right.text), [item]);

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '1fr 60px 1fr',
      gap: 0,
      background: 'var(--paper-2)',
      border: '1px solid var(--rule)',
      borderRadius: 4,
      minHeight: 380,
    }}>
      {/* LEFT pane */}
      <DiffPane
        version={item.left.v}
        text={item.left.text}
        segs={segs.left}
        no={item.no}
        title={item.title}
        side="left"
      />

      {/* center gutter with article number + change indicator */}
      <div style={{
        borderLeft: '1px solid var(--rule)',
        borderRight: '1px solid var(--rule)',
        background: 'var(--paper)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '28px 0',
      }}>
        <div style={{
          fontFamily: 'var(--serif-display)',
          fontStyle: 'italic',
          fontSize: 14,
          color: 'var(--muted)',
          writingMode: 'vertical-rl',
          letterSpacing: '0.05em',
          marginBottom: 12,
        }}>
          {item.no}
        </div>
        <Diamond size={10} color="var(--accent)" />
        <div style={{ flex: 1, width: 1, background: 'var(--rule-strong)', margin: '12px 0' }}></div>
        <ChangeChip change={item.change} />
        <div style={{ flex: 1, width: 1, background: 'var(--rule-strong)', margin: '12px 0' }}></div>
      </div>

      {/* RIGHT pane */}
      <DiffPane
        version={item.right.v}
        text={item.right.text}
        segs={segs.right}
        no={item.no}
        title={item.title}
        side="right"
      />

      {/* note ribbon */}
      <div style={{
        gridColumn: '1 / -1',
        borderTop: '1px solid var(--rule)',
        padding: '14px 28px',
        background: 'var(--paper)',
        display: 'flex',
        alignItems: 'center',
        gap: 14,
      }}>
        <div className="t-meta-cap" style={{ color: 'var(--accent)' }}>
          <Diamond size={6} /> 编辑解读
        </div>
        <span style={{
          fontFamily: 'var(--serif-body)',
          fontStyle: 'italic',
          fontSize: 14,
          color: 'var(--ink-2)',
        }}>
          {item.note}
        </span>
      </div>
    </div>
  );
}

function DiffPane({ version, text, segs, no, title, side }) {
  return (
    <div style={{ padding: '24px 28px 22px', position: 'relative' }}>
      <div className="flex items-baseline justify-between" style={{ marginBottom: 12 }}>
        <span className="t-meta-cap" style={{ color: side === 'left' ? 'var(--muted)' : 'var(--accent)' }}>
          {side === 'left' ? '旧版' : '现版'} · {version}
        </span>
      </div>
      <div className="t-meta-cap" style={{ marginBottom: 4, color: 'var(--muted-2)' }}>
        {no} · {title}
      </div>
      <p style={{
        fontFamily: '"Noto Serif SC", serif',
        fontSize: 16,
        lineHeight: 1.85,
        color: 'var(--ink-2)',
        margin: '14px 0 0',
        letterSpacing: '0.005em',
        textAlign: 'justify',
      }}>
        {segs.map((seg, i) => (
          <span key={i} style={{
            background:
              seg.kind === 'add' ? 'rgba(79, 110, 59, 0.18)' :
              seg.kind === 'del' ? 'rgba(181, 67, 47, 0.16)' :
              'transparent',
            color: seg.kind === 'del' ? 'var(--rose)' : 'var(--ink-2)',
            textDecoration: seg.kind === 'del' ? 'line-through' : 'none',
            textDecorationColor: 'var(--rose)',
            padding: seg.kind !== 'eq' ? '0 2px' : 0,
            borderRadius: 2,
          }}>{seg.text}</span>
        ))}
      </p>
    </div>
  );
}

// Very simple character-level LCS-based diff for short paragraphs
function diffSegments(a, b) {
  // Simple longest-common-subsequence (DP)
  const n = a.length, m = b.length;
  // limit size guard
  if (n + m > 1500) {
    // fallback — just mark whole strings
    return {
      left:  [{ kind: 'eq', text: a }],
      right: [{ kind: 'eq', text: b }],
    };
  }
  const dp = Array.from({ length: n + 1 }, () => new Int32Array(m + 1));
  for (let i = n - 1; i >= 0; i--) {
    for (let j = m - 1; j >= 0; j--) {
      dp[i][j] = a[i] === b[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1]);
    }
  }
  const left = [], right = [];
  let i = 0, j = 0;
  while (i < n && j < m) {
    if (a[i] === b[j]) {
      push(left, 'eq', a[i]); push(right, 'eq', b[j]); i++; j++;
    } else if (dp[i + 1][j] >= dp[i][j + 1]) {
      push(left, 'del', a[i]); i++;
    } else {
      push(right, 'add', b[j]); j++;
    }
  }
  while (i < n) { push(left,  'del', a[i++]); }
  while (j < m) { push(right, 'add', b[j++]); }
  return { left, right };
}
function push(arr, kind, ch) {
  const last = arr[arr.length - 1];
  if (last && last.kind === kind) last.text += ch;
  else arr.push({ kind, text: ch });
}

window.ComparePage = ComparePage;

})();

