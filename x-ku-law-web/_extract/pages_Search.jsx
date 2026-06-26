// === src/pages/Search.jsx ===
(function(){
// Search results — editorial index + time/level map
const { useState: useStateS, useMemo: useMemoS } = React;

function SearchPage({ go }) {
  const { SEARCH_RESULTS } = window.XKU_DATA;
  const [q, setQ] = useStateS('重要数据 出境');
  const [activeLevel, setActiveLevel] = useStateS('全部');

  const levels = ['全部', '法律', '行政法规', '部门规章', '地方性法规', '司法解释'];
  const filtered = activeLevel === '全部' ? SEARCH_RESULTS : SEARCH_RESULTS.filter(r => r.level === activeLevel);
  const totalHits = filtered.reduce((s, r) => s + r.hits, 0);

  return (
    <div className="page-enter" style={{ padding: '32px 48px 48px', maxWidth: 1440, margin: '0 auto' }}>

      {/* === Query header === */}
      <header style={{ marginBottom: 26 }}>
        <div className="t-meta-cap" style={{ marginBottom: 14 }}>
          <span className="section-mark">§</span> 检索 · SEARCH RESULTS
        </div>
        <div className="flex items-baseline gap-4" style={{ flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 480px', position: 'relative' }}>
            <input
              value={q}
              onChange={e => setQ(e.target.value)}
              style={{
                width: '100%',
                border: 0, borderBottom: '2px solid var(--ink)',
                outline: 0, background: 'transparent',
                fontFamily: 'var(--serif-display)',
                fontStyle: 'italic',
                fontSize: 56,
                lineHeight: 1,
                padding: '0 0 12px',
                color: 'var(--ink)',
                letterSpacing: '-0.015em',
              }}
            />
            <span style={{
              position: 'absolute', right: 0, top: 8,
              fontFamily: 'var(--mono)', fontSize: 11,
              color: 'var(--muted)',
            }}>↵ ENTER</span>
          </div>
        </div>
        <div className="t-mono" style={{ marginTop: 14, color: 'var(--ink-2)' }}>
          <span className="num" style={{ fontSize: 14, fontStyle: 'italic' }}>{totalHits}</span> 条命中 · 跨 <span className="num" style={{ fontSize: 14, fontStyle: 'italic' }}>{filtered.length}</span> 件文件 · 用时 0.21s
        </div>
      </header>

      {/* === Filter row === */}
      <div className="flex items-center gap-2" style={{ flexWrap: 'wrap', marginBottom: 8 }}>
        <span className="t-meta-cap" style={{ marginRight: 8 }}>效力层级</span>
        {levels.map(lv => (
          <button
            key={lv}
            onClick={() => setActiveLevel(lv)}
            className={`chip ${activeLevel === lv ? 'chip-accent' : 'chip-outline'}`}
            style={{ cursor: 'pointer', height: 24, fontSize: 11 }}
          >
            {lv}
          </button>
        ))}
        <span style={{ flex: 1 }}></span>
        <button className="btn btn-sm">
          <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M2 3 H10 M3 6 H9 M4.5 9 H7.5"/></svg>
          更多筛选
        </button>
        <button className="btn btn-sm">高级检索 →</button>
      </div>

      {/* === Level × Time matrix === */}
      <section style={{
        marginTop: 26,
        marginBottom: 36,
        padding: '26px 26px 16px',
        background: 'var(--paper-2)',
        border: '1px solid var(--rule)',
        borderRadius: 4,
        position: 'relative',
        overflow: 'hidden',
      }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
          <span className="t-meta-cap">
            <span className="section-mark">§</span> 结果分布 · LEVEL × TIME
          </span>
          <span className="t-mono" style={{ color: 'var(--muted)' }}>2018 — 2026</span>
        </div>
        <ResultMatrix items={filtered} go={go} />
      </section>

      {/* === Results list === */}
      <section>
        <div className="t-meta-cap" style={{ marginBottom: 16 }}>
          <span className="section-mark">§</span> 文件 · DOCUMENTS
        </div>
        <div style={{ borderTop: '1px solid var(--ink)' }}>
          {filtered.map((r, i) => (
            <article
              key={r.id}
              onClick={() => go('law')}
              style={{
                display: 'grid',
                gridTemplateColumns: '60px 1fr 200px',
                gap: 28,
                padding: '26px 0 28px',
                borderBottom: '1px solid var(--rule)',
                cursor: 'pointer',
                transition: 'background 0.15s var(--ease)',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--paper-2)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            >
              {/* Index */}
              <div>
                <div className="num" style={{ fontSize: 40, fontStyle: 'italic', lineHeight: 1, color: 'var(--muted-2)' }}>
                  {String(i + 1).padStart(2, '0')}
                </div>
                <div className="t-mono" style={{ marginTop: 6, color: 'var(--muted)' }}>{r.hits} hits</div>
              </div>

              {/* Body */}
              <div>
                <div className="flex items-center gap-2" style={{ marginBottom: 8, flexWrap: 'wrap' }}>
                  <span className="chip chip-outline" style={{ background: 'var(--paper-card)' }}>{r.level}</span>
                  <span className="chip chip-outline" style={{ background: 'var(--paper-card)' }}>{r.region}</span>
                  {r.status === '现行有效' && <span className="chip chip-moss">现行有效</span>}
                  {r.superseded && <span className="chip chip-rose">已修订</span>}
                  <span className="t-mono" style={{ color: 'var(--muted)' }}>{r.docNo}</span>
                </div>
                <h3 style={{
                  fontFamily: 'var(--serif-display)',
                  fontSize: 26,
                  fontWeight: 400,
                  lineHeight: 1.15,
                  letterSpacing: '-0.01em',
                  margin: '0 0 10px',
                  color: 'var(--ink)',
                }}>
                  {highlightTerms(r.title, q)}
                </h3>
                <p style={{
                  fontFamily: 'var(--serif-body)',
                  fontSize: 15,
                  lineHeight: 1.65,
                  color: 'var(--ink-3)',
                  margin: 0,
                  maxWidth: 720,
                }}>
                  …{highlightTerms(r.body, q)}…
                </p>
              </div>

              {/* Right meta */}
              <div style={{ textAlign: 'right' }}>
                <div className="t-meta-cap" style={{ marginBottom: 4 }}>施行</div>
                <div className="num" style={{ fontSize: 18, fontStyle: 'italic', color: 'var(--ink)', marginBottom: 14 }}>
                  {r.effective}
                </div>
                <div className="t-meta-cap" style={{ marginBottom: 4 }}>被引</div>
                <div className="flex items-baseline justify-end gap-1">
                  <span className="num" style={{ fontSize: 20, fontStyle: 'italic', color: 'var(--ink)' }}>{r.cited}</span>
                  <span className="t-mono" style={{ color: 'var(--muted)' }}>次</span>
                </div>
              </div>
            </article>
          ))}
        </div>

        {/* pagination footer */}
        <div className="flex items-center justify-between" style={{ marginTop: 30 }}>
          <div className="t-mono" style={{ color: 'var(--muted)' }}>显示 1–{filtered.length} · 共 {totalHits} 命中</div>
          <div className="flex gap-2">
            <button className="btn btn-sm" disabled style={{ opacity: 0.4 }}>← 上一页</button>
            <button className="btn btn-sm btn-primary">下一页 →</button>
          </div>
        </div>
      </section>
    </div>
  );
}

function highlightTerms(text, q) {
  if (!q) return text;
  const terms = q.split(/\\s+/).filter(Boolean);
  let parts = [text];
  terms.forEach(t => {
    parts = parts.flatMap(p => {
      if (typeof p !== 'string') return [p];
      const out = [];
      let last = 0;
      const re = new RegExp(t.replace(/[.*+?^${}()|[\\]\\\\]/g, '\\\\$&'), 'g');
      let m;
      while ((m = re.exec(p)) !== null) {
        if (m.index > last) out.push(p.slice(last, m.index));
        out.push(<span key={m.index + t} className="hl">{m[0]}</span>);
        last = m.index + m[0].length;
      }
      if (last < p.length) out.push(p.slice(last));
      return out;
    });
  });
  return parts;
}

function ResultMatrix({ items, go }) {
  const levels = ['法律', '行政法规', '部门规章', '地方性法规', '司法解释'];
  const minYear = 2018, maxYear = 2026;

  // Layout
  const W = 1100, H = 160;
  const padL = 90, padR = 12, padT = 14, padB = 28;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;
  const xFor = (date) => {
    const y = parseInt(date.slice(0, 4));
    const m = parseInt(date.slice(5, 7)) || 1;
    const frac = (y - minYear) + (m - 1) / 12;
    return padL + (frac / (maxYear - minYear)) * innerW;
  };
  const yFor = (lv) => {
    const idx = levels.indexOf(lv);
    const ri = idx < 0 ? levels.length - 1 : idx;
    return padT + (ri + 0.5) * (innerH / levels.length);
  };

  return (
    <svg viewBox={`0 0 ${W} ${H}`} style={{ width: '100%', display: 'block' }}>
      {/* horizontal level rows */}
      {levels.map((lv, i) => (
        <g key={lv}>
          <line
            x1={padL} x2={W - padR}
            y1={padT + i * (innerH / levels.length)}
            y2={padT + i * (innerH / levels.length)}
            stroke="var(--rule)"
            strokeWidth="0.5"
            strokeDasharray={i === 0 ? '' : '2 3'}
          />
          <text
            x={padL - 10}
            y={padT + (i + 0.5) * (innerH / levels.length) + 4}
            textAnchor="end"
            fontSize="11"
            fontFamily="var(--sans)"
            fill="var(--muted)"
            letterSpacing="0.04em"
          >{lv}</text>
        </g>
      ))}
      {/* bottom rule */}
      <line x1={padL} x2={W - padR} y1={padT + innerH} y2={padT + innerH} stroke="var(--ink)" strokeWidth="0.5" />
      {/* year ticks */}
      {Array.from({ length: maxYear - minYear + 1 }).map((_, i) => {
        const y = minYear + i;
        const x = padL + (i / (maxYear - minYear)) * innerW;
        return (
          <g key={y}>
            <line x1={x} y1={padT + innerH} x2={x} y2={padT + innerH + 4} stroke="var(--rule-strong)" strokeWidth="0.5" />
            <text x={x} y={H - 8} textAnchor="middle" fontSize="10" fontFamily="var(--mono)" fill="var(--muted)">{y}</text>
          </g>
        );
      })}
      {/* nodes */}
      {items.map((r, i) => {
        const x = xFor(r.effective);
        const y = yFor(r.level);
        const radius = 4 + Math.min(r.hits, 14) * 0.5;
        return (
          <g key={r.id} style={{ cursor: 'pointer' }} onClick={() => go('law')}>
            <circle cx={x} cy={y} r={radius + 4} fill="var(--accent)" opacity="0.06" />
            <circle cx={x} cy={y} r={radius} fill={r.superseded ? 'var(--rose)' : 'var(--accent)'} opacity={r.superseded ? 0.7 : 0.9} />
            <text x={x} y={y - radius - 5} textAnchor="middle" fontSize="9" fontFamily="var(--sans)" fill="var(--ink-3)">
              {r.hits}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

window.SearchPage = SearchPage;

})();

