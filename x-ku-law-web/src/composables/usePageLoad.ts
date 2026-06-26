import { ref } from 'vue';
import { resolveApiError } from '@/utils/apiError';

export function usePageLoad(defaultError = '数据读取失败。') {
  const loading = ref(false);
  const error = ref('');

  async function run<T>(fn: () => Promise<T>): Promise<T | undefined> {
    loading.value = true;
    error.value = '';
    try {
      return await fn();
    } catch (err) {
      error.value = resolveApiError(err, defaultError);
      return undefined;
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, run };
}
