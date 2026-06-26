// === src/pages/AIChat.jsx ===
(function(){
// AI 可溯源问答 — chat + citation lineage
const { useState: useStateA, useRef: useRefA, useEffect: useEffectA } = React;

function AIChat({ go }) {
  const { AI_MESSAGES } = window.XKU_DATA;
  const [activeCite, setActiveCite] = useStateA('DSL-21');
  const [input, setInput] = useStateA('');

  const cites = AI_MESSAGES[1].citations;

  return (
    <div className="page-enter" style={{
      display: 'grid',
      gridTemplateColumns: '220px minmax(0, 1fr) 340px',
      height: '100%',
    }}>

      {/* ============ LEFT: sessions ============ */}
      <aside style={{
        borderRight: '1px solid var(--rule)',
        padding: '28px 18px',
        overflow: 'auto',
        background: 'var(--paper-2)',
      }}>
        <div className="flex items-center justify-between" style={{ marginBottom: 14 }}>
          <span className="t-meta-cap">会话 · SESSIONS</span>
          <button className="btn btn-sm btn-primary" title="新建" style={{ width: 24, height: 24, padding: 0, justifyContent: 'center', borderRadius: 4 }}>
            <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M6 2 V10 M2 6 H10"/></svg>
          </button>
        </div>
        <div style={{ borderTop: '1px solid var(--rule)' }}>
          {[
            { t: '订单数据 → 香港 出境合规',         when: '今天 10:42', active: true, citations: 6 },
            { t: '关键信息基础设施 · 认定标准',       when: '昨天 16:08', citations: 4 },
            { t: '个人信息出境 · 标准合同 v2 续签',   when: '5·22',      citations: 3 },
            { t: '生成式 AI 训练数据合规',           when: '5·19',      citations: 8 },
            { t: '上海地方数据条例 vs 国家法',         when: '5·15',      citations: 5 },
          ].map((s, i) => (
            <div key={i} style={{
              padding: '12px 10px',
              borderBottom: '1px solid var(--rule)',
              cursor: 'pointer',
              background: s.active ? 'var(--paper-card)' : 'transparent',
              borderLeft: s.active ? '2px solid var(--accent)' : '2px solid transparent',
              marginLeft: -10,
              paddingLeft: 12,
            }}>
              <div style={{
                fontFamily: 'var(--serif-body)',
                fontSize: 13,
                lineHeight: 1.4,
                color: s.active ? 'var(--ink)' : 'var(--ink-2)',
                fontWeight: s.active ? 500 : 400,
                marginBottom: 4,
              }}>{s.t}</div>
              <div className="flex justify-between" style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--muted)' }}>
                <span>{s.when}</span>
                <span>{s.citations} 引用</span>
              </div>
            </div>
          ))}
        </div>
      </aside>

      {/* ============ CENTER: chat ============ */}
      <main style={{
        overflow: 'auto',
        padding: '40px 56px 24px',
        position: 'relative',
        background: 'var(--paper)',
      }}>
        {/* opinion-style title */}
        <div style={{ marginBottom: 24 }}>
          <div className="t-meta-cap" style={{ marginBottom: 10 }}>
            <span className="section-mark">§</span> 法律意见 · LEGAL OPINION
          </div>
          <h2 style={{
            fontFamily: 'var(--serif-display)',
            fontSize: 36,
            lineHeight: 1.1,
            margin: 0,
            fontWeight: 400,
            letterSpacing: '-0.01em',
          }}>
            <em>关于</em> 跨境电商订单数据出境的合规义务<em>。</em>
          </h2>
          <div className="t-mono" style={{ marginTop: 10, color: 'var(--muted)' }}>
            X-KU Copilot · 基于 6 条法源 · 92% 置信
          </div>
        </div>

        {/* User question */}
        <div style={{ marginBottom: 36, paddingLeft: 32, position: 'relative' }}>
          <span style={{
            position: 'absolute', left: 0, top: 2,
            fontFamily: 'var(--serif-display)', fontStyle: 'italic',
            fontSize: 28, color: 'var(--muted-2)', lineHeight: 1,
          }}>"</span>
          <p style={{
            fontFamily: '"Noto Serif SC", serif',
            fontSize: 17, lineHeight: 1.7, color: 'var(--ink-2)',
            margin: 0,
          }}>
            {AI_MESSAGES[0].text}
          </p>
          <div className="t-mono" style={{ marginTop: 8, color: 'var(--muted)' }}>
            李书航 · {AI_MESSAGES[0].when}
          </div>
        </div>

        {/* Assistant response — opinion-style */}
        <article style={{ position: 'relative' }}>
          {/* TL;DR */}
          <div style={{
            padding: '22px 24px',
            background: 'var(--paper-2)',
            borderLeft: '3px solid var(--accent)',
            marginBottom: 32,
            position: 'relative',
          }}>
            <div className="t-meta-cap" style={{ color: 'var(--accent)', marginBottom: 8 }}>
              <Diamond size={6} /> 要点摘述 · TL;DR
            </div>
            <p style={{
              fontFamily: '"Noto Serif SC", serif',
              fontSize: 16, lineHeight: 1.75, color: 'var(--ink-2)',
              margin: 0,
            }}>
              {AI_MESSAGES[1].blocks[0].text}
            </p>
          </div>

          {/* Numbered steps with citations */}
          <ol style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {AI_MESSAGES[1].blocks.filter(b => b.kind === 'step').map((step, i) => (
              <li key={i} style={{
                marginBottom: 32,
                paddingLeft: 64,
                position: 'relative',
              }}>
                {/* Big italic number */}
                <span style={{
                  position: 'absolute', left: 0, top: -8,
                  fontFamily: 'var(--serif-display)', fontStyle: 'italic',
                  fontSize: 44, color: 'var(--accent)', lineHeight: 1,
                }} className="num">
                  {String(step.n).padStart(2, '0')}
                </span>
                <h3 style={{
                  fontFamily: '"Noto Serif SC", serif',
                  fontSize: 19, lineHeight: 1.35, fontWeight: 500,
                  margin: '0 0 10px',
                }}>
                  {step.title}
                </h3>
                <p style={{
                  fontFamily: '"Noto Serif SC", serif',
                  fontSize: 16, lineHeight: 1.8,
                  color: 'var(--ink-2)',
                  margin: 0,
                }}>
                  {step.body}
                  {step.citeIds.map((cid, j) => {
                    const c = cites.find(x => x.id === cid);
                    const idx = cites.findIndex(x => x.id === cid) + 1;
                    return (
                      <sup
                        key={cid}
                        onClick={() => setActiveCite(cid)}
                        title={c ? `${c.source} · ${c.article}` : ''}
                        style={{
                          fontFamily: 'var(--mono)',
                          fontSize: 10,
                          color: activeCite === cid ? 'var(--accent)' : 'var(--ink-2)',
                          background: activeCite === cid ? 'var(--accent-glow)' : 'var(--paper-sunk)',
                          padding: '1px 5px',
                          borderRadius: 100,
                          marginLeft: 3,
                          cursor: 'pointer',
                          verticalAlign: 'super',
                          fontWeight: 500,
                          transition: 'all 0.15s var(--ease)',
                        }}
                      >{idx}</sup>
                    );
                  })}
                </p>
              </li>
            ))}
          </ol>

          {/* Closing footer */}
          <div className="hairline" style={{ margin: '40px 0 20px' }}></div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="t-meta-cap">引用 · CITATIONS</span>
              <div className="flex items-center gap-1">
                {cites.map((c, i) => (
                  <button
                    key={c.id}
                    onClick={() => setActiveCite(c.id)}
                    style={{
                      width: 22, height: 22, borderRadius: '50%',
                      border: activeCite === c.id ? '1.5px solid var(--accent)' : '1px solid var(--rule-strong)',
                      background: activeCite === c.id ? 'var(--accent)' : 'var(--paper-card)',
                      color: activeCite === c.id ? 'white' : 'var(--ink-2)',
                      fontFamily: 'var(--mono)', fontSize: 10,
                      cursor: 'pointer',
                      transition: 'all 0.15s var(--ease)',
                    }}
                  >{i + 1}</button>
                ))}
              </div>
            </div>
            <div className="flex items-center gap-1">
              <button className="btn btn-sm btn-ghost" title="复制">
                <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.4"><rect x="2" y="2" width="6" height="6" /><rect x="4" y="4" width="6" height="6" /></svg>
              </button>
              <button className="btn btn-sm btn-ghost" title="点赞">
                <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.4"><path d="M4 5 L4 10 H8 L10 6 V5 H6 L7 2 L5 2 Z"/></svg>
              </button>
              <button className="btn btn-sm btn-ghost" title="纠错">
                <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.4"><path d="M6 2 L10 10 H2 Z M6 5 V7 M6 8.5 V9"/></svg>
              </button>
            </div>
          </div>
        </article>

        {/* Follow-up suggestions */}
        <section style={{ marginTop: 30 }}>
          <div className="t-meta-cap" style={{ marginBottom: 12 }}>追问 · FOLLOW-UP</div>
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            {[
              '哪些类别属于"重要数据"？请举例。',
              '如果走标准合同路径，需要提交什么材料？',
              '安全评估的审查时间一般是多长？',
              '不同省份的网信办对受理材料有差异吗？',
            ].map((q, i) => (
              <button key={i} className="btn btn-sm" style={{
                fontFamily: 'var(--serif-body)',
                fontStyle: 'italic',
                fontSize: 13,
                color: 'var(--ink-2)',
                background: 'var(--paper-2)',
                borderColor: 'var(--rule)',
              }}>
                <Diamond size={5} color="var(--accent)" />
                {q}
              </button>
            ))}
          </div>
        </section>

        {/* Input box */}
        <div style={{
          marginTop: 40,
          marginBottom: 20,
          padding: '14px 16px',
          border: '1px solid var(--rule-strong)',
          borderRadius: 6,
          background: 'var(--paper-card)',
          display: 'flex',
          alignItems: 'flex-start',
          gap: 12,
        }}>
          <Diamond size={8} />
          <textarea
            value={input}
            onChange={e => setInput(e.target.value)}
            placeholder="继续提问…  /  ⌘ + Enter 发送  ·  / 切换提示词模板"
            style={{
              flex: 1, border: 0, outline: 0, background: 'transparent',
              fontFamily: 'var(--serif-body)', fontSize: 15,
              color: 'var(--ink)', resize: 'none', minHeight: 24,
              maxHeight: 120,
            }}
          />
          <div className="flex items-center gap-2">
            <button className="btn btn-sm">附件</button>
            <button className="btn btn-sm btn-primary">
              发送 <span className="kbd" style={{ background: 'rgba(255,255,255,0.15)', color: 'rgba(255,255,255,0.7)' }}>⌘↵</span>
            </button>
          </div>
        </div>
      </main>

      {/* ============ RIGHT: evidence ledger ============ */}
      <aside style={{
        borderLeft: '1px solid var(--rule)',
        background: 'var(--paper-2)',
        overflow: 'auto',
        padding: '32px 24px',
      }}>
        <div className="flex justify-between items-baseline" style={{ marginBottom: 18 }}>
          <span className="t-meta-cap">
            <span className="section-mark">§</span> 证据账册 · EVIDENCE
          </span>
          <span className="t-mono" style={{ color: 'var(--muted)' }}>{cites.length} 条法源</span>
        </div>

        <CitationCards cites={cites} activeCite={activeCite} onSelect={setActiveCite} go={go} />

        {/* Confidence widget */}
        <div style={{
          marginTop: 28,
          padding: '16px 16px',
          background: 'var(--ink)',
          color: 'var(--paper-2)',
          borderRadius: 4,
          position: 'relative',
          overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, top: -30, opacity: 0.15 }}>
            <CubeOrnament size={130} />
          </div>
          <div className="t-meta-cap" style={{ color: 'rgba(255,255,255,0.55)', marginBottom: 8 }}>
            综合置信
          </div>
          <div className="flex items-baseline gap-2">
            <span className="num" style={{ fontSize: 44, fontStyle: 'italic', color: 'var(--accent)' }}>92</span>
            <span className="t-mono" style={{ color: 'rgba(255,255,255,0.6)' }}>%</span>
          </div>
          <div style={{
            fontFamily: 'var(--serif-body)',
            fontStyle: 'italic',
            fontSize: 12,
            color: 'rgba(255,255,255,0.7)',
            marginTop: 8,
            lineHeight: 1.5,
          }}>
            所有论断均回溯到上方 6 条原文，
            <br />
            请仍以官方公告与机构判断为准。
          </div>
        </div>
      </aside>
    </div>
  );
}

function CitationCards({ cites, activeCite, onSelect, go }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {cites.map((c, i) => {
        const isActive = activeCite === c.id;
        return (
          <div
            key={c.id}
            onClick={() => onSelect(c.id)}
            style={{
              padding: isActive ? '16px 16px 14px' : '12px 14px',
              background: isActive ? 'var(--paper-card)' : 'transparent',
              border: isActive ? '1px solid var(--accent)' : '1px solid var(--rule)',
              borderRadius: 4,
              cursor: 'pointer',
              transition: 'all 0.2s var(--ease)',
              position: 'relative',
            }}
          >
            <div className="flex items-center gap-2" style={{ marginBottom: 6 }}>
              <span style={{
                width: 18, height: 18, borderRadius: '50%',
                background: isActive ? 'var(--accent)' : 'var(--paper-sunk)',
                color: isActive ? 'white' : 'var(--ink-2)',
                fontFamily: 'var(--mono)', fontSize: 10,
                display: 'grid', placeItems: 'center',
              }}>{i + 1}</span>
              <span style={{
                fontFamily: 'var(--serif-body)',
                fontSize: 13, fontWeight: 500,
                color: 'var(--ink)',
                flex: 1,
                whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
              }}>
                {c.source}
              </span>
              <span className="t-mono" style={{ color: 'var(--accent)' }}>{(c.confidence * 100).toFixed(0)}%</span>
            </div>
            <div className="t-mono" style={{ marginBottom: isActive ? 10 : 0, color: 'var(--muted)' }}>{c.article}</div>
            {isActive && (
              <>
                <p style={{
                  fontFamily: '"Noto Serif SC", serif',
                  fontSize: 13, lineHeight: 1.6,
                  color: 'var(--ink-2)',
                  margin: '8px 0 12px',
                  paddingLeft: 10,
                  borderLeft: '2px solid var(--accent-glow)',
                  letterSpacing: '0.005em',
                }}>
                  {c.excerpt}
                </p>
                <div className="flex gap-2">
                  <button className="btn btn-sm" onClick={() => go('law')} style={{ flex: 1, justifyContent: 'center' }}>
                    跳转正文 →
                  </button>
                  <button className="btn btn-sm" title="批注">
                    <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.4"><path d="M9 2 L10 3 L4 9 L2 10 L3 8 Z"/></svg>
                  </button>
                </div>
              </>
            )}
          </div>
        );
      })}
    </div>
  );
}

window.AIChat = AIChat;

})();

