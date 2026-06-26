import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { clearSession, getAccessToken, getRefreshToken, updateAccessToken } from './token';
import type { CommonResult } from '@/types/api';
import { resolveHttpErrorMessage } from '@/utils/apiError';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000
});

const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 12000
});

let refreshing: Promise<string> | null = null;

let unauthorizedHandler: ((redirect: string) => void) | null = null;
export function setUnauthorizedHandler(handler: (redirect: string) => void) {
  unauthorizedHandler = handler;
}

function handleUnauthorized() {
  clearSession();
  const redirect = window.location.pathname + window.location.search;
  if (unauthorizedHandler) {
    unauthorizedHandler(redirect);
  } else {
    window.location.assign(`/login?redirect=${encodeURIComponent(redirect)}`);
  }
}

function attachToken(config: InternalAxiosRequestConfig) {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error('missing refresh token');
  refreshing ??= refreshClient
    .post<CommonResult<string>>('/auth/refresh', null, {
      headers: { 'X-Refresh-Token': refreshToken }
    })
    .then((response) => {
      if (response.data.code !== 0 || !response.data.data) {
        throw new Error(response.data.msg || 'refresh failed');
      }
      updateAccessToken(response.data.data);
      return response.data.data;
    })
    .finally(() => {
      refreshing = null;
    });
  return refreshing;
}

http.interceptors.request.use(attachToken);

http.interceptors.response.use(
  (response) => {
    const body = response.data as CommonResult<unknown>;
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject({ code: body.code, msg: body.msg || '请求失败', status: response.status });
    }
    return response;
  },
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const status = error.response?.status;

    // 仅排除签发/校验类端点（它们 401 不该触发刷新重放，否则死循环）；
    // /auth/me 等需鉴权的普通接口必须能走刷新，否则 access 过期即被强制重登。
    const isAuthFreeEndpoint = /\/auth\/(login|register|refresh|logout)\b/.test(original?.url ?? '');

    if (status === 401 && original && !original._retry && !isAuthFreeEndpoint) {
      try {
        original._retry = true;
        const token = await refreshAccessToken();
        original.headers.Authorization = `Bearer ${token}`;
        return http(original);
      } catch {
        handleUnauthorized();
      }
    } else if (status === 401) {
      handleUnauthorized();
    }


    return Promise.reject({
      code: status || 500,
      msg: resolveHttpErrorMessage(error),
      status
    });
  }
);

export async function unwrap<T>(request: Promise<{ data: CommonResult<T> }>): Promise<T> {
  const response = await request;
  return response.data.data;
}
