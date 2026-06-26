// === src/pages/Home.jsx ===
(function(){
// Home / Workspace — editorial landing
const { useState: useStateH, useEffect: useEffectH } = React;

function Home({ go }) {
  const { TODAY_HIGHLIGHTS, RECENT_ALERTS, COMPLIANCE_SUBJECTS } = window.XKU_DATA;
  const [q, setQ] = useStateH('');
  const today = new Date();
  const dateStr = `${today.getFullYear()}.${String(today.getMonth() + 1).padStart(2, '0')}.${String(today.getDate()).padStart(2, '0')}`;
  const weekday = ['日','一','二','三','四','五','六'][today.getDay()];

  return (
    <div className="page-enter" style={{ padding: '32px 48px 48px', maxWidth: 1440, margin: '0 auto' }}>

      {/* === Editorial masthead === */}
      <header className="flex justify-between items-start" style={{ marginBottom: 30 }}>
        <div>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>
            X · KU REGULATIONS &nbsp; ·&nbsp; 工作台 · WORKSPACE
          </div>
          <div style={{
            fontFamily: 'var(--serif-display)',
            fontSize: 72,
            lineHeight: 0.92,
            letterSpacing: '-0.02em',
            color: 'var(--ink)',
            fontWeight: 400,
          }}>
            <em>今日</em>，
            <span style={{ color: 'var(--accent)' }} className="num">7</span> 项立法动向，
            <br />
            <span className="num" style={{ color: 'var(--rose)' }}>3</span> 条预警等待裁断。
          </div>
        </div>
        <aside style={{ textAlign: 'right', paddingTop: 6 }}>
          <div className="t-mono" style={{ fontSize: 12, color: 'var(--ink-2)', letterSpacing: '0.06em' }}>
            {dateStr} · 星期{weekday}
          </div>
          <div className="t-meta" style={{ fontSize: 10, marginTop: 4 }}>
            Issue №{Math.floor((today - new Date('2026-01-01')) / 86400000)}
          </div>
          <div style={{ marginTop: 16, marginRight: -8 }}>
            <CubeOrnament size={120} />
          </div>
        </aside>
      </header>

      <div className="hairline-strong" style={{ borderTopColor: 'var(--ink)' }}></div>

      {/* === Today's highlight strip === */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: 0,
        margin: '0',
        borderBottom: '1px solid var(--rule)',
      }}>
        {TODAY_HIGHLIGHTS.map((h, i) => (
          <div key={i} style={{
            padding: '20px 24px',
            borderRight: i < 3 ? '1px solid var(--rule)' : 'none',
            cursor: 'pointer',
          }}>
            <div className="t-meta-cap" style={{ marginBottom: 12, color: 'var(--muted)' }}>{h.kind}</div>
            <div className="flex items-baseline gap-2">
              <span className="num" style={{ fontSize: 44, lineHeight: 1, fontStyle: 'italic', color: 'var(--ink)' }}>{h.n}</span>
              {h.delta && (
                <span className="t-mono" style={{ fontSize: 11, color: h.delta.startsWith('+') ? 'var(--moss)' : 'var(--rose)' }}>
                  {h.delta}
                </span>
              )}
            </div>
            <div className="t-label" style={{ marginTop: 6, color: 'var(--ink-2)' }}>{h.k}</div>
          </div>
        ))}
      </div>

      {/* === Hero search + spotlight === */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: '7fr 5fr',
        gap: 36,
        margin: '40px 0 50px',
        alignItems: 'start',
      }}>
        {/* Left: search */}
        <div>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>
            <span className="section-mark">§</span> 01 &nbsp;探询
          </div>
          <h2 className="t-display" style={{ fontSize: 36, marginBottom: 18, fontStyle: 'italic' }}>
            Ask the corpus.
          </h2>
          <div style={{
            border: '1px solid var(--rule-strong)',
            borderRadius: 4,
            background: 'var(--paper-2)',
            padding: 0,
            display: 'flex',
            alignItems: 'stretch',
            transition: 'all 0.2s var(--ease)',
            boxShadow: q ? '0 0 0 3px var(--accent-soft)' : 'none',
            borderColor: q ? 'var(--accent)' : 'var(--rule-strong)',
          }}>
            <input
              value={q}
              onChange={e => setQ(e.target.value)}
              placeholder="跨境电商订单数据传到香港，2026年需要哪些合规动作?"
              style={{
                flex: 1,
                border: 0,
                outline: 0,
                background: 'transparent',
                padding: '20px 22px',
                fontFamily: 'var(--serif-body)',
                fontSize: 17,
                color: 'var(--ink)',
                lineHeight: 1.5,
              }}
              onKeyDown={e => { if (e.key === 'Enter') go('ai'); }}
            />
            <button
              onClick={() => go('ai')}
              style={{
                width: 56, border: 0,
                background: 'var(--ink)',
                color: 'var(--paper-2)',
                cursor: 'pointer',
                display: 'grid', placeItems: 'center',
                borderRadius: '0 4px 4px 0',
              }}
              title="问 X-KU 助手"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M4 10 H16 M11 5 L16 10 L11 15"/>
              </svg>
            </button>
          </div>

          {/* Suggested queries */}
          <div className="flex gap-2" style={{ marginTop: 14, flexWrap: 'wrap' }}>
            {[
              { t: '比较 数据安全法 v1 / v2 第三十一条', r: 'compare' },
              { t: '《数据安全法》第二十一条全文', r: 'law' },
              { t: '关键信息基础设施 · 出境义务清单', r: 'search' },
            ].map((s, i) => (
              <button
                key={i}
                className="btn btn-sm"
                onClick={() => go(s.r)}
                style={{
                  background: 'var(--paper-2)',
                  fontFamily: 'var(--serif-body)',
                  fontStyle: 'italic',
                  fontSize: 12,
                  color: 'var(--ink-2)',
                  borderColor: 'var(--rule)',
                }}
              >
                <Diamond size={6} color="var(--muted-2)" />
                <span>{s.t}</span>
              </button>
            ))}
          </div>

          {/* Three modes */}
          <div style={{ marginTop: 32, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 0, border: '1px solid var(--rule)', borderRadius: 4 }}>
            {[
              { k: '01', mode: '关键词检索', sub: 'KEYWORD', desc: '同义词、地区、效力层级', r: 'search' },
              { k: '02', mode: 'AI 可溯源问答', sub: 'COPILOT', desc: '自然语言 · 条款级引用', r: 'ai' },
              { k: '03', mode: '版本对比', sub: 'COMPARE', desc: '两版本条款级差异', r: 'compare' },
            ].map((m, i) => (
              <button key={i} onClick={() => go(m.r)} style={{
                padding: '18px 18px 16px',
                borderLeft: i ? '1px solid var(--rule)' : 0,
                background: 'transparent', cursor: 'pointer', textAlign: 'left',
                fontFamily: 'inherit', color: 'inherit',
                transition: 'background 0.15s var(--ease)',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--paper-2)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
              >
                <div className="flex items-center gap-2 t-meta-cap" style={{ marginBottom: 10 }}>
                  <span className="num" style={{ color: 'var(--accent)', fontStyle: 'italic' }}>{m.k}</span>
                  <span>{m.sub}</span>
                </div>
                <div className="t-h3" style={{ marginBottom: 4 }}>{m.mode}</div>
                <div className="t-mono" style={{ color: 'var(--muted)' }}>{m.desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Right: spotlight */}
        <div style={{
          background: 'var(--ink)',
          color: 'var(--paper-2)',
          padding: '30px 30px 28px',
          borderRadius: 4,
          position: 'relative',
          overflow: 'hidden',
          minHeight: 380,
        }}>
          {/* decorative cube */}
          <div style={{ position: 'absolute', right: -60, top: -50, opacity: 0.18, pointerEvents: 'none' }}>
            <CubeOrnament size={260} />
          </div>
          <div className="flex items-center justify-between" style={{ marginBottom: 18 }}>
            <span className="t-meta-cap" style={{ color: 'rgba(255,255,255,0.6)' }}>
              <span className="section-mark" style={{ color: 'var(--accent)' }}>§</span> SPOTLIGHT · 头条
            </span>
            <span className="t-mono" style={{ color: 'rgba(255,255,255,0.5)', fontSize: 10 }}>05·24 · 09:12</span>
          </div>

          <div style={{ position: 'relative', zIndex: 1 }}>
            <div className="chip chip-accent" style={{ marginBottom: 16 }}>
              <Diamond size={6} color="var(--accent)" /> 司法解释 · 征求意见
            </div>
            <h3 className="t-display" style={{ fontSize: 34, color: 'var(--paper-2)', lineHeight: 1.05, marginBottom: 18 }}>
              <em>《数据安全法》</em>
              <br />
              司法解释（二）<br />
              <span style={{ color: 'var(--accent)', fontStyle: 'italic' }}>公开征求意见</span>。
            </h3>
            <p className="t-body" style={{ color: 'rgba(255,255,255,0.72)', fontSize: 14, marginBottom: 22 }}>
              本次解释聚焦数据出境、重要数据目录、跨境业务连续性三类争议，
              对照你的订阅，<span style={{ color: 'var(--accent)' }}>命中 3 个主体、7 项清单</span>。
            </p>
            <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
              <button className="btn btn-accent" onClick={() => go('law')}>
                <span>查看正文</span>
                <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M3 6 H9 M7 4 L9 6 L7 8"/></svg>
              </button>
              <button className="btn" onClick={() => go('compare')} style={{
                background: 'transparent', color: 'var(--paper-2)',
                borderColor: 'rgba(255,255,255,0.25)',
              }}>对比 v1 / v2</button>
            </div>

            {/* footnote */}
            <div style={{ position: 'absolute', bottom: 0, right: 0, fontFamily: 'var(--serif-display)', fontStyle: 'italic', fontSize: 12, color: 'rgba(255,255,255,0.4)' }}>
              — 第 {Math.floor(Math.random() * 30) + 80} 期 · 头条
            </div>
          </div>
        </div>
      </section>

      {/* === Three columns: alerts · compliance pulse · AI hub === */}
      <section style={{
        display: 'grid',
        gridTemplateColumns: '5fr 4fr 3fr',
        gap: 32,
        marginBottom: 56,
      }}>

        {/* Alerts (editorial list) */}
        <div>
          <div className="flex justify-between items-baseline" style={{ marginBottom: 14 }}>
            <div className="t-meta-cap">
              <span className="section-mark">§</span> 02 &nbsp;预警 · ALERTS
            </div>
            <button className="btn-ghost btn btn-sm">查看全部 →</button>
          </div>
          <div style={{ borderTop: '1px solid var(--ink)' }}>
            {RECENT_ALERTS.map((a, i) => (
              <article key={a.id} style={{
                padding: '20px 0 22px',
                borderBottom: '1px solid var(--rule)',
                display: 'grid',
                gridTemplateColumns: '46px 1fr auto',
                gap: 16,
                cursor: 'pointer',
              }}
              onClick={() => go('law')}
              >
                <div style={{ textAlign: 'right' }}>
                  <div className="num" style={{ fontSize: 28, fontStyle: 'italic', lineHeight: 1, color: 'var(--muted-2)' }}>
                    {String(i + 1).padStart(2, '0')}
                  </div>
                </div>
                <div>
                  <div className="flex items-center gap-2" style={{ marginBottom: 8 }}>
                    <span className={`chip ${a.severity === 'high' ? 'chip-rose' : a.severity === 'mid' ? 'chip-gold' : 'chip-outline'}`}>
                      {a.severity === 'high' ? '高优' : a.severity === 'mid' ? '中等' : '低'}
                    </span>
                    <span className="t-mono">{a.when}</span>
                    <span className="t-mono" style={{ color: 'var(--muted-2)' }}>·</span>
                    <span className="t-mono">命中 {a.matched}</span>
                  </div>
                  <h4 className="t-h3" style={{ marginBottom: 6, fontFamily: 'var(--serif-display)', fontSize: 19, fontWeight: 400, letterSpacing: '-0.005em' }}>
                    {a.title}
                  </h4>
                  <p className="t-body" style={{ fontSize: 14, color: 'var(--ink-3)', margin: 0 }}>
                    {a.body}
                  </p>
                </div>
                <div style={{ alignSelf: 'center', color: 'var(--muted-2)' }}>
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4"><path d="M5 3 L9 7 L5 11"/></svg>
                </div>
              </article>
            ))}
          </div>
        </div>

        {/* Compliance pulse */}
        <div>
          <div className="flex justify-between items-baseline" style={{ marginBottom: 14 }}>
            <div className="t-meta-cap">
              <span className="section-mark">§</span> 03 &nbsp;合规脉搏 · PULSE
            </div>
            <button className="btn-ghost btn btn-sm" onClick={() => go('compliance')}>工作台 →</button>
          </div>
          <div className="card" style={{ padding: '22px 22px 20px' }}>
            {/* overall score */}
            <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
              <div>
                <div className="t-meta-cap" style={{ marginBottom: 6 }}>整体合规指数</div>
                <div className="flex items-baseline gap-2">
                  <span className="num" style={{ fontSize: 56, fontStyle: 'italic', lineHeight: 1 }}>78</span>
                  <span className="t-mono" style={{ color: 'var(--moss)' }}>▲ 4 · 7d</span>
                </div>
              </div>
              <div style={{ width: 70, height: 70 }}>
                <svg viewBox="0 0 70 70">
                  <circle cx="35" cy="35" r="28" fill="none" stroke="var(--rule)" strokeWidth="3"/>
                  <circle cx="35" cy="35" r="28" fill="none" stroke="var(--accent)" strokeWidth="3"
                    strokeDasharray={`${0.78 * 2 * Math.PI * 28} ${2 * Math.PI * 28}`}
                    strokeDashoffset={2 * Math.PI * 28 * 0.25}
                    transform="rotate(-90 35 35)"
                    strokeLinecap="round"
                  />
                  <text x="35" y="38" textAnchor="middle" fontSize="11" fill="var(--ink-2)" fontFamily="var(--mono)">78%</text>
                </svg>
              </div>
            </div>
            <div className="hairline"></div>
            <div style={{ marginTop: 14 }}>
              {COMPLIANCE_SUBJECTS.slice(0, 4).map((s, i) => (
                <div key={i} className="flex items-center justify-between" style={{ padding: '8px 0', borderBottom: i < 3 ? '1px solid var(--rule)' : 'none' }}>
                  <div className="flex items-center gap-2">
                    <Diamond size={6} color={s.risk === 'high' ? 'var(--rose)' : s.risk === 'mid' ? 'var(--gold)' : 'var(--moss)'} />
                    <span style={{ fontSize: 12, color: 'var(--ink-2)' }}>{s.name}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    {s.tasks.due > 0 && <span className="t-mono" style={{ color: 'var(--rose)' }}>{s.tasks.due} 逾期</span>}
                    <span className="num" style={{ fontSize: 14, fontStyle: 'italic' }}>{s.score}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* AI / Copilot */}
        <div>
          <div className="flex justify-between items-baseline" style={{ marginBottom: 14 }}>
            <div className="t-meta-cap">
              <span className="section-mark">§</span> 04 &nbsp;COPILOT
            </div>
          </div>
          <div style={{
            background: 'var(--paper-2)',
            border: '1px solid var(--rule)',
            borderRadius: 4,
            padding: '20px 18px',
            position: 'relative',
            overflow: 'hidden',
          }}>
            <div style={{ position: 'absolute', right: -30, top: -30, opacity: 0.18 }}>
              <CubeOrnament size={140} spin />
            </div>
            <div className="t-meta-cap" style={{ marginBottom: 14, color: 'var(--accent)' }}>
              X-KU ASSISTANT · 在线
            </div>
            <p style={{
              fontFamily: 'var(--serif-display)',
              fontStyle: 'italic',
              fontSize: 20,
              lineHeight: 1.3,
              color: 'var(--ink)',
              marginBottom: 18,
              position: 'relative', zIndex: 1,
            }}>
              "我会把我的回答<br/>逐句对齐到<br/>原始条款上。"
            </p>
            <div className="t-mono" style={{ color: 'var(--muted)', marginBottom: 14 }}>
              本周 142 次问答 · 92% 引用准确
            </div>
            <button className="btn btn-primary" onClick={() => go('ai')} style={{ width: '100%', justifyContent: 'center' }}>
              开启对话 →
            </button>
          </div>
        </div>
      </section>

      {/* === Footer ribbon: hot topics === */}
      <section style={{ borderTop: '1px solid var(--ink)', paddingTop: 22 }}>
        <div className="flex justify-between items-baseline" style={{ marginBottom: 16 }}>
          <div className="t-meta-cap">
            <span className="section-mark">§</span> 05 &nbsp;热点专题 · TOPICS
          </div>
          <div className="t-mono" style={{ color: 'var(--muted)' }}>由运营整理 · 周一刷新</div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 0, borderLeft: '1px solid var(--rule)' }}>
          {[
            { num: '01', cn: '数据出境', en: 'Cross-border Data', count: 24 },
            { num: '02', cn: '生成式 AI', en: 'Generative AI', count: 18 },
            { num: '03', cn: '关键信息基础设施', en: 'CII Operators', count: 31 },
            { num: '04', cn: '个人信息处理者', en: 'PI Processors', count: 47 },
          ].map((t, i) => (
            <div key={i} style={{
              padding: '24px 22px 22px',
              borderRight: '1px solid var(--rule)',
              cursor: 'pointer',
              background: 'transparent',
              transition: 'background 0.15s var(--ease)',
            }}
            onMouseEnter={e => e.currentTarget.style.background = 'var(--paper-2)'}
            onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            >
              <div className="flex justify-between items-baseline" style={{ marginBottom: 16 }}>
                <span className="num" style={{ fontStyle: 'italic', fontSize: 22, color: 'var(--accent)' }}>{t.num}</span>
                <span className="t-mono">{t.count} 篇</span>
              </div>
              <div style={{ fontFamily: 'var(--serif-display)', fontSize: 22, color: 'var(--ink)', marginBottom: 4 }}>{t.cn}</div>
              <div className="t-meta" style={{ fontSize: 10 }}>{t.en}</div>
            </div>
          ))}
        </div>
      </section>

    </div>
  );
}

window.Home = Home;

})();

