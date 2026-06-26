import type { AxiosError } from 'axios';
import type { ApiErrorPayload } from '@/types/api';

const HTTP_STATUS_MESSAGES: Record<number, string> = {
  400: '请求参数错误。',
  401: '未登录或登录已过期。',
  403: '无访问权限。',
  404: '请求的资源不存在。',
  405: '请求方法不允许。',
  408: '请求超时，请稍后重试。',
  409: '请求冲突，请刷新后重试。',
  429: '请求过于频繁，请稍后再试。',
  500: '服务器内部错误，请稍后重试。',
  502: '网关错误，请稍后重试。',
  503: '服务暂时不可用，请稍后重试。',
  504: '网关超时，请稍后重试。'
};

function isApiErrorPayload(value: unknown): value is ApiErrorPayload {
  if (!value || typeof value !== 'object') {
    return false;
  }
  return 'msg' in value && typeof (value as { msg?: unknown }).msg === 'string';
}

function extractResponseMessage(data: unknown): string | undefined {
  if (!data || typeof data !== 'object') {
    return undefined;
  }
  const payload = data as { msg?: unknown; message?: unknown };
  if (typeof payload.msg === 'string' && payload.msg.trim()) {
    return payload.msg.trim();
  }
  if (typeof payload.message === 'string' && payload.message.trim()) {
    return payload.message.trim();
  }
  return undefined;
}

function isAxiosDefaultMessage(message: string): boolean {
  return (
    /^Request failed with status code \d+$/.test(message) ||
    /^timeout of \d+ms exceeded$/i.test(message) ||
    message === 'Network Error' ||
    message === 'Request aborted' ||
    message === 'Network request failed'
  );
}

export function resolveHttpStatusMessage(status: number | undefined, fallback = '网络请求失败'): string {
  if (status && HTTP_STATUS_MESSAGES[status]) {
    return HTTP_STATUS_MESSAGES[status];
  }
  if (status) {
    return `请求失败（${status}），请稍后重试。`;
  }
  return fallback;
}

export function resolveHttpErrorMessage(error: AxiosError, fallback = '网络请求失败'): string {
  const fromBody = extractResponseMessage(error.response?.data);
  if (fromBody) {
    return fromBody;
  }

  if (error.code === 'ECONNABORTED' || /timeout/i.test(error.message)) {
    return '请求超时，请稍后重试。';
  }
  if (error.code === 'ERR_CANCELED' || error.message === 'Request aborted') {
    return '请求已取消。';
  }
  if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
    return '网络连接失败，请检查网络后重试。';
  }

  const statusMessage = resolveHttpStatusMessage(error.response?.status, fallback);
  if (error.response?.status) {
    return statusMessage;
  }

  if (error.message.trim() && !isAxiosDefaultMessage(error.message)) {
    return error.message;
  }
  return fallback;
}

export function resolveApiError(error: unknown, fallback: string): string {
  if (isApiErrorPayload(error) && error.msg.trim()) {
    if (!isAxiosDefaultMessage(error.msg)) {
      return error.msg;
    }
    return resolveHttpStatusMessage(error.status, fallback);
  }
  if (error instanceof Error && error.message.trim() && !isAxiosDefaultMessage(error.message)) {
    return error.message;
  }
  return fallback;
}
