// === src/pages/Compliance.jsx ===
(function(){
// Compliance dashboard — editorial data viz
const { useState: useStateCo, useMemo: useMemoCo } = React;

function Compliance({ go }) {
  const { COMPLIANCE_SUBJECTS, COMPLIANCE_TASKS } = window.XKU_DATA;

  return (
    <div className="page-enter" style={{ padding: '32px 48px 56px', maxWidth: 1440, margin: '0 auto' }}>

      {/* === Editorial masthead === */}
      <header style={{ marginBottom: 32 }}>
        <div className="flex justify-between items-end">
          <div>
            <div className="t-meta-cap" style={{ marginBottom: 12 }}>
              <span className="section-mark">§</span> 合规工作台 · COMPLIANCE
            </div>
            <h1 style={{
              fontFamily: 'var(--serif-display)',
              fontSize: 60,
              lineHeight: 0.95,
              letterSpacing: '-0.02em',
              fontWeight: 400,
              margin: 0,
            }}>
              <em>四个主体</em>，<br/>
              <span className="num" style={{ color: 'var(--accent)' }}>31</span> 项待办，
              <span className="num" style={{ color: 'var(--rose)' }}>8</span> 项<em>逾期</em>。
            </h1>
          </div>
          <div className="flex gap-2">
            <button className="btn btn-sm">导出报告</button>
            <button className="btn btn-sm btn-primary">新建任务</button>
          </div>
        </div>
      </header>

      <div style={{ borderTop: '1px solid var(--ink)' }}></div>

      {/* === Score strip === */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: '320px 1fr 1fr',
        gap: 0,
        borderBottom: '1px solid var(--rule)',
      }}>
        {/* Big score */}
        <div style={{ padding: '32px 28px 28px', borderRight: '1px solid var(--rule)' }}>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>整体合规指数</div>
          <div className="flex items-baseline gap-3">
            <span className="num" style={{ fontSize: 96, fontStyle: 'italic', lineHeight: 0.95, color: 'var(--ink)' }}>78</span>
            <div style={{ paddingBottom: 14 }}>
              <span className="t-mono" style={{ color: 'var(--moss)', fontSize: 12 }}>▲ 4 · 7d</span>
              <div className="t-mono" style={{ color: 'var(--muted)', marginTop: 2 }}>组内排名 6 / 23</div>
            </div>
          </div>
        </div>

        {/* Stat 2 */}
        <div style={{ padding: '32px 28px 28px', borderRight: '1px solid var(--rule)' }}>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>本月完成事项</div>
          <div className="flex items-baseline gap-3">
            <span className="num" style={{ fontSize: 60, fontStyle: 'italic', lineHeight: 0.95 }}>138</span>
            <span className="t-mono" style={{ color: 'var(--muted)', paddingBottom: 12 }}>/ 192 计划</span>
          </div>
          <div style={{ marginTop: 12, height: 6, background: 'var(--paper-sunk)', borderRadius: 100, overflow: 'hidden' }}>
            <div style={{ width: '72%', height: '100%', background: 'var(--accent)' }}></div>
          </div>
          <div className="t-mono" style={{ marginTop: 8, color: 'var(--muted)' }}>完成率 72% · 较上月 +5pp</div>
        </div>

        {/* Stat 3 */}
        <div style={{ padding: '32px 28px 28px' }}>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>风险等级分布</div>
          <RiskBreakdown />
        </div>
      </section>

      {/* === Time river — obligations by month === */}
      <section style={{ padding: '36px 0 30px', borderBottom: '1px solid var(--rule)' }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
          <span className="t-meta-cap">
            <span className="section-mark">§</span> 时间长河 · OBLIGATION RIVER · 12 MONTHS
          </span>
          <div className="flex items-center gap-4">
            <LegendDot color="var(--accent)" t="按期完成" />
            <LegendDot color="var(--gold)" t="进行中" />
            <LegendDot color="var(--rose)" t="逾期" />
          </div>
        </div>
        <River />
      </section>

      {/* === Subject grid === */}
      <section style={{ padding: '36px 0', borderBottom: '1px solid var(--rule)' }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
          <span className="t-meta-cap">
            <span className="section-mark">§</span> 主体 · SUBJECTS
          </span>
          <button className="btn btn-sm btn-ghost">查看主体库 →</button>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
          {COMPLIANCE_SUBJECTS.map((s, i) => (
            <SubjectTile key={i} s={s} idx={i} />
          ))}
        </div>
      </section>

      {/* === Tasks list === */}
      <section style={{ padding: '36px 0 0' }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
          <span className="t-meta-cap">
            <span className="section-mark">§</span> 高优待办 · TOP TASKS
          </span>
          <div className="flex gap-2">
            <button className="chip chip-accent" style={{ height: 24, fontSize: 11 }}>全部主体</button>
            <button className="chip chip-outline" style={{ height: 24, fontSize: 11 }}>本周到期</button>
            <button className="chip chip-outline" style={{ height: 24, fontSize: 11 }}>等我处理</button>
          </div>
        </div>

        <div style={{ borderTop: '1px solid var(--ink)' }}>
          {COMPLIANCE_TASKS.map((t, i) => (
            <article
              key={t.id}
              onClick={() => go('task')}
              style={{
                display: 'grid',
                gridTemplateColumns: '48px 1fr 200px 180px 120px',
                gap: 20,
                padding: '22px 8px',
                borderBottom: '1px solid var(--rule)',
                alignItems: 'center',
                cursor: 'pointer',
                transition: 'background 0.15s var(--ease)',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--paper-2)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            >
              <div className="num" style={{ fontSize: 28, fontStyle: 'italic', color: 'var(--muted-2)', lineHeight: 1 }}>
                {String(i + 1).padStart(2, '0')}
              </div>
              <div>
                <div className="flex items-center gap-2" style={{ marginBottom: 8 }}>
                  <span className={`chip ${t.priority === 'high' ? 'chip-rose' : t.priority === 'mid' ? 'chip-gold' : 'chip-outline'}`}>
                    {t.priority === 'high' ? '高优' : t.priority === 'mid' ? '中' : '低'}
                  </span>
                  <span className="chip chip-outline">{t.status}</span>
                  <span className="t-mono" style={{ color: 'var(--muted)' }}>{t.id}</span>
                </div>
                <div style={{
                  fontFamily: 'var(--serif-display)',
                  fontSize: 20,
                  fontStyle: 'italic',
                  fontWeight: 400,
                  letterSpacing: '-0.005em',
                  color: 'var(--ink)',
                  marginBottom: 4,
                  lineHeight: 1.2,
                }}>
                  {t.title}
                </div>
                <div className="t-mono" style={{ color: 'var(--muted)' }}>依据：{t.basis}</div>
              </div>
              <div>
                <div className="t-meta-cap" style={{ marginBottom: 4 }}>主体</div>
                <div style={{ fontFamily: 'var(--serif-body)', fontSize: 13, color: 'var(--ink-2)' }}>{t.subject}</div>
              </div>
              <div>
                <div className="t-meta-cap" style={{ marginBottom: 6 }}>进度 · 截止 {t.due}</div>
                <div style={{ height: 4, background: 'var(--paper-sunk)', borderRadius: 100, overflow: 'hidden', marginBottom: 6 }}>
                  <div style={{
                    width: `${t.progress * 100}%`,
                    height: '100%',
                    background: t.priority === 'high' ? 'var(--rose)' : 'var(--accent)',
                  }}></div>
                </div>
                <div className="t-mono" style={{ color: 'var(--muted)' }}>{Math.round(t.progress * 100)}%</div>
              </div>
              <div className="text-right">
                <div className="t-meta-cap" style={{ marginBottom: 4 }}>负责</div>
                <div style={{
                  fontFamily: 'var(--serif-body)',
                  fontSize: 13,
                  color: 'var(--ink-2)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 6,
                }}>
                  <span style={{
                    width: 22, height: 22, borderRadius: '50%',
                    background: 'var(--ink)', color: 'var(--paper-2)',
                    display: 'grid', placeItems: 'center',
                    fontFamily: 'var(--serif-display)', fontSize: 11, fontStyle: 'italic',
                  }}>{t.owner[0]}</span>
                  {t.owner}
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

function LegendDot({ color, t }) {
  return (
    <span className="t-mono" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--ink-2)' }}>
      <span style={{ width: 8, height: 8, background: color, transform: 'rotate(45deg)' }}></span>
      {t}
    </span>
  );
}

function RiskBreakdown() {
  const data = [
    { k: '低', n: 42, color: 'var(--moss)' },
    { k: '中', n: 26, color: 'var(--gold)' },
    { k: '高', n: 9,  color: 'var(--rose)' },
  ];
  const total = data.reduce((s, d) => s + d.n, 0);
  return (
    <div>
      <div style={{ display: 'flex', height: 8, background: 'var(--paper-sunk)', borderRadius: 100, overflow: 'hidden', marginBottom: 12 }}>
        {data.map((d, i) => (
          <div key={i} style={{ width: `${(d.n / total) * 100}%`, background: d.color }}></div>
        ))}
      </div>
      <div className="flex justify-between gap-3">
        {data.map((d, i) => (
          <div key={i} style={{ flex: 1 }}>
            <div className="flex items-center gap-1" style={{ marginBottom: 4 }}>
              <span style={{ width: 6, height: 6, background: d.color, transform: 'rotate(45deg)' }}></span>
              <span className="t-mono" style={{ color: 'var(--muted)' }}>{d.k}</span>
            </div>
            <span className="num" style={{ fontSize: 24, fontStyle: 'italic', lineHeight: 1 }}>{d.n}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function River() {
  // 12 months
  const months = ['6月', '7月', '8月', '9月', '10月', '11月', '12月', '1月', '2月', '3月', '4月', '5月'];
  const data = months.map((m, i) => ({
    m,
    done:     6 + Math.round(Math.abs(Math.sin(i * 1.7)) * 14 + (i / 4) * 2),
    inprog:   3 + Math.round(Math.abs(Math.cos(i * 1.2)) * 6),
    overdue:  i > 4 && i < 10 ? Math.round(Math.abs(Math.sin(i)) * 3 + 1) : 0,
  }));

  const W = 1100, H = 220;
  const padL = 32, padR = 24, padT = 30, padB = 36;
  const innerW = W - padL - padR;
  const innerH = H - padT - padB;
  const max = Math.max(...data.map(d => d.done + d.inprog + d.overdue)) + 4;
  const xFor = (i) => padL + (i + 0.5) * (innerW / data.length);
  const yFor = (v) => padT + innerH - (v / max) * innerH;
  const barW = (innerW / data.length) * 0.45;

  return (
    <svg viewBox={`0 0 ${W} ${H}`} style={{ width: '100%', display: 'block' }}>
      {/* Horizontal gridlines */}
      {[0, 0.25, 0.5, 0.75, 1].map((p, i) => (
        <g key={i}>
          <line x1={padL} x2={W - padR}
            y1={padT + innerH * (1 - p)} y2={padT + innerH * (1 - p)}
            stroke="var(--rule)" strokeWidth="0.5"
            strokeDasharray={p === 0 ? '' : '2 3'}
          />
          {p > 0 && (
            <text x={padL - 6} y={padT + innerH * (1 - p) + 3} textAnchor="end" fontSize="9" fontFamily="var(--mono)" fill="var(--muted)">
              {Math.round(max * p)}
            </text>
          )}
        </g>
      ))}

      {/* Bars */}
      {data.map((d, i) => {
        const x = xFor(i);
        const total = d.done + d.inprog + d.overdue;
        let y = padT + innerH;
        return (
          <g key={i}>
            {/* done */}
            <rect x={x - barW/2} y={yFor(d.done)} width={barW} height={padT + innerH - yFor(d.done)} fill="var(--accent)" opacity="0.9" />
            {/* inprog stack */}
            <rect x={x - barW/2} y={yFor(d.done + d.inprog)} width={barW} height={yFor(d.done) - yFor(d.done + d.inprog)} fill="var(--gold)" opacity="0.85" />
            {/* overdue */}
            {d.overdue > 0 && (
              <rect x={x - barW/2} y={yFor(d.done + d.inprog + d.overdue)} width={barW} height={yFor(d.done + d.inprog) - yFor(d.done + d.inprog + d.overdue)} fill="var(--rose)" opacity="0.85" />
            )}
            {/* total label */}
            <text x={x} y={yFor(total) - 6} textAnchor="middle" fontSize="9" fontFamily="var(--mono)" fill="var(--ink-3)">{total}</text>
            {/* month label */}
            <text x={x} y={H - 12} textAnchor="middle" fontSize="11" fontFamily="var(--sans)" fill="var(--ink-2)">{d.m}</text>
          </g>
        );
      })}

      {/* "Now" marker */}
      <g>
        <line x1={xFor(11)} x2={xFor(11)} y1={padT - 8} y2={padT + innerH} stroke="var(--ink)" strokeWidth="1" strokeDasharray="3 3" />
        <text x={xFor(11)} y={padT - 12} textAnchor="middle" fontSize="9" fontFamily="var(--mono)" fill="var(--ink)" letterSpacing="0.1em">NOW</text>
      </g>
    </svg>
  );
}

function SubjectTile({ s, idx }) {
  const sparkData = useMemoCo(() => {
    return Array.from({ length: 20 }, (_, i) =>
      Math.max(0, Math.min(100,
        s.score + Math.sin(i * 0.4 + idx) * 12 + (i - 10) * (s.risk === 'high' ? -1.2 : 0.4)
      ))
    );
  }, [s, idx]);
  const max = Math.max(...sparkData);
  const min = Math.min(...sparkData);
  const w = 220, h = 40;
  const xFor = i => (i / (sparkData.length - 1)) * w;
  const yFor = v => h - ((v - min) / (max - min || 1)) * h;
  const path = sparkData.map((v, i) => `${i ? 'L' : 'M'} ${xFor(i).toFixed(1)} ${yFor(v).toFixed(1)}`).join(' ');

  const riskColor = s.risk === 'high' ? 'var(--rose)' : s.risk === 'mid' ? 'var(--gold)' : 'var(--moss)';

  return (
    <div style={{
      padding: '18px 18px 16px',
      background: 'var(--paper-card)',
      border: '1px solid var(--rule)',
      borderRadius: 4,
      cursor: 'pointer',
      transition: 'all 0.15s var(--ease)',
      position: 'relative',
    }}
    onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--ink)'; }}
    onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--rule)'; }}
    >
      <div className="flex items-center justify-between" style={{ marginBottom: 14 }}>
        <span className="num" style={{ fontSize: 13, fontStyle: 'italic', color: 'var(--muted-2)' }}>
          № {String(idx + 1).padStart(2, '0')}
        </span>
        <span style={{ width: 8, height: 8, background: riskColor, transform: 'rotate(45deg)' }}></span>
      </div>

      <div style={{
        fontFamily: '"Noto Serif SC", serif',
        fontSize: 15, fontWeight: 500,
        color: 'var(--ink)',
        marginBottom: 14,
        lineHeight: 1.3,
        minHeight: 38,
      }}>
        {s.name}
      </div>

      <div className="flex items-baseline justify-between" style={{ marginBottom: 6 }}>
        <span className="num" style={{ fontSize: 32, fontStyle: 'italic', lineHeight: 1 }}>{s.score}</span>
        <span className="t-mono" style={{ color: 'var(--muted)' }}>合规指数</span>
      </div>

      {/* Sparkline */}
      <svg viewBox={`0 0 ${w} ${h}`} style={{ width: '100%', height: 36, display: 'block', marginTop: 8 }}>
        <path d={path} fill="none" stroke={riskColor} strokeWidth="1.2" />
        <circle cx={xFor(sparkData.length - 1)} cy={yFor(sparkData[sparkData.length - 1])} r="2" fill={riskColor} />
      </svg>

      <div className="flex justify-between" style={{ marginTop: 12, fontFamily: 'var(--mono)', fontSize: 10 }}>
        <span style={{ color: 'var(--ink-2)' }}>待办 {s.tasks.open}</span>
        {s.tasks.due > 0 ? (
          <span style={{ color: 'var(--rose)' }}>逾期 {s.tasks.due}</span>
        ) : (
          <span style={{ color: 'var(--moss)' }}>✓ 无逾期</span>
        )}
        <span style={{ color: 'var(--muted)' }}>完成 {s.tasks.done}</span>
      </div>
    </div>
  );
}

window.Compliance = Compliance;

})();

