import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { loginApi, logoutApi, meApi, registerApi } from '@/api/auth';
import { clearSession, getSession, setSession } from '@/api/token';
import { usePreferencesStore } from '@/stores/preferences';
import type { CurrentUser, LoginRequest, RegisterRequest } from '@/types/auth';

const DEFAULT_TENANT_CODE = import.meta.env.VITE_DEFAULT_TENANT_CODE ?? 'platform';

const ADMIN_ROLE = 'platform_admin';

export const useAuthStore = defineStore('auth', () => {
  const session = ref(getSession());
  const profile = ref<CurrentUser | null>(null);
  const isAuthed = computed(() => Boolean(session.value?.accessToken));

  const isAdmin = computed(() => {
    const p = profile.value;
    if (!p) return false;
    if (p.roles?.includes(ADMIN_ROLE)) return true;
    return (p.permissions || []).some((code) => code.startsWith('system:'));
  });

  function hasPermission(code: string) {
    return Boolean(profile.value?.permissions?.includes(code));
  }

  function sync() {
    session.value = getSession();
    if (!session.value) profile.value = null;
  }

  let profilePromise: Promise<CurrentUser | null> | null = null;
  async function ensureProfile(force = false) {
    if (!getSession()) {
      profile.value = null;
      return null;
    }
    if (profile.value && !force) return profile.value;
    profilePromise ??= meApi()
      .then((me) => {
        profile.value = me;
        return me;
      })
      .catch(() => {
        profile.value = null;
        return null;
      })
      .finally(() => {
        profilePromise = null;
      });
    return profilePromise;
  }

  async function login(payload: LoginRequest) {
    const result = await loginApi(payload);
    setSession({ ...result, tenantCode: payload.tenantCode, username: payload.username });
    sync();
    await ensureProfile(true);
  }

  async function register(payload: RegisterRequest) {
    const result = await registerApi(payload);
    setSession({ ...result, tenantCode: DEFAULT_TENANT_CODE, username: payload.username });
    sync();
    await ensureProfile(true);
  }

  async function logout() {
    try {
      if (session.value?.accessToken) await logoutApi();
    } finally {
      clearSession();
      profile.value = null;
      // 清空个性化偏好缓存，避免下一位登录用户继承上一位的检索默认值。
      usePreferencesStore().reset();
      sync();
    }
  }

  window.addEventListener('xku-auth-change', sync);

  return { session, profile, isAuthed, isAdmin, hasPermission, ensureProfile, login, register, logout, sync };
});
