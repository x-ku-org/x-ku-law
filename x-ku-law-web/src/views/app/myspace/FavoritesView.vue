<template>
  <div class="ms-pane">
    <header class="ms-pane__head">
      <div>
        <h2 class="ms-pane__title">收藏夹</h2>
        <p>集中管理收藏的法规与检索对象，点击即可回到原文。</p>
      </div>
      <span class="ms-pane__count mono">共 {{ total }} 条</span>
    </header>

    <nav v-if="folders.length" class="ms-folders" aria-label="文件夹">
      <button type="button" class="ms-folder" :class="{ active: folder === '' }" @click="selectFolder('')">全部</button>
      <button
        v-for="f in folders"
        :key="f"
        type="button"
        class="ms-folder"
        :class="{ active: folder === f }"
        @click="selectFolder(f)"
      >
        {{ f }}
      </button>
    </nav>

    <PageState v-if="error" :error="error" />
    <SkeletonList v-else-if="loading" :count="4" />
    <EmptyState
      v-else-if="!rows.length"
      title="收藏夹是空的"
      description="在法规详情或检索结果里点收藏，条目会出现在这里。"
    >
      <XButton variant="primary" @click="goSearch">去检索法规</XButton>
    </EmptyState>

    <ul v-else class="fav-list">
      <li v-for="fav in rows" :key="fav.id" class="fav-item">
        <button v-if="isLaw(fav)" type="button" class="fav-item__title link" @click="openLaw(fav)">
          {{ fav.titleSnapshot || `收藏 #${fav.id}` }}
        </button>
        <span v-else class="fav-item__title">{{ fav.titleSnapshot || `收藏 #${fav.id}` }}</span>
        <div class="fav-item__meta">
          <XChip tone="outline">{{ labelOf(fav.refType) }}</XChip>
          <XChip v-if="fav.folderName" tone="default">{{ fav.folderName }}</XChip>
          <span class="mono fav-item__time">{{ formatDateTime(fav.createTime) }}</span>
        </div>
        <XButton size="small" variant="ghost" class="fav-item__del" @click="remove(fav)">删除</XButton>
      </li>
    </ul>

    <XPagination v-if="rows.length" class="ms-pager" :total="total" :page-no="pageNo" :page-size="pageSize" @change="reload" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import EmptyState from '@/components/common/EmptyState.vue';
import PageState from '@/components/common/PageState.vue';
import SkeletonList from '@/components/common/SkeletonList.vue';
import XButton from '@/components/common/XButton.vue';
import XChip from '@/components/common/XChip.vue';
import XPagination from '@/components/common/XPagination.vue';
import type { Favorite } from '@/types/workspace';
import { deleteFavorite, getFavorites } from '@/api/workspace';
import { formatDateTime } from '@/utils/datetime';
import { labelOf } from '@/utils/labels';
import { lawDetailTo, lawSearchTo } from '@/router/navigation';
import { resolveApiError } from '@/utils/apiError';
import { useConfirm } from '@/composables/useConfirm';
import { useToast } from '@/composables/useToast';

const router = useRouter();
const { confirm } = useConfirm();
const toast = useToast();

const rows = ref<Favorite[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 10;
const loading = ref(false);
const error = ref('');
const folder = ref('');
const folders = ref<string[]>([]);

function isLaw(fav: Favorite) {
  return (fav.refType === 'document' || fav.refType === 'law_document') && Boolean(fav.refId);
}

async function reload(next = pageNo.value) {
  pageNo.value = next;
  loading.value = true;
  error.value = '';
  try {
    const res = await getFavorites({ pageNo: pageNo.value, pageSize, folderName: folder.value || undefined });
    rows.value = res.list;
    total.value = res.total;
    // 累积已知文件夹，作为分段筛选项（无独立接口，按所见聚合）
    if (!folder.value) {
      const seen = new Set(folders.value);
      res.list.forEach((f) => f.folderName && seen.add(f.folderName));
      folders.value = [...seen];
    }
  } catch (err) {
    error.value = resolveApiError(err, '收藏读取失败。');
    rows.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function selectFolder(f: string) {
  if (folder.value === f) return;
  folder.value = f;
  reload(1);
}

function openLaw(fav: Favorite) {
  if (fav.refId) router.push(lawDetailTo(fav.refId));
}

function goSearch() {
  router.push(lawSearchTo());
}

async function remove(fav: Favorite) {
  const ok = await confirm({
    title: '取消收藏？',
    message: '该条目将从收藏夹移除。',
    confirmText: '确认移除',
    danger: true
  });
  if (!ok) return;
  try {
    await deleteFavorite(fav.id);
    toast.success('已取消收藏。');
    await reload(pageNo.value);
  } catch (err) {
    toast.error(resolveApiError(err, '操作失败。'));
  }
}

onMounted(() => reload(1));
</script>

<style scoped>
.ms-pane {
  display: grid;
  gap: 16px;
}

.ms-pane__head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.ms-pane__title {
  margin: 0;
  font-family: var(--serif-display);
  font-size: 24px;
  font-weight: 400;
  color: var(--ink);
}

.ms-pane__head p {
  margin: 6px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 14px;
}

.ms-pane__count {
  color: var(--muted);
  font-size: var(--font-xs);
  white-space: nowrap;
}

.ms-folders {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ms-folder {
  height: var(--control-h-sm);
  padding: 0 12px;
  border: 1px solid var(--rule-strong);
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--ink-2);
  font-size: var(--font-xs);
  font-weight: 600;
  cursor: pointer;
}

.ms-folder:hover {
  border-color: var(--ink);
  color: var(--ink);
}

.ms-folder.active {
  border-color: var(--accent);
  background: var(--accent-soft);
  color: var(--accent-deep);
}

.fav-list {
  margin: 0;
  padding: 0;
  list-style: none;
  border-top: 1px solid var(--ink);
}

.fav-item {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px 16px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--rule);
}

.fav-item__title {
  grid-column: 1;
  font-family: var(--serif-body);
  font-size: 16px;
  color: var(--ink);
  text-align: left;
}

.fav-item__title.link {
  border: 0;
  background: transparent;
  cursor: pointer;
}

.fav-item__title.link:hover {
  color: var(--accent-deep);
  text-decoration: underline;
}

.fav-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  grid-column: 1;
}

.fav-item__time {
  color: var(--muted);
  font-size: var(--font-xs);
}

.fav-item__del {
  grid-column: 2;
  grid-row: 1 / span 2;
}
</style>
