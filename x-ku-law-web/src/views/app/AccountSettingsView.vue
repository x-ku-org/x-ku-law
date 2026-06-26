<template>
  <div class="account-settings">
    <nav class="tabs" aria-label="账号设置">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="tab"
        :class="{ active: active === tab.key }"
        @click="switchTab(tab.key)"
      >
        <component :is="tab.icon" :size="15" />
        <span>{{ tab.label }}</span>
      </button>
    </nav>

    <div class="body">
      <PageState v-if="loadError" :error="loadError" />
      <SkeletonList v-else-if="loading" :count="3" />

      <!-- 账号概览 -->
      <section v-else-if="active === 'overview'" class="pane overview">
        <header class="pane-head">
          <div>
            <h2 class="pane-title">账号概览</h2>
            <p>你的身份、所属与安全信息一览。</p>
          </div>
        </header>

        <XCard>
          <div class="identity">
            <div class="avatar-wrap">
              <img class="avatar-img" :src="avatarSrc" alt="" />
            </div>
            <div class="identity-meta">
              <div class="identity-line">
                <strong class="name">{{ profile?.nickname || profile?.realName || profile?.username }}</strong>
                <XChip tone="accent">{{ labelOf(profile?.userType) }}</XChip>
              </div>
              <span class="username mono">@{{ profile?.username }}</span>
              <span v-if="profile?.tenantName" class="tenant">{{ profile.tenantName }}</span>
            </div>
          </div>
          <div v-if="profile?.roles?.length" class="roles">
            <span class="roles-cap">角色</span>
            <XChip v-for="role in profile.roles" :key="role" tone="outline">{{ labelOf(role) }}</XChip>
          </div>
          <p v-else class="roles-empty">暂无分配角色</p>
        </XCard>

        <XCard>
          <h3 class="card-title">安全信息</h3>
          <dl class="meta-grid">
            <div><dt>上次登录</dt><dd class="mono">{{ formatDateTime(profile?.lastLoginTime) }}</dd></div>
            <div><dt>登录 IP</dt><dd class="mono">{{ profile?.lastLoginIp || '—' }}</dd></div>
            <div><dt>密码更新</dt><dd class="mono">{{ formatDateTime(profile?.passwordUpdateTime) }}</dd></div>
            <div><dt>注册时间</dt><dd class="mono">{{ formatDateTime(profile?.createTime) }}</dd></div>
          </dl>
        </XCard>
      </section>

      <!-- 个人资料 -->
      <section v-else-if="active === 'profile'" class="pane">
        <header class="pane-head">
          <div>
            <h2 class="pane-title">个人资料</h2>
            <p>维护对外展示的昵称、联系方式与头像。</p>
          </div>
        </header>

        <XCard>
          <div class="form">
            <div class="avatar-row">
              <div class="avatar-wrap lg">
                <img class="avatar-img" :src="avatarSrc" alt="" />
              </div>
              <div class="avatar-upload">
                <span class="avatar-upload__label">头像</span>
                <input
                  ref="avatarInput"
                  type="file"
                  accept="image/png,image/jpeg,image/webp,image/gif"
                  class="avatar-file"
                  @change="onAvatarChange"
                />
                <XButton :loading="uploadingAvatar" @click="pickAvatar">上传头像</XButton>
                <small class="avatar-upload__hint">支持 JPG / PNG / WEBP / GIF，不超过 5MB。</small>
              </div>
            </div>

            <div class="grid-2">
              <XFormField label="真实姓名">
                <XInput v-model="profileForm.realName" placeholder="真实姓名" />
              </XFormField>
              <XFormField label="昵称">
                <XInput v-model="profileForm.nickname" placeholder="昵称" />
              </XFormField>
              <XFormField label="邮箱" :error="emailError">
                <XInput v-model="profileForm.email" :invalid="!!emailError" placeholder="name@example.com" />
              </XFormField>
              <XFormField label="手机号" :error="mobileError">
                <XInput v-model="profileForm.mobile" :invalid="!!mobileError" placeholder="11 位手机号" />
              </XFormField>
              <XFormField label="性别">
                <XSelect v-model="profileForm.gender" :options="genderOptions" placeholder="未设置" />
              </XFormField>
            </div>

            <div class="actions">
              <XButton variant="ghost" :disabled="savingProfile" @click="resetProfileForm">还原</XButton>
              <XButton variant="primary" :loading="savingProfile" :disabled="!!emailError || !!mobileError" @click="submitProfile">
                保存资料
              </XButton>
            </div>
          </div>
        </XCard>
      </section>

      <!-- 安全 -->
      <section v-else-if="active === 'security'" class="pane">
        <header class="pane-head">
          <div>
            <h2 class="pane-title">安全</h2>
            <p>修改登录密码；更新后当前登录会话保持有效。</p>
          </div>
        </header>

        <XCard>
          <div class="form narrow">
            <XFormField label="当前密码">
              <XInput v-model="pwForm.oldPassword" type="password" placeholder="当前密码" autocomplete="current-password" />
            </XFormField>
            <XFormField label="新密码" hint="6-64 位" :error="newPwError">
              <XInput v-model="pwForm.newPassword" type="password" :invalid="!!newPwError" placeholder="新密码" autocomplete="new-password" />
            </XFormField>
            <XFormField label="确认新密码" :error="confirmError">
              <XInput v-model="pwForm.confirm" type="password" :invalid="!!confirmError" placeholder="再次输入新密码" autocomplete="new-password" />
            </XFormField>
            <div class="actions">
              <XButton variant="primary" :loading="savingPw" :disabled="!canSubmitPw" @click="submitPassword">更新密码</XButton>
            </div>
          </div>
        </XCard>
      </section>

      <!-- 检索默认 -->
      <section v-else-if="active === 'search'" class="pane">
        <header class="pane-head">
          <div>
            <h2 class="pane-title">检索默认</h2>
            <p>设定后进入法规检索（无分享条件时）将自动套用这些默认筛选与排序。</p>
          </div>
        </header>

        <XCard>
          <div class="form">
            <div class="grid-2">
              <XFormField label="默认排序">
                <XSelect v-model="searchForm.sort" :options="sortOptions" placeholder="默认（相关度）" />
              </XFormField>
              <XFormField label="每页条数">
                <XSelect v-model="searchForm.pageSize" :options="pageSizeOptions" placeholder="默认（20 条）" />
              </XFormField>
              <XFormField label="默认地区">
                <XSelect v-model="searchForm.regionCode" :options="regionSelectOptions" placeholder="不限" />
              </XFormField>
              <XFormField label="默认效力层级">
                <XSelect v-model="searchForm.effectLevel" :options="effectLevelSelectOptions" placeholder="不限" />
              </XFormField>
              <XFormField label="默认时效状态">
                <XSelect v-model="searchForm.status" :options="statusSelectOptions" placeholder="不限" />
              </XFormField>
            </div>
            <div class="actions">
              <XButton variant="ghost" :disabled="savingSearch" @click="resetSearchForm">还原</XButton>
              <XButton variant="primary" :loading="savingSearch" @click="submitSearchDefaults">保存偏好</XButton>
            </div>
          </div>
        </XCard>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { CircleUser, KeyRound, SlidersHorizontal, UserCog } from '@lucide/vue';
