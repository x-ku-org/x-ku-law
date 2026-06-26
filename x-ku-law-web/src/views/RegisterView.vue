<template>
  <main class="login">
    <section class="brand" aria-labelledby="register-title">
      <div class="brand__bg" aria-hidden="true">
        <span class="brand__square brand__square--lg"></span>
        <span class="brand__square brand__square--md"></span>
        <span class="brand__diamond"></span>
      </div>

      <header class="brand__top">
        <span class="brand__badge">
          <XkuLogo :size="30" />
        </span>
        <div class="brand__id">
          <strong>X-KU</strong>
          <span>法规智询</span>
        </div>
      </header>

      <div class="brand__copy">
        <p class="brand__chapter">§ 注册</p>
        <h1 id="register-title" class="brand__display">
          建立你的账号，<br /><span class="hl">沉淀每一次检索。</span>
        </h1>
        <p class="brand__lede">订阅、问答、合规任务，登录后随你掌控。</p>
      </div>

      <dl class="brand__ledger" aria-label="平台能力">
        <div>
          <dt class="num">01</dt>
          <dd>条款检索</dd>
        </div>
        <div>
          <dt class="num">02</dt>
          <dd>版本沿革</dd>
        </div>
        <div>
          <dt class="num">03</dt>
          <dd>合规任务</dd>
        </div>
      </dl>
    </section>

    <aside class="access">
      <div class="access__inner">
        <div class="access__meta">
          <span>创建账号</span>
          <time class="clock mono" :datetime="clockIso">北京时间 {{ clock }}</time>
        </div>

        <form class="form" @submit.prevent="submit">
          <header class="form__head">
            <div class="section-kicker">§ 注册</div>
            <h2>创建账号</h2>
            <p>填写信息即可注册并自动登录。</p>
          </header>

          <div class="form__fields">
            <XFormField label="用户名" required>
              <XInput v-model="form.username" autocomplete="username" placeholder="4-32 位" />
            </XFormField>
            <XFormField label="密码" required>
              <span class="password-field">
                <XInput v-model="form.password" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" placeholder="6-64 位" />
                <button type="button" class="password-toggle" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="16" />
                  <Eye v-else :size="16" />
                </button>
              </span>
            </XFormField>
            <XFormField label="确认密码" required>
              <span class="password-field">
                <XInput v-model="form.confirmPassword" :type="showConfirm ? 'text' : 'password'" autocomplete="new-password" />
                <button type="button" class="password-toggle" :aria-label="showConfirm ? '隐藏密码' : '显示密码'" @click="showConfirm = !showConfirm">
                  <EyeOff v-if="showConfirm" :size="16" />
                  <Eye v-else :size="16" />
                </button>
              </span>
            </XFormField>
            <XFormField label="手机号" required>
              <XInput v-model="form.mobile" autocomplete="tel" inputmode="numeric" maxlength="11" placeholder="11 位手机号" />
            </XFormField>
            <XFormField label="图形验证码" required>
              <div class="captcha-row">
                <XInput v-model="form.captcha" autocomplete="off" maxlength="4" placeholder="不区分大小写" />
                <XCaptcha ref="captchaRef" v-model="captchaCode" />
              </div>
            </XFormField>
          </div>

          <p v-if="error" class="error">{{ error }}</p>
          <XButton class="submit-button" variant="primary" type="submit" :disabled="loading">
            {{ loading ? '正在创建…' : '注册并进入' }}
          </XButton>
        </form>

        <div class="access__foot">
          <RouterLink class="public-link" to="/login">
            <span class="diamond" aria-hidden="true"></span>
            已有账号，去登录
          </RouterLink>
          <p>注册成功后将自动登录并进入工作台。</p>
        </div>
      </div>
    </aside>
  </main>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Eye, EyeOff } from '@lucide/vue';
import XkuLogo from '@/components/brand/XkuLogo.vue';
import XButton from '@/components/common/XButton.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XCaptcha from '@/components/common/XCaptcha.vue';
import { useAuthStore } from '@/stores/auth';
import { resolveApiError } from '@/utils/apiError';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const error = ref('');
const showPassword = ref(false);
const showConfirm = ref(false);
const captchaCode = ref('');
const captchaRef = ref<InstanceType<typeof XCaptcha> | null>(null);
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  mobile: '',
  captcha: ''
});

const clock = ref('');
const clockIso = ref('');
let clockTimer: ReturnType<typeof setInterval> | undefined;

