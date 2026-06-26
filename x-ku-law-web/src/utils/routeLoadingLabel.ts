/** Boot / Suspense fallback copy keyed by pathname. */
export function routeLoadingLabel(path: string): string {
  if (path.startsWith('/login')) return '正在载入登录';
  if (path.startsWith('/admin')) return '正在载入后台';
  if (path.startsWith('/app/workbench')) return '正在载入工作台';
  if (path === '/' || path === '/app' || path === '/app/' || path.startsWith('/app/home')) {
    return '正在载入首页';
  }
  if (path.startsWith('/app/laws/search')) return '正在载入检索';
  if (path.startsWith('/app/laws/') && path.includes('/compare')) return '正在载入版本对比';
  if (path.startsWith('/app/laws/')) return '正在载入法规';
  if (path.startsWith('/app/ai/chat')) return '正在载入会话';
  if (path.startsWith('/app/')) return '正在载入页面';
  return '正在载入';
}