import XButton from '@/components/common/XButton.vue';
import XCard from '@/components/common/XCard.vue';
import XChip from '@/components/common/XChip.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XSelect from '@/components/common/XSelect.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import { getAccountProfile, updateAccountProfile, changePassword, uploadAvatar, resolveAvatarUrl } from '@/api/account';
import { usePreferencesStore } from '@/stores/preferences';
import defaultAvatarSrc from '../../../law_avatar.png';
import { useAuthStore } from '@/stores/auth';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';
import { formatDateTime } from '@/utils/datetime';
import { effectLevelOptions, labelOf, regionOptions, searchSortOptions, timelinessOptions } from '@/utils/labels';
import type { AccountProfile } from '@/types/account';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const preferences = usePreferencesStore();
const auth = useAuthStore();

const tabs = [
  { key: 'overview', label: '账号概览', icon: CircleUser },
  { key: 'profile', label: '个人资料', icon: UserCog },
  { key: 'security', label: '安全', icon: KeyRound },
  { key: 'search', label: '检索默认', icon: SlidersHorizontal }
] as const;

const validKeys = tabs.map((t) => t.key) as readonly string[];
const active = computed(() => {
  const t = String(route.query.tab || 'overview');
  return validKeys.includes(t) ? t : 'overview';
});
function switchTab(key: string) {
  router.replace({ query: { ...route.query, tab: key } });
}

const loading = ref(true);
const loadError = ref('');
const profile = ref<AccountProfile | null>(null);

const avatarSrc = computed(() => resolveAvatarUrl(profile.value?.avatarUrl) || defaultAvatarSrc);

