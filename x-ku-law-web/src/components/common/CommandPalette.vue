<template>
  <XModal
    :open="open"
    kicker="§ Command"
    title="快速跳转"
    description="输入关键词筛选页面，或选择下方入口。"
    max-width="640px"
    @update:open="emit('update:open', $event)"
  >
    <div class="command-search">
      <Search :size="15" />
      <input v-model="filterText" class="command-input" placeholder="筛选页面…" autofocus />
      <kbd class="kbd">{{ commandHint }}</kbd>
    </div>
    <div class="command-list">
      <button v-for="item in filteredItems" :key="item.label" type="button" class="command-item" @click="go(item.to)">
        <component :is="item.icon" :size="16" />
        <span class="command-copy">
          <strong>{{ item.label }}</strong>
          <small>{{ item.description }}</small>
        </span>
      </button>
      <p v-if="!filteredItems.length" class="command-empty mono">无匹配页面</p>
    </div>
  </XModal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter, type RouteLocationRaw } from 'vue-router';
import { Bell, Bot, Bookmark, FileSearch, Home, Layers3, LayoutDashboard, MessageSquare, RefreshCcw, Scale, Search, UploadCloud, Workflow } from '@lucide/vue';
import XModal from '@/components/common/XModal.vue';
import { useAuthStore } from '@/stores/auth';

interface CommandItem {
  label: string;
  description: string;
  to: RouteLocationRaw;
  icon: object;
}

const props = defineProps<{
  open: boolean;
  recentLawId?: string | null;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
}>();

const router = useRouter();
const auth = useAuthStore();
const filterText = ref('');

const commandHint = computed(() => (window.navigator.platform.toLowerCase().includes('mac') ? '⌘K' : 'Ctrl+K'));

const items = computed<CommandItem[]>(() => {
  const recentLaw: CommandItem[] = props.recentLawId
    ? [
        {
          label: '最近阅读',
          description: '继续阅读上次打开的法规正文。',
          to: { name: 'law.detail', params: { documentId: props.recentLawId } },
          icon: Scale
        },
        {
          label: '最近对比',
          description: '继续查看最近阅读法规的版本对比。',
          to: { name: 'law.compare', params: { documentId: props.recentLawId } },
          icon: Layers3
        }
      ]
    : [];

  const baseItems: CommandItem[] = [
    { label: '首页', description: '回到检索门户首页。', to: { name: 'app.home' }, icon: Home },
    { label: '工作台', description: '回到工作台概览。', to: { name: 'app.workbench' }, icon: LayoutDashboard },
    { label: '法规检索', description: '按关键词、文号或机关检索法规。', to: { name: 'law.search' }, icon: FileSearch },
    ...recentLaw,
    { label: 'AI 会话', description: '查看历史 AI 会话与消息。', to: { name: 'ai.chat' }, icon: Bot },
    { label: '预警中心', description: '查看订阅命中与预警消息。', to: { name: 'app.alerts' }, icon: Bell },
    { label: '消息中心', description: '查看系统通知收件箱。', to: { name: 'app.messages' }, icon: MessageSquare },
    { label: '收藏夹', description: '管理已收藏的法规。', to: { name: 'app.favorites' }, icon: Bookmark }
  ];

  if (!auth.isAdmin) {
    return baseItems;
  }

  return [
    ...baseItems,
    { label: '上传接入', description: '上传法规文件并发起接入。', to: { name: 'admin.ingest' }, icon: UploadCloud },
    { label: '采集运维', description: '手动触发采集扫描、重置卡住记录。', to: { name: 'admin.ops.collect' }, icon: RefreshCcw },
    { label: '处理管线', description: '查看法规处理任务并重试失败项。', to: { name: 'admin.ops.process' }, icon: Workflow }
  ];
});

const filteredItems = computed(() => {
  const q = filterText.value.trim().toLowerCase();
  if (!q) return items.value;
  return items.value.filter(
    (item) => item.label.toLowerCase().includes(q) || item.description.toLowerCase().includes(q)
  );
});

watch(
  () => props.open,
  (open) => {
    if (!open) filterText.value = '';
  }
);

function go(to: RouteLocationRaw) {
  emit('update:open', false);
  router.push(to);
}
</script>

<style scoped>
.command-search {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  height: var(--control-h);
  padding: 0 12px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.command-search:focus-within {
  border-color: var(--ink);
  background: var(--paper);
}

.command-input {
  flex: 1;
  min-width: 0;
  border: 0;
  background: transparent;
  outline: none;
  color: var(--ink);
  font-size: 13px;
}

.command-list {
  display: grid;
  gap: 10px;
}

.command-item {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 12px;
  align-items: start;
  padding: 14px 16px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper);
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.15s var(--ease);
}

.command-item:hover {
  border-color: var(--ink);
}

.command-copy {
  display: grid;
  gap: 2px;
}

.command-copy strong {
  font-size: 14px;
  font-weight: 600;
}

.command-copy small {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.45;
}

.command-empty {
  margin: 0;
  padding: 12px 0;
  text-align: center;
  color: var(--muted);
  font-size: 12px;
}
</style>
