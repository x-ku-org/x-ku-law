import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { setUnauthorizedHandler } from './api/http';
import { getSession } from './api/token';
import { useAuthStore } from './stores/auth';
import { useDictStore } from './stores/dict';
import './styles/tokens.css';
import './styles/base.css';

const app = createApp(App);
const pinia = createPinia();
app.use(pinia).use(router);

setUnauthorizedHandler((redirect) => {
  const current = router.currentRoute.value;
  if (current.name === 'login' || current.name === 'session-expired') return;
  router.replace({ name: 'login', query: { redirect } });
});

async function bootstrap() {
  if (getSession()) {
    await useAuthStore(pinia).ensureProfile();
    // 预加载核心字典词表（失败不阻塞挂载，调用方回退内置常量）。
    void useDictStore(pinia).ensureLoaded();
  }
  app.mount('#app');
}

void bootstrap();
