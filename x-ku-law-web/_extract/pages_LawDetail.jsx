// === src/pages/LawDetail.jsx ===
(function(){
// Law detail — editorial reading experience
const { useState: useStateL, useRef: useRefL, useEffect: useEffectL } = React;

function LawDetail({ go }) {
  const { MOCK_LAW, ARTICLES, TIMELINE } = window.XKU_DATA;
  const [version, setVersion] = useStateL('v2');
  const [activeArticleIdx, setActiveArticleIdx] = useStateL(2); // 第二十一条
  const [showAIPanel, setShowAIPanel] = useStateL(false);

  return (
    <div className="page-enter" style={{ paddingBottom: 80 }}>
      {/* === Editorial meta bar === */}
      <header style={{
        padding: '28px 48px 24px',
        borderBottom: '1px solid var(--rule)',
        background: 'var(--paper-2)',
      }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 14 }}>
          <div className="t-meta-cap">
            <span className="section-mark">§</span> {MOCK_LAW.level} · NATIONAL LAW · ZH-CN
          </div>
          <div className="flex items-center gap-3">
            <button className="btn btn-sm">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M2 6 H10 M2 6 L4 4 M2 6 L4 8 M10 6 L8 4 M10 6 L8 8"/></svg>
              对比版本
            </button>
            <button className="btn btn-sm">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M3 2 H8 L10 4 V10 H3 Z M5 6 H8 M5 8 H8"/></svg>
              收藏
            </button>
            <button className="btn btn-sm">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M2 8 V10 H10 V8 M6 2 V8 M3 5 L6 2 L9 5"/></svg>
              导出
            </button>
            <button className="btn btn-sm btn-primary" onClick={() => setShowAIPanel(!showAIPanel)}>
              <Diamond size={6} color="white" />
              问 AI
            </button>
          </div>
        </div>

        <div className="flex items-end justify-between gap-8" style={{ flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 600px' }}>
            <h1 style={{
              fontFamily: '"Noto Serif SC", serif',
              fontSize: 44,
              lineHeight: 1.08,
              fontWeight: 500,
              letterSpacing: '-0.005em',
              margin: '8px 0 8px',
              color: 'var(--ink)',
            }}>
              {MOCK_LAW.title}
            </h1>
            <div style={{
              fontFamily: 'var(--serif-display)',
              fontStyle: 'italic',
              fontSize: 17,
              color: 'var(--muted)',
              marginBottom: 14,
            }}>
              {MOCK_LAW.titleEn}
            </div>
            <div className="flex items-center gap-2" style={{ flexWrap: 'wrap' }}>
              <span className="chip chip-accent">{MOCK_LAW.level}</span>
              <span className="chip chip-moss">{MOCK_LAW.status}</span>
              <span className="chip chip-outline">{MOCK_LAW.region}</span>
              {MOCK_LAW.category.map(c => (
                <span key={c} className="chip chip-outline">{c}</span>
              ))}
            </div>
            <div className="t-mono" style={{ marginTop: 14, color: 'var(--ink-2)' }}>
              {MOCK_LAW.docNo} &nbsp;·&nbsp; 发文 {MOCK_LAW.issuingBody}
            </div>
          </div>

          {/* Stats column */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, auto)',
            gap: 28,
            padding: '8px 0',
          }}>
            {[
              { k: '条款', v: MOCK_LAW.articlesTotal },
              { k: '版本', v: MOCK_LAW.versions },
              { k: '被引', v: MOCK_LAW.citedBy },
              { k: '关联', v: MOCK_LAW.relatedRegulations },
            ].map((s, i) => (
              <div key={i} style={{ textAlign: 'right' }}>
                <div className="num" style={{ fontSize: 28, fontStyle: 'italic', lineHeight: 1, color: 'var(--ink)' }}>{s.v}</div>
                <div className="t-meta-cap" style={{ marginTop: 4 }}>{s.k}</div>
              </div>
            ))}
          </div>
        </div>
      </header>

      {/* === Version Lineage Timeline === */}
      <div style={{
        padding: '0 48px',
        borderBottom: '1px solid var(--rule)',
        background: 'var(--paper)',
      }}>
        <VersionLineage timeline={TIMELINE} current={version} onSelect={setVersion} />
      </div>

      {/* === Reading layout === */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '240px minmax(0, 1fr) 280px',
        gap: 0,
        maxWidth: 1380,
        margin: '0 auto',
      }}>
        {/* Left: TOC */}
        <aside style={{
          padding: '32px 24px 32px 48px',
          borderRight: '1px solid var(--rule)',
          position: 'sticky',
          top: 0,
          maxHeight: 'calc(100vh - 56px)',
          overflow: 'auto',
        }}>
          <div className="t-meta-cap" style={{ marginBottom: 14 }}>目录 · CONTENTS</div>
          <TOC articles={ARTICLES} activeIdx={activeArticleIdx} onActive={setActiveArticleIdx} />

          <div className="hairline" style={{ margin: '24px 0' }}></div>

          <div className="t-meta-cap" style={{ marginBottom: 12 }}>关联法规 · RELATED</div>
          <ul style={{ margin: 0, padding: 0, listStyle: 'none' }}>
            {['个人信息保护法', '网络安全法', '关键信息基础设施安全保护条例', '数据出境安全评估办法'].map((r, i) => (
              <li key={i} style={{ padding: '6px 0', borderBottom: '1px solid var(--rule)' }}>
                <a href="#" onClick={e => e.preventDefault()} className="no-underline" style={{ fontFamily: 'var(--serif-body)', fontSize: 13, color: 'var(--ink-2)' }}>
                  {r}
                </a>
              </li>
            ))}
          </ul>
        </aside>

        {/* Center: reading column */}
        <main style={{ padding: '40px 56px 80px', position: 'relative' }}>
          {/* Chapter heading */}
          <div style={{ marginBottom: 32 }}>
            <div className="t-meta-cap" style={{ marginBottom: 6 }}>
              <span className="section-mark">§</span> 总则 · CHAPTER I
            </div>
            <h2 style={{
              fontFamily: '"Noto Serif SC", serif',
              fontSize: 28,
              fontWeight: 500,
              margin: 0,
              color: 'var(--ink)',
            }}>第一章 总则</h2>
          </div>

          {ARTICLES.slice(0, 2).map((a, i) => (
            <Article key={a.no} a={a} active={activeArticleIdx === i} onClick={() => setActiveArticleIdx(i)} />
          ))}

          <div style={{ marginTop: 56, marginBottom: 32 }}>
            <div className="t-meta-cap" style={{ marginBottom: 6 }}>
              <span className="section-mark">§</span> 数据安全制度 · CHAPTER III
            </div>
            <h2 style={{
              fontFamily: '"Noto Serif SC", serif',
              fontSize: 28,
              fontWeight: 500,
              margin: 0,
              color: 'var(--ink)',
            }}>第三章 数据安全制度</h2>
          </div>

          {ARTICLES.slice(2, 3).map((a, i) => (
            <Article key={a.no} a={a} active={activeArticleIdx === i + 2} onClick={() => setActiveArticleIdx(i + 2)} />
          ))}

          <div style={{ marginTop: 56, marginBottom: 32 }}>
            <div className="t-meta-cap" style={{ marginBottom: 6 }}>
              <span className="section-mark">§</span> 数据安全保护义务 · CHAPTER IV
            </div>
            <h2 style={{
              fontFamily: '"Noto Serif SC", serif',
              fontSize: 28,
              fontWeight: 500,
              margin: 0,
              color: 'var(--ink)',
            }}>第四章 数据安全保护义务</h2>
          </div>

          {ARTICLES.slice(3).map((a, i) => (
            <Article key={a.no} a={a} active={activeArticleIdx === i + 3} onClick={() => setActiveArticleIdx(i + 3)} />
          ))}

          {/* Document end mark */}
          <div className="flex items-center justify-center" style={{ marginTop: 80, gap: 16 }}>
            <span style={{ flex: 1, height: 1, background: 'var(--rule-strong)' }}></span>
            <Diamond size={10} />
            <span className="t-meta-cap">文献终止 · END OF DOCUMENT</span>
            <Diamond size={10} />
            <span style={{ flex: 1, height: 1, background: 'var(--rule-strong)' }}></span>
          </div>
        </main>

        {/* Right: marginalia + actions */}
        <aside style={{
          padding: '40px 48px 32px 24px',
          borderLeft: '1px solid var(--rule)',
        }}>
          <Marginalia activeArticle={ARTICLES[activeArticleIdx]} go={go} />
        </aside>
      </div>

      {/* Floating AI prompt */}
      {showAIPanel && (
        <div style={{
          position: 'fixed',
          right: 24, bottom: 24,
          width: 360,
          background: 'var(--ink)',
          color: 'var(--paper-2)',
          padding: 20,
          borderRadius: 8,
          boxShadow: '0 20px 40px rgba(11, 21, 48, 0.25)',
          zIndex: 50,
        }}>
          <div className="flex items-center justify-between" style={{ marginBottom: 12 }}>
            <span className="t-meta-cap" style={{ color: 'rgba(255,255,255,0.6)' }}>
              <Diamond size={8} /> 问 X-KU · 关于第二十七条
            </span>
            <button onClick={() => setShowAIPanel(false)} style={{ background: 'transparent', border: 0, color: 'var(--paper-2)', cursor: 'pointer', fontSize: 18, lineHeight: 1 }}>×</button>
          </div>
          <textarea
            placeholder="例如：这一条对我公司具体意味着什么？"
            style={{
              width: '100%',
              minHeight: 80,
              background: 'rgba(255,255,255,0.06)',
              border: '1px solid rgba(255,255,255,0.15)',
              color: 'var(--paper-2)',
              fontFamily: 'var(--serif-body)',
              fontSize: 14,
              padding: 10,
              borderRadius: 4,
              outline: 'none',
              resize: 'none',
            }}
          ></textarea>
          <button className="btn btn-accent" style={{ marginTop: 10, width: '100%', justifyContent: 'center' }} onClick={() => go('ai')}>
            发起可溯源问答 →
          </button>
        </div>
      )}
    </div>
  );
}