const genderOptions = [
  { label: '未设置', value: '' },
  { label: '男', value: '男' },
  { label: '女', value: '女' },
  { label: '保密', value: '保密' }
];

// ===== 个人资料 =====
const profileForm = reactive({ realName: '', nickname: '', email: '', mobile: '', gender: '' });
const savingProfile = ref(false);

// ===== 头像上传 =====
const avatarInput = ref<HTMLInputElement | null>(null);
const uploadingAvatar = ref(false);

function pickAvatar() {
  avatarInput.value?.click();
}

async function onAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  if (!/^image\/(png|jpe?g|webp|gif)$/.test(file.type)) {
    toast.error('仅支持 JPG/PNG/WEBP/GIF 图片');
    input.value = '';
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    toast.error('头像不能超过 5MB');
    input.value = '';
    return;
  }
  uploadingAvatar.value = true;
  try {
    const updated = await uploadAvatar(file);
    profile.value = updated;
    fillProfileForm(updated);
    toast.success('头像已更新');
    // 刷新全局用户资料，侧边栏头像随之更新。
    await auth.ensureProfile(true);
  } catch (err) {
    toast.error(resolveApiError(err, '头像上传失败。'));
  } finally {
    uploadingAvatar.value = false;
    input.value = '';
  }
}

function fillProfileForm(p: AccountProfile) {
  profileForm.realName = p.realName || '';
  profileForm.nickname = p.nickname || '';
  profileForm.email = p.email || '';
  profileForm.mobile = p.mobile || '';
  profileForm.gender = p.gender || '';
}
function resetProfileForm() {
  if (profile.value) fillProfileForm(profile.value);
}

const emailError = computed(() => {
  const v = profileForm.email.trim();
  if (!v) return '';
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? '' : '邮箱格式不正确';
});
const mobileError = computed(() => {
  const v = profileForm.mobile.trim();
  if (!v) return '';
  return /^1[3-9]\d{9}$/.test(v) ? '' : '手机号格式不正确';
});

async function submitProfile() {
  if (emailError.value || mobileError.value) return;
  savingProfile.value = true;
  try {
    await updateAccountProfile({
      realName: profileForm.realName.trim(),
      nickname: profileForm.nickname.trim(),
      email: profileForm.email.trim(),
      mobile: profileForm.mobile.trim(),
      gender: profileForm.gender
    });
    toast.success('资料已保存');
    await loadProfile();
    await auth.ensureProfile(true);
  } catch (err) {
    toast.error(resolveApiError(err, '保存资料失败。'));
  } finally {
    savingProfile.value = false;
  }
}

// ===== 安全 / 密码 =====
const pwForm = reactive({ oldPassword: '', newPassword: '', confirm: '' });
const savingPw = ref(false);

const newPwError = computed(() => {
  const v = pwForm.newPassword;
  if (!v) return '';
  return v.length >= 6 && v.length <= 64 ? '' : '新密码长度需为 6-64 位';
});
const confirmError = computed(() => {
  if (!pwForm.confirm) return '';
  return pwForm.confirm === pwForm.newPassword ? '' : '两次输入不一致';
});
const canSubmitPw = computed(
  () =>
    !!pwForm.oldPassword &&
    !!pwForm.newPassword &&
    !!pwForm.confirm &&
    !newPwError.value &&
    !confirmError.value
);

async function submitPassword() {
  if (!canSubmitPw.value) return;
  savingPw.value = true;
  try {
    await changePassword({ oldPassword: pwForm.oldPassword, newPassword: pwForm.newPassword });
    toast.success('密码已更新');
    pwForm.oldPassword = '';
    pwForm.newPassword = '';
    pwForm.confirm = '';
  } catch (err) {
    toast.error(resolveApiError(err, '密码更新失败。'));
  } finally {
    savingPw.value = false;
  }
}

// ===== 检索默认 =====
const searchForm = reactive({ sort: '', pageSize: '', regionCode: '', effectLevel: '', status: '' });
const savingSearch = ref(false);

const sortOptions = [{ label: '默认（相关度）', value: '' }, ...searchSortOptions];
const pageSizeOptions = [
  { label: '默认（20 条）', value: '' },
  ...[10, 20, 50, 100].map((n) => ({ label: `${n} 条/页`, value: String(n) }))
];
const regionSelectOptions = [{ label: '不限', value: '' }, ...regionOptions];
const effectLevelSelectOptions = [{ label: '不限', value: '' }, ...effectLevelOptions];
const statusSelectOptions = [{ label: '不限', value: '' }, ...timelinessOptions];

