import type { AuthSession, LoginResponse } from '@/types/auth';

const SESSION_KEY = 'xku.auth.session';

export function getSession(): AuthSession | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function setSession(payload: LoginResponse & { tenantCode: string; username: string }) {
  const session: AuthSession = {
    ...payload,
    loginAt: Date.now()
  };
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  window.dispatchEvent(new CustomEvent('xku-auth-change'));
}

export function updateAccessToken(accessToken: string) {
  const session = getSession();
  if (!session) return;
  localStorage.setItem(SESSION_KEY, JSON.stringify({ ...session, accessToken }));
  window.dispatchEvent(new CustomEvent('xku-auth-change'));
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  window.dispatchEvent(new CustomEvent('xku-auth-change'));
}

export function getAccessToken() {
  return getSession()?.accessToken;
}

export function getRefreshToken() {
  return getSession()?.refreshToken;
}