function TOC({ articles, activeIdx, onActive }) {
  const chapters = [
    { name: '第一章 总则', items: [0, 1] },
    { name: '第三章 数据安全制度', items: [2] },
    { name: '第四章 数据安全保护义务', items: [3, 4, 5] },
  ];
  return (
    <div style={{ fontFamily: 'var(--serif-body)' }}>
      {chapters.map((ch, ci) => (
        <div key={ci} style={{ marginBottom: 14 }}>
          <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--ink)', marginBottom: 4 }}>{ch.name}</div>
          {ch.items.map(idx => {
            const a = articles[idx];
            const isActive = activeIdx === idx;
            return (
              <div
                key={a.no}
                onClick={() => onActive(idx)}
                style={{
                  padding: '4px 0 4px 12px',
                  cursor: 'pointer',
                  position: 'relative',
                  fontSize: 12,
                  color: isActive ? 'var(--ink)' : 'var(--ink-3)',
                  fontWeight: isActive ? 500 : 400,
                }}
              >
                {isActive && <span style={{ position: 'absolute', left: 0, top: 9, width: 6, height: 6, background: 'var(--accent)', transform: 'rotate(45deg)' }}></span>}
                <span style={{ fontFamily: 'var(--mono)', fontSize: 10, marginRight: 6, color: 'var(--muted)' }}>
                  {String(idx + 1).padStart(2, '0')}
                </span>
                {a.no}
                {a.obligation && <span style={{ marginLeft: 6, color: 'var(--gold)', fontFamily: 'var(--mono)', fontSize: 9 }}>义务</span>}
              </div>
            );
          })}
        </div>
      ))}
    </div>
  );
}