const clockFormatter = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false
});

function tickClock() {
  const now = new Date();
  clock.value = clockFormatter.format(now);
  clockIso.value = now.toISOString();
}

function validate(): string | null {
  if (form.username.trim().length < 4 || form.username.trim().length > 32) {
    return '用户名长度需为 4-32 位。';
  }
  if (form.password.length < 6 || form.password.length > 64) {
    return '密码长度需为 6-64 位。';
  }
  if (form.password !== form.confirmPassword) {
    return '两次输入的密码不一致。';
  }
  if (!/^1[3-9]\d{9}$/.test(form.mobile)) {
    return '请输入正确的 11 位手机号。';
  }
  if (form.captcha.trim().toUpperCase() !== captchaCode.value.toUpperCase()) {
    return '图形验证码不正确。';
  }
  return null;
}

async function submit() {
  error.value = '';
  const invalid = validate();
  if (invalid) {
    error.value = invalid;
    return;
  }
  loading.value = true;
  try {
    await auth.register({
      username: form.username.trim(),
      password: form.password,
      mobile: form.mobile.trim()
    });
    router.push({ name: 'app.home' });
  } catch (err) {
    error.value = resolveApiError(err, '注册失败，请稍后重试。');
    captchaRef.value?.refresh();
    form.captcha = '';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  tickClock();
  clockTimer = setInterval(tickClock, 1000);
});

onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer);
});
</script>

<style scoped>
.login {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(400px, 30vw);
  min-height: 100%;
  background: var(--paper);
}

/* ---------- Brand panel (dark) ---------- */
.brand {
  position: relative;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 40px;
  min-height: 100vh;
  padding: clamp(40px, 6vw, 88px);
  overflow: hidden;
  background: linear-gradient(158deg, #0b1530 0%, #0e1a3a 52%, #122249 100%);
  color: #f3f5fa;
}

.brand__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.brand__square {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transform: rotate(45deg);
}

.brand__square--lg {
  top: 50%;
  right: clamp(-120px, -6vw, -60px);
  width: 340px;
  height: 340px;
  margin-top: -170px;
}

.brand__square--md {
  top: 50%;
  right: clamp(-30px, 2vw, 30px);
  width: 200px;
  height: 200px;
  margin-top: -100px;
  border-color: rgba(255, 255, 255, 0.12);
}

.brand__diamond {
  position: absolute;
  top: 50%;
  right: clamp(58px, 7vw, 116px);
  width: 22px;
  height: 22px;
  margin-top: -11px;
  background: var(--accent);
  box-shadow: 0 0 32px 6px var(--accent-glow);
  transform: rotate(45deg);
}

.brand__top,
.brand__copy,
.brand__ledger {
  position: relative;
  z-index: 1;
}

.brand__top {
  display: flex;
  gap: 14px;
  align-items: center;
}

.brand__badge {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.28);
}

.brand__id {
  display: grid;
  gap: 3px;
  line-height: 1;
}