function fillSearchForm() {
  const d = preferences.searchDefaults;
  searchForm.sort = d.sort;
  searchForm.pageSize = d.pageSize != null ? String(d.pageSize) : '';
  searchForm.regionCode = d.regionCode;
  searchForm.effectLevel = d.effectLevel;
  searchForm.status = d.status;
}
function resetSearchForm() {
  fillSearchForm();
}

async function submitSearchDefaults() {
  savingSearch.value = true;
  try {
    await preferences.saveSearchDefaults({
      sort: searchForm.sort,
      pageSize: searchForm.pageSize ? Number(searchForm.pageSize) : undefined,
      regionCode: searchForm.regionCode,
      effectLevel: searchForm.effectLevel,
      status: searchForm.status
    });
    toast.success('检索偏好已保存');
  } catch (err) {
    toast.error(resolveApiError(err, '保存偏好失败。'));
  } finally {
    savingSearch.value = false;
  }
}

async function loadProfile() {
  const p = await getAccountProfile();
  profile.value = p;
  fillProfileForm(p);
}

onMounted(async () => {
  try {
    await Promise.all([loadProfile(), preferences.ensureLoaded()]);
    fillSearchForm();
  } catch (err) {
    loadError.value = resolveApiError(err, '加载账号信息失败。');
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.account-settings {
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px 48px 48px;
  animation: page-in 0.45s var(--ease) both;
}

.tabs {
  display: flex;
  gap: 6px;
  border-bottom: 1px solid var(--rule);
}

.tab {
  display: inline-flex;
  gap: 7px;
  align-items: center;
  padding: 9px 14px;
  margin-bottom: -1px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--ink-3);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.14s var(--ease), border-color 0.14s var(--ease);
}

.tab :deep(svg) {
  color: var(--muted);
  transition: color 0.14s var(--ease);
}

.tab:hover {
  color: var(--ink);
}

.tab.active {
  color: var(--accent-deep);
  border-bottom-color: var(--accent);
  font-weight: 600;
}

.tab.active :deep(svg) {
  color: var(--accent);
}

.body {
  max-width: 1000px;
  padding-top: 28px;
}

.pane {
  display: grid;
  gap: 16px;
}

.pane-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.pane-title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 24px;
  font-weight: 400;
  color: var(--ink);
}

.pane-head p {
  margin: 6px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 14px;
}

.pane :deep(.x-card) {
  padding: 20px 22px;
}

.card-title {
  margin: 0 0 14px;
  font-family: var(--serif-display);
  font-size: 18px;
  font-weight: 400;
  color: var(--ink);
}

/* 概览 */
.identity {
  display: flex;
  gap: 16px;
  align-items: center;
}

.avatar-wrap {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  overflow: hidden;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper-sunk);
}

.avatar-wrap.lg {
  width: 72px;
  height: 72px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload {
  display: grid;
  gap: 8px;
  justify-items: start;
}

.avatar-upload__label {
  color: var(--ink-2);
  font-size: 12px;
  font-weight: 600;
}

.avatar-file {
  display: none;
}

.avatar-upload__hint {
  color: var(--muted);
  font-size: 12px;
}

.identity-meta {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.identity-line {
  display: flex;
  gap: 10px;
  align-items: center;
}

.identity-line .name {
  color: var(--ink);
  font-size: 17px;
  font-weight: 600;
}

.username {
  color: var(--muted);
  font-size: 12.5px;
}

.tenant {
  color: var(--ink-3);
  font-size: 12.5px;
}

.roles {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  align-items: center;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--rule);
}

.roles-cap {
  color: var(--muted);
  font-size: 12px;
}

.roles-empty {
  margin: 16px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--rule);
  color: var(--muted);
  font-size: 12.5px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 28px;
  margin: 0;
}

.meta-grid dt {
  color: var(--muted);
  font-size: 12px;
}

.meta-grid dd {
  margin: 4px 0 0;
  color: var(--ink-2);
  font-size: 13px;
}

/* 表单 */
.form {
  display: grid;
  gap: 18px;
}

.form.narrow {
  max-width: 420px;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.avatar-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.avatar-row .grow {
  flex: 1;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 4px;
}

@media (max-width: 860px) {
  .account-settings {
    padding: 20px 14px 40px;
  }

  .grid-2,
  .meta-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