function Article({ a, active, onClick }) {
  return (
    <article
      onClick={onClick}
      style={{
        marginBottom: 36,
        padding: '20px 0 20px 28px',
        position: 'relative',
        cursor: 'pointer',
        borderLeft: active ? '2px solid var(--accent)' : '2px solid transparent',
        background: active ? 'var(--accent-soft)' : 'transparent',
        marginLeft: -28,
        paddingLeft: 28,
        transition: 'all 0.2s var(--ease)',
      }}
    >
      {/* Article number — sits in margin */}
      <div className="flex items-baseline gap-3" style={{ marginBottom: 10 }}>
        <span style={{
          fontFamily: 'var(--serif-display)',
          fontStyle: 'italic',
          fontSize: 22,
          color: a.obligation ? 'var(--accent)' : 'var(--ink)',
          fontWeight: 400,
          lineHeight: 1,
        }}>
          {a.no}
        </span>
        {a.obligation && (
          <span className="chip chip-accent" style={{ height: 18, fontSize: 10, padding: '0 7px' }}>
            <Diamond size={5} /> 核心义务
          </span>
        )}
        {a.tags && a.tags.map(t => (
          <span key={t} className="t-mono" style={{ color: 'var(--muted)' }}>· {t}</span>
        ))}
        <span style={{ flex: 1 }}></span>
        {a.citations && (
          <span className="t-mono" style={{ color: 'var(--muted)' }}>{a.citations} 引用</span>
        )}
      </div>

      <p style={{
        fontFamily: '"Noto Serif SC", serif',
        fontSize: 17,
        lineHeight: 1.85,
        color: 'var(--ink-2)',
        margin: 0,
        letterSpacing: '0.005em',
        textAlign: 'justify',
        textJustify: 'inter-ideograph',
      }}>
        {a.cn}
      </p>

      {a.obligation && (
        <div className="flex items-center gap-2" style={{ marginTop: 14 }}>
          <button className="btn btn-sm">
            <Diamond size={5} /> 加入合规清单
          </button>
          <button className="btn btn-sm">引用</button>
          <button className="btn btn-sm">批注</button>
          <button className="btn btn-sm btn-ghost">看修订史 →</button>
        </div>
      )}
    </article>
  );
}

