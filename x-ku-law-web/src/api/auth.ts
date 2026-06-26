import { http, unwrap } from './http';
import type { CurrentUser, LoginRequest, LoginResponse, RegisterRequest } from '@/types/auth';

export function loginApi(payload: LoginRequest) {
  return unwrap<LoginResponse>(http.post('/auth/login', payload));
}

export function registerApi(payload: RegisterRequest) {
  return unwrap<LoginResponse>(http.post('/auth/register', payload));
}

export function logoutApi() {
  return unwrap<void>(http.post('/auth/logout'));
}

export function meApi() {
  return unwrap<CurrentUser>(http.get('/auth/me'));
}
