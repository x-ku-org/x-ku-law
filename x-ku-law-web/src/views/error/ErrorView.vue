<template>
  <main class="error-page hairline-strong">
    <Diamond :size="8" />
    <div class="section-kicker">§ {{ code }}</div>
    <h1 class="display">{{ title }}</h1>
    <p>{{ description }}</p>
    <div class="actions">
      <XButton v-if="showBackButton" variant="ghost" @click="router.back()">返回上一页</XButton>
      <XButton v-if="code !== 500" variant="ghost" @click="router.push('/app/laws/search')">重新检索</XButton>
      <XButton variant="primary" @click="router.push('/app/home')">返回首页</XButton>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Diamond from '@/components/brand/Diamond.vue';
import XButton from '@/components/common/XButton.vue';

const route = useRoute();
const router = useRouter();
const code = computed(() => Number(route.meta.code || 404));
const title = computed(() => (code.value === 403 ? '无访问权限' : code.value === 500 ? '系统异常' : '页面不存在'));
const description = computed(() => {
  if (code.value === 403) return '当前账号没有访问该页面或操作的权限。';
  if (code.value === 500) return '请求处理失败，请稍后重试。';
  return '目标页面没有被当前前端路由识别。';
});
const showBackButton = computed(() => window.history.length > 1);
</script>

<style scoped>
.error-page {
  display: grid;
  gap: 20px;
  align-content: center;
  min-height: 100%;
  padding: 56px;
}

p {
  max-width: 54ch;
  margin: 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