.brand__id strong {
  font-family: var(--sans);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.brand__id span {
  color: rgba(243, 245, 250, 0.62);
  font-family: var(--mono);
  font-size: var(--font-xs);
  letter-spacing: 0.06em;
}

.brand__copy {
  display: grid;
  align-content: center;
  max-width: 640px;
}

.brand__chapter {
  margin: 0 0 18px;
  color: rgba(243, 245, 250, 0.5);
  font-family: var(--mono);
  font-size: var(--font-xs);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand__display {
  margin: 0;
  color: #fff;
  font-family: var(--serif-display);
  font-size: clamp(42px, 5.4vw, 74px);
  font-style: italic;
  font-weight: 400;
  line-height: 1.02;
}

.brand__display .hl {
  padding: 0 2px;
  background: linear-gradient(180deg, transparent 62%, var(--accent-glow) 62%);
}

.brand__lede {
  max-width: 46ch;
  margin: 26px 0 0;
  color: rgba(243, 245, 250, 0.74);
  font-family: var(--serif-body);
  font-size: 17px;
  line-height: 1.7;
}

.brand__ledger {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.brand__ledger div {
  display: grid;
  gap: 10px;
  padding: 22px 24px 0 0;
}

.brand__ledger div + div {
  padding-left: 24px;
  border-left: 1px solid rgba(255, 255, 255, 0.12);
}

.brand__ledger .num {
  color: #fff;
  font-family: var(--serif-display);
  font-size: 34px;
  font-style: italic;
  line-height: 0.9;
}

.brand__ledger dd {
  margin: 0;
  color: rgba(243, 245, 250, 0.74);
  font-family: var(--sans);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.1em;
}

/* ---------- Access panel (light) ---------- */
.access {
  display: grid;
  background: var(--paper-2);
}

.access__inner {
  display: grid;
  align-content: center;
  gap: 24px;
  width: 100%;
  max-width: 420px;
  min-height: 100vh;
  margin: 0 auto;
  padding: clamp(32px, 5vw, 56px) clamp(24px, 4vw, 44px);
}

.access__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--rule);
  color: var(--muted);
  font-size: var(--font-xs);
  font-family: var(--sans);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.clock {
  font-weight: 500;
  letter-spacing: 0.04em;
  text-transform: none;
  font-variant-numeric: tabular-nums;
}

.form {
  display: grid;
  gap: 22px;
  padding: 28px;
  border: 1px solid var(--rule-strong);
  border-radius: 6px;
  background: var(--paper-card);
  box-shadow: 0 1px 2px rgba(11, 21, 48, 0.04), 0 14px 40px rgba(11, 21, 48, 0.06);
}

.form__head {
  display: grid;
  gap: 6px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--rule);
}

.form__head h2 {
  margin: 4px 0 0;
  font-family: var(--serif-display);
  font-size: 32px;
  font-weight: 400;
  font-style: italic;
  line-height: 1.05;
}

.form__head p {
  margin: 0;
  color: var(--muted);
  font-family: var(--serif-body);
  font-size: 13px;
}

.form__fields {
  display: grid;
  gap: 16px;
}

.form :deep(.field) {
  gap: 8px;
}

.form :deep(.label) {
  font-size: var(--font-xs);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.form :deep(.x-input) {
  height: var(--control-h-lg);
  border-color: var(--rule-strong);
  background: var(--paper);
  font-family: var(--mono);
  font-size: 13px;
}

.password-field {
  position: relative;
  display: block;
}

.password-field :deep(.x-input) {
  width: 100%;
  padding-right: 42px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 10px;
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transform: translateY(-50%);
  transition: color 0.15s var(--ease), background 0.15s var(--ease);
}

.password-toggle:hover {
  background: var(--paper-sunk);
  color: var(--ink);
}

.captcha-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
}

.captcha-row :deep(.x-input) {
  width: 100%;
}

.error {
  margin: 0;
  padding: 10px 12px;
  border: 1px solid var(--rose);
  border-radius: 4px;
  background: var(--rose-soft);
  color: var(--rose);
  font-size: 13px;
  line-height: 1.45;
}

.submit-button {
  width: 100%;
  height: var(--control-h-lg);
}

.access__foot {
  display: grid;
  gap: 8px;
  padding-top: 18px;
  border-top: 1px solid var(--rule);
}

.public-link {
  display: inline-flex;
  gap: 9px;
  align-items: center;
  color: var(--ink-2);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.15s var(--ease);
}

.public-link:hover {
  color: var(--accent);
}

.public-link:hover .diamond {
  background: var(--accent);
}

.diamond {
  flex: 0 0 auto;
  width: 8px;
  height: 8px;
  background: var(--ink);
  transform: rotate(45deg);
  transition: background 0.15s var(--ease);
}

.access__foot p {
  margin: 0;
  color: var(--muted);
  font-family: var(--serif-body);
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 1080px) {
  .login {
    grid-template-columns: minmax(0, 1fr) 380px;
  }
}

@media (max-width: 860px) {
  .login {
    grid-template-columns: 1fr;
  }

  .brand {
    min-height: auto;
    gap: 32px;
    padding: 40px 28px;
  }

  .brand__square--lg,
  .brand__square--md,
  .brand__diamond {
    opacity: 0.6;
  }

  .access__inner {
    min-height: auto;
    max-width: none;
  }
}

@media (max-width: 560px) {
  .brand {
    gap: 28px;
    padding: 32px 20px;
  }

  .brand__square--lg,
  .brand__square--md,
  .brand__diamond {
    display: none;
  }

  .brand__lede {
    font-size: 15px;
  }

  .brand__ledger {
    grid-template-columns: 1fr;
  }

  .brand__ledger div {
    padding: 16px 0 0;
  }

  .brand__ledger div + div {
    padding-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.12);
    border-left: 0;
  }

  .access__inner {
    padding: 28px 20px;
  }
}
</style>
