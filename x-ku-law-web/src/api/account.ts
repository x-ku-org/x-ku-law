import { http, unwrap } from './http';
import type { AccountProfile, AccountProfileUpdate, ChangePasswordPayload } from '@/types/account';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

/** 把后端返回的头像相对路径拼成 <img> 可直接使用的地址；无头像返回 null。 */
export function resolveAvatarUrl(path?: string | null): string | null {
  return path ? `${API_BASE}${path}` : null;
}

export function getAccountProfile() {
  return unwrap<AccountProfile>(http.get('/account/profile'));
}

export function updateAccountProfile(payload: AccountProfileUpdate) {
  return unwrap<void>(http.put('/account/profile', payload));
}

export function uploadAvatar(file: File) {
  const form = new FormData();
  form.append('file', file);
  return unwrap<AccountProfile>(http.post('/account/avatar', form));
}

export function changePassword(payload: ChangePasswordPayload) {
  return unwrap<void>(http.post('/account/password', payload));
}

export function getPreferences() {
  return unwrap<Record<string, string>>(http.get('/account/preferences'));
}

export function savePreferences(kv: Record<string, string>) {
  return unwrap<void>(http.put('/account/preferences', kv));
}