function Marginalia({ activeArticle, go }) {
  if (!activeArticle) return null;
  return (
    <div style={{ position: 'sticky', top: 24 }}>
      <div className="t-meta-cap" style={{ marginBottom: 12 }}>
        <span className="section-mark">§</span> 边注 · MARGINALIA
      </div>
      <div className="t-mono" style={{ color: 'var(--accent)', marginBottom: 4 }}>{activeArticle.no}</div>

      {activeArticle.annotation && (
        <p className="marginalia" style={{ margin: '12px 0 24px' }}>
          {activeArticle.annotation}
        </p>
      )}

      {/* AI summary */}
      <div style={{
        marginTop: 18,
        padding: '14px 14px 14px',
        background: 'var(--ink)',
        color: 'var(--paper-2)',
        borderRadius: 4,
        position: 'relative',
        overflow: 'hidden',
      }}>
        <div className="t-meta-cap" style={{ color: 'var(--accent)', marginBottom: 8 }}>
          <Diamond size={6} /> AI 摘要
        </div>
        <p style={{
          fontFamily: 'var(--serif-body)',
          fontStyle: 'italic',
          fontSize: 13,
          lineHeight: 1.55,
          margin: 0,
          color: 'rgba(255,255,255,0.85)',
        }}>
          {activeArticle.obligation
            ? '这是一条主动义务，要求处理者建立制度并持续履行；适用范围广。'
            : '本条为定义/适用范围条款，不直接产生义务。'}
        </p>
        <button onClick={() => go('ai')} className="btn btn-sm" style={{
          marginTop: 12,
          background: 'transparent',
          color: 'var(--paper-2)',
          borderColor: 'rgba(255,255,255,0.2)',
          width: '100%',
          justifyContent: 'center',
        }}>展开推理 →</button>
      </div>

      {/* Cited by */}
      <div style={{ marginTop: 24 }}>
        <div className="t-meta-cap" style={{ marginBottom: 10 }}>引用此条的文件 · 7</div>
        <ul style={{ margin: 0, padding: 0, listStyle: 'none' }}>
          {[
            { t: '数据出境安全评估办法 · 第七条', src: 'CAC 2024' },
            { t: '关键信息基础设施安全保护条例 · 第二十二条', src: 'SC 2021' },
            { t: '工业和信息化领域数据安全管理办法（试行）· 第十八条', src: 'MIIT 2022' },
          ].map((c, i) => (
            <li key={i} style={{ padding: '8px 0', borderBottom: '1px solid var(--rule)' }}>
              <div style={{ fontFamily: 'var(--serif-body)', fontSize: 13, color: 'var(--ink-2)', lineHeight: 1.4 }}>{c.t}</div>
              <div className="t-mono" style={{ color: 'var(--muted)', marginTop: 2 }}>{c.src}</div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function VersionLineage({ timeline, current, onSelect }) {
  return (
    <div style={{ padding: '22px 0 18px', position: 'relative' }}>
      <div className="flex items-baseline justify-between" style={{ marginBottom: 18 }}>
        <span className="t-meta-cap">
          <span className="section-mark">§</span> 版本沿革 · LINEAGE
        </span>
        <span className="t-mono" style={{ color: 'var(--muted)' }}>5 个时间点 · 3 个版本</span>
      </div>

      {/* horizontal timeline */}
      <div style={{ position: 'relative', height: 70 }}>
        <div style={{
          position: 'absolute',
          left: 0, right: 0, top: 32,
          height: 1, background: 'var(--rule-strong)',
        }}></div>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          position: 'relative',
        }}>
          {timeline.map((t, i) => {
            const isCurrent = t.isCurrent;
            const isFuture = t.future;
            return (
              <div key={i} style={{ textAlign: 'center', maxWidth: 180 }}>
                <div className="t-mono" style={{ fontSize: 10, color: 'var(--muted)', marginBottom: 4 }}>
                  {t.date}
                </div>
                <div style={{ marginBottom: 6, position: 'relative', height: 14 }}>
                  <span style={{
                    display: 'inline-block',
                    width: isCurrent ? 14 : 10,
                    height: isCurrent ? 14 : 10,
                    background: isFuture ? 'var(--paper)' : (isCurrent ? 'var(--accent)' : 'var(--ink)'),
                    border: isFuture ? `1.5px dashed var(--muted)` : 'none',
                    transform: 'rotate(45deg)',
                    transition: 'all 0.2s var(--ease)',
                  }}></span>
                </div>
                <div style={{
                  fontFamily: 'var(--serif-display)',
                  fontStyle: 'italic',
                  fontSize: 14,
                  color: isFuture ? 'var(--muted)' : (isCurrent ? 'var(--accent)' : 'var(--ink)'),
                }}>
                  {t.label} · {t.v}
                </div>
                {t.note && (
                  <div style={{
                    fontSize: 10,
                    color: 'var(--muted)',
                    marginTop: 2,
                    fontStyle: 'italic',
                    maxWidth: 160,
                    margin: '2px auto 0',
                  }}>
                    {t.note}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

window.LawDetail = LawDetail;

})();

