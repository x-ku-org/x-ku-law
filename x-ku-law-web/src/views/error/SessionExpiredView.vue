<template>
  <main class="expired">
    <div class="section-kicker">§ Session</div>
    <h1 class="display">登录状态已过期。</h1>
    <p>请重新登录，系统会带上原始访问地址继续跳转。</p>
    <XButton variant="primary" @click="goLogin">重新登录</XButton>
  </main>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import XButton from '@/components/common/XButton.vue';

const route = useRoute();
const router = useRouter();

function goLogin() {
  router.push({ path: '/login', query: { redirect: route.query.redirect as string } });
}
</script>

<style scoped>
.expired {
  display: grid;
  gap: 20px;
  align-content: center;
  min-height: 100%;
  padding: 56px;
}

p {
  margin: 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 18px;
}
</style>
