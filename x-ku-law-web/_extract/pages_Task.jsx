// === src/pages/Task.jsx ===
(function(){
// Task detail — editorial brief
const { useState: useStateT } = React;

function TaskDetail({ go }) {
  const { COMPLIANCE_TASKS, TASK_TIMELINE } = window.XKU_DATA;
  const t = COMPLIANCE_TASKS[0];
  const [evidenceFocus, setEvidenceFocus] = useStateT(1);

  return (
    <div className="page-enter" style={{ padding: '32px 48px 56px', maxWidth: 1280, margin: '0 auto' }}>

      {/* === Breadcrumbs === */}
      <div className="flex items-center gap-2 t-mono" style={{ marginBottom: 18, color: 'var(--muted)' }}>
        <a href="#" onClick={e => { e.preventDefault(); go('compliance'); }} style={{ color: 'var(--muted)', textDecoration: 'none' }}>合规</a>
        <span>›</span>
        <span>任务</span>
        <span>›</span>
        <span style={{ color: 'var(--ink-2)' }}>{t.id}</span>
      </div>

      {/* === Header === */}
      <header style={{ marginBottom: 28 }}>
        <div className="flex items-baseline justify-between" style={{ marginBottom: 14 }}>
          <div className="flex items-center gap-2">
            <span className={`chip ${t.priority === 'high' ? 'chip-rose' : 'chip-gold'}`}>高优 · HIGH</span>
            <span className="chip chip-accent">{t.status}</span>
            <span className="t-mono">主体：{t.subject}</span>
          </div>
          <div className="flex gap-2">
            <button className="btn btn-sm">关注</button>
            <button className="btn btn-sm">转交</button>
            <button className="btn btn-sm btn-primary">提交审核</button>
          </div>
        </div>

        <h1 style={{
          fontFamily: 'var(--serif-display)',
          fontSize: 48,
          lineHeight: 1.05,
          letterSpacing: '-0.015em',
          margin: '0 0 16px',
          fontWeight: 400,
        }}>
          {t.title.split('').map((c, i) => i === 0 ? <em key={i}>{c}</em> : c)}
        </h1>

        <p style={{
          fontFamily: 'var(--serif-body)',
          fontSize: 16,
          lineHeight: 1.7,
          color: 'var(--ink-3)',
          margin: 0,
          maxWidth: 800,
        }}>
          本任务由系统依据 <span className="hl">《数据出境安全评估办法 · 第七条》</span> 自动生成，
          归属"境外仓 · 香港业务线"主体；需在 <strong>{t.due}</strong> 前完成全部材料并发起申报。
        </p>
      </header>

      {/* === Main two-col === */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.6fr 1fr', gap: 36 }}>

        {/* LEFT */}
        <div>

          {/* Progress meter */}
          <section className="card" style={{ padding: '22px 24px', marginBottom: 28 }}>
            <div className="flex items-baseline justify-between" style={{ marginBottom: 14 }}>
              <div className="t-meta-cap">进度 · 工序</div>
              <div className="t-mono">距截止 22 天</div>
            </div>
            <ProgressLadder />
          </section>

          {/* Basis — legal anchor */}
          <section style={{ marginBottom: 28 }}>
            <div className="t-meta-cap" style={{ marginBottom: 12 }}>
              <span className="section-mark">§</span> 法律依据 · LEGAL BASIS
            </div>
            <div className="card" style={{ padding: '20px 24px', position: 'relative' }}>
              <div className="flex items-baseline justify-between" style={{ marginBottom: 8 }}>
                <span className="t-mono" style={{ color: 'var(--accent)' }}>第七条</span>
                <a href="#" onClick={e => { e.preventDefault(); go('law'); }} className="t-mono no-underline" style={{ color: 'var(--ink-2)' }}>
                  跳至正文 →
                </a>
              </div>
              <div style={{
                fontFamily: 'var(--serif-display)',
                fontSize: 22,
                marginBottom: 8,
                fontStyle: 'italic',
                color: 'var(--ink)',
              }}>数据出境安全评估办法</div>
              <p style={{
                fontFamily: '"Noto Serif SC", serif',
                fontSize: 15,
                lineHeight: 1.75,
                color: 'var(--ink-2)',
                margin: 0,
                letterSpacing: '0.005em',
              }}>
                "数据处理者申报数据出境安全评估的，<span className="hl">应当通过所在地省级网信部门</span>
                向国家网信部门<span className="hl">提交</span>：(一) 申报书；(二) 风险自评估报告；(三) 数据出境合同或法律文件；(四) 网信部门要求提交的其他材料。"
              </p>
            </div>
          </section>

          {/* Evidence */}
          <section style={{ marginBottom: 28 }}>
            <div className="flex items-baseline justify-between" style={{ marginBottom: 12 }}>
              <div className="t-meta-cap">
                <span className="section-mark">§</span> 证据材料 · EVIDENCE
              </div>
              <button className="btn btn-sm">
                <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M6 2 V8 M3 5 L6 2 L9 5 M2 10 H10"/></svg>
                上传新材料
              </button>
            </div>
            <div className="card" style={{ overflow: 'hidden' }}>
              {[
                { name: '数据资产盘点 v4.xlsx', size: '38 KB', by: '李书航', when: '05-18', status: 'ok', match: '完整' },
                { name: '出境数据流图 v2.pdf',   size: '1.2 MB', by: '李书航', when: '05-18', status: 'ok', match: '完整' },
                { name: '风险自评估报告 v0.3.docx', size: '780 KB', by: '李书航', when: '05-19', status: 'partial', match: '3 处缺失' },
                { name: '接收方安全能力评估（待补）', size: '—', by: '—', when: '—', status: 'missing', match: '缺失' },
              ].map((e, i) => (
                <div key={i}
                  onClick={() => setEvidenceFocus(i)}
                  style={{
                    display: 'grid',
                    gridTemplateColumns: '1fr 100px 80px 70px 90px',
                    gap: 14,
                    padding: '14px 18px',
                    borderBottom: i < 3 ? '1px solid var(--rule)' : 'none',
                    alignItems: 'center',
                    cursor: 'pointer',
                    background: evidenceFocus === i ? 'var(--paper-2)' : 'transparent',
                  }}
                >
                  <div className="flex items-center gap-3">
                    <span style={{
                      width: 28, height: 28,
                      background:
                        e.status === 'ok' ? 'var(--moss-soft)' :
                        e.status === 'partial' ? 'var(--gold-soft)' : 'var(--rose-soft)',
                      color:
                        e.status === 'ok' ? 'var(--moss)' :
                        e.status === 'partial' ? 'var(--gold)' : 'var(--rose)',
                      display: 'grid', placeItems: 'center',
                      borderRadius: 3,
                      fontFamily: 'var(--mono)',
                      fontSize: 9,
                    }}>{e.name.split('.').pop().slice(0, 3).toUpperCase()}</span>
                    <div>
                      <div style={{ fontFamily: 'var(--serif-body)', fontSize: 13, color: 'var(--ink)', fontWeight: 500 }}>
                        {e.name}
                      </div>
                      <div className="t-mono" style={{ color: 'var(--muted)', marginTop: 2 }}>
                        {e.by} · {e.when}
                      </div>
                    </div>
                  </div>
                  <span className="t-mono" style={{ color: 'var(--muted)' }}>{e.size}</span>
                  <span className={`chip ${
                    e.status === 'ok' ? 'chip-moss' :
                    e.status === 'partial' ? 'chip-gold' : 'chip-rose'
                  }`} style={{ height: 18, fontSize: 10, padding: '0 7px' }}>
                    {e.match}
                  </span>
                  <span className="t-mono" style={{ color: 'var(--muted)' }}>v{i + 1}</span>
                  <div className="text-right">
                    <button className="btn btn-sm btn-ghost">…</button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* AI check */}
          <section style={{
            padding: '20px 22px',
            background: 'var(--ink)',
            color: 'var(--paper-2)',
            borderRadius: 4,
            position: 'relative',
            overflow: 'hidden',
          }}>
            <div style={{ position: 'absolute', right: -40, top: -30, opacity: 0.12 }}>
              <CubeOrnament size={160} />
            </div>
            <div className="t-meta-cap" style={{ color: 'var(--accent)', marginBottom: 10 }}>
              <Diamond size={6} /> AI 复核 · 5·19 10:02
            </div>
            <p style={{
              fontFamily: 'var(--serif-display)',
              fontStyle: 'italic',
              fontSize: 18,
              color: 'var(--paper-2)',
              lineHeight: 1.4,
              margin: 0,
              maxWidth: 520,
              position: 'relative', zIndex: 1,
            }}>
              "对比 v3 模板，<br/>
              <span style={{ color: 'var(--accent)' }}>3 处缺失字段</span>需补全：①重要数据定级依据；
              ②接收方安全能力评估；③安全风险及应对措施。"
            </p>
            <div className="flex gap-2" style={{ marginTop: 14 }}>
              <button className="btn btn-sm btn-accent">让 AI 自动补全 →</button>
              <button className="btn btn-sm" style={{ background: 'transparent', color: 'var(--paper-2)', borderColor: 'rgba(255,255,255,0.2)' }}>查看完整复核</button>
            </div>
          </section>
        </div>

        {/* RIGHT */}
        <aside>
          {/* meta card */}
          <section className="card" style={{ padding: '18px 20px', marginBottom: 20 }}>
            <div className="t-meta-cap" style={{ marginBottom: 14 }}>属性 · META</div>
            <Meta k="任务编号" v={<span className="t-mono">{t.id}</span>} />
            <Meta k="责任人"   v={<span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <span style={{ width: 18, height: 18, borderRadius: '50%', background: 'var(--ink)', color: 'var(--paper-2)', display: 'inline-grid', placeItems: 'center', fontFamily: 'var(--serif-display)', fontSize: 10, fontStyle: 'italic' }}>{t.owner[0]}</span>
              {t.owner}
            </span>} />
            <Meta k="协办人"   v="张雯、王予" />
            <Meta k="主体"     v={t.subject} />
            <Meta k="清单"     v={<a href="#" className="no-underline" style={{ color: 'var(--accent)' }}>数据安全 · 高优 v3</a>} />
            <Meta k="创建"     v={<span className="t-mono">2026-05-12 09:20</span>} last />
          </section>

          {/* activity */}
          <section>
            <div className="t-meta-cap" style={{ marginBottom: 14 }}>
              <span className="section-mark">§</span> 活动 · ACTIVITY
            </div>
            <div style={{ position: 'relative' }}>
              <div style={{
                position: 'absolute',
                left: 7, top: 4, bottom: 4,
                width: 1, background: 'var(--rule)',
              }}></div>
              {TASK_TIMELINE.map((e, i) => (
                <div key={i} style={{ position: 'relative', paddingLeft: 28, paddingBottom: 18 }}>
                  <span style={{
                    position: 'absolute', left: 4, top: 5,
                    width: 8, height: 8, background: i === 0 ? 'var(--accent)' : 'var(--paper)',
                    border: i === 0 ? 'none' : '1.2px solid var(--ink)',
                    transform: 'rotate(45deg)',
                  }}></span>
                  <div className="t-mono" style={{ marginBottom: 4, color: 'var(--muted)' }}>
                    {e.ts} · {e.by}
                  </div>
                  <div style={{
                    fontFamily: 'var(--serif-body)',
                    fontSize: 13.5,
                    lineHeight: 1.55,
                    color: 'var(--ink-2)',
                  }}>
                    {e.what}
                  </div>
                </div>
              ))}
              {/* compose */}
              <div style={{ paddingLeft: 28, marginTop: 8 }}>
                <textarea
                  placeholder="留个批注 …  支持 @ 提及"
                  style={{
                    width: '100%',
                    padding: 10,
                    border: '1px solid var(--rule-strong)',
                    borderRadius: 4,
                    background: 'var(--paper-card)',
                    fontFamily: 'var(--serif-body)',
                    fontSize: 13,
                    resize: 'none',
                    minHeight: 60,
                    outline: 'none',
                  }}
                ></textarea>
                <div className="flex justify-end gap-2" style={{ marginTop: 6 }}>
                  <button className="btn btn-sm">取消</button>
                  <button className="btn btn-sm btn-primary">发送</button>
                </div>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}

function Meta({ k, v, last }) {
  return (
    <div style={{
      padding: '8px 0',
      borderBottom: last ? 'none' : '1px solid var(--rule)',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      gap: 16,
    }}>
      <span className="t-meta-cap">{k}</span>
      <span style={{ fontFamily: 'var(--serif-body)', fontSize: 13, color: 'var(--ink-2)', textAlign: 'right' }}>{v}</span>
    </div>
  );
}

function ProgressLadder() {
  const steps = [
    { k: 'A', t: '数据盘点',   done: true,  active: false },
    { k: 'B', t: '风险评估',   done: true,  active: false },
    { k: 'C', t: '材料整理',   done: false, active: true,  pct: 60 },
    { k: 'D', t: '内部审核',   done: false, active: false },
    { k: 'E', t: '省网信受理', done: false, active: false },
    { k: 'F', t: '国家网信审查', done: false, active: false },
  ];
  return (
    <div className="flex items-center" style={{ position: 'relative' }}>
      {steps.map((s, i) => (
        <React.Fragment key={i}>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            position: 'relative',
            zIndex: 1,
          }}>
            <span style={{
              width: 32, height: 32, borderRadius: '50%',
              background: s.done ? 'var(--ink)' : (s.active ? 'var(--paper)' : 'var(--paper-card)'),
              color: s.done ? 'var(--paper-2)' : (s.active ? 'var(--accent)' : 'var(--muted-2)'),
              border: s.active ? '2px solid var(--accent)' : (s.done ? '2px solid var(--ink)' : '1px solid var(--rule-strong)'),
              display: 'grid', placeItems: 'center',
              fontFamily: 'var(--serif-display)', fontSize: 13, fontStyle: 'italic',
            }}>{s.done ? '✓' : s.k}</span>
            <span style={{
              fontFamily: 'var(--serif-body)',
              fontSize: 12,
              color: s.active ? 'var(--ink)' : 'var(--muted)',
              fontWeight: s.active ? 500 : 400,
              marginTop: 8,
            }}>{s.t}</span>
            {s.active && s.pct !== undefined && (
              <span className="t-mono" style={{ marginTop: 2, color: 'var(--accent)' }}>{s.pct}%</span>
            )}
          </div>
          {i < steps.length - 1 && (
            <div style={{
              flex: 1,
              height: 1,
              background: s.done ? 'var(--ink)' : 'var(--rule)',
              margin: '0 4px',
              marginBottom: 30,
            }}></div>
          )}
        </React.Fragment>
      ))}
    </div>
  );
}

window.TaskDetail = TaskDetail;

})();

