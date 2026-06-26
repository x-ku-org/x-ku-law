<template>
  <div class="source-preview">
    <div class="source-preview-toolbar">
      <div>
        <div class="section-kicker">§ Source File</div>
        <p class="source-preview-title">{{ displayName }}</p>
      </div>
      <div class="source-preview-actions">
        <XButton v-if="canOpenInNewTab" size="small" variant="ghost" @click="openSource">新标签打开</XButton>
      </div>
    </div>

    <div v-if="loading && previewKind !== 'docx'" class="source-preview-state">
      <Skeleton width="64%" />
      <Skeleton width="88%" />
      <Skeleton width="76%" />
    </div>

    <PageState v-else-if="error" mode="inline" :error="error" />

    <iframe
      v-else-if="previewUrl && previewKind === 'pdf'"
      class="source-preview-frame"
      :src="previewUrl"
      title="源文件 PDF 预览"
    />

    <div v-else-if="previewKind === 'docx'" class="source-preview-docx-wrap">
      <div v-if="loading" class="source-preview-state">
        <Skeleton width="64%" />
        <Skeleton width="88%" />
        <Skeleton width="76%" />
      </div>
      <div v-show="!loading" ref="docxHost" class="source-preview-docx" />
    </div>

    <div v-else class="source-preview-state">
      <div class="source-preview-mark mono">DOC</div>
      <h3>旧版 Word 暂不能在浏览器内直接预览</h3>
      <p>请在接入流程中生成 PDF 预览件，或上传 `.docx` 文件。当前入口不会自动下载源文件。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { renderAsync } from 'docx-preview';
import { getFileUrl } from '@/api/admin';
import PageState from '@/components/common/PageState.vue';
import Skeleton from '@/components/common/Skeleton.vue';
import XButton from '@/components/common/XButton.vue';
import { resolveApiError } from '@/utils/apiError';

const props = withDefaults(
  defineProps<{
    open: boolean;
    fileId?: number | string | null;
    sourceUrl?: string;
    officialUrl?: string;
    title?: string;
  }>(),
  {
    fileId: null,
    sourceUrl: '',
    officialUrl: '',
    title: ''
  }
);

const loading = ref(false);
const error = ref('');
const previewUrl = ref('');
const docxHost = ref<HTMLElement | null>(null);

const displayName = computed(() => props.title || '法规源文件');
const fallbackUrl = computed(() => props.sourceUrl || props.officialUrl || '');
const sourceKey = computed(() => `${props.fileId || ''}|${fallbackUrl.value}`);

const extension = computed(() => {
  const url = previewUrl.value || fallbackUrl.value;
  const path = url.split('?')[0]?.toLowerCase() || '';
  const match = path.match(/\.([a-z0-9]+)$/);
  return match?.[1] || '';
});

const previewKind = computed<'pdf' | 'docx' | 'doc' | 'unknown'>(() => {
  const ext = extension.value;
  if (ext === 'pdf') return 'pdf';
  if (ext === 'docx') return 'docx';
  if (ext === 'doc') return 'doc';
  return 'unknown';
});

const canOpenInNewTab = computed(() => previewKind.value === 'pdf' && Boolean(previewUrl.value));

watch(
  () => [props.open, sourceKey.value],
  async ([open]) => {
    if (!open) return;
    await loadPreview();
  },
  { immediate: true }
);

async function loadPreview() {
  loading.value = true;
  error.value = '';
  previewUrl.value = '';
  clearDocx();

  try {
    const url = props.fileId ? await getFileUrl(props.fileId) : fallbackUrl.value;
    if (!url) {
      error.value = '当前版本没有可预览的源文件。';
      return;
    }

    previewUrl.value = url;

    if (previewKind.value === 'docx') {
      await nextTick();
      await renderDocx(url);
    }
  } catch (err) {
    error.value = resolveApiError(err, '源文件预览加载失败。');
  } finally {
    loading.value = false;
  }
}

async function renderDocx(url: string) {
  if (!docxHost.value) return;
  clearDocx();
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error('Word 文件读取失败。');
  }
  const blob = await response.blob();
  await renderAsync(blob, docxHost.value, undefined, {
    className: 'docx',
    inWrapper: true,
    ignoreWidth: true,
    ignoreHeight: false,
    breakPages: true
  });
  harmonizeDocxStyles(docxHost.value);
}

function harmonizeDocxStyles(host: HTMLElement) {
  host.querySelectorAll<HTMLElement>('[style*="background"]').forEach((el) => {
    const bg = el.style.backgroundColor || el.style.background;
    if (isWhiteBackground(bg)) {
      el.style.removeProperty('background');
      el.style.removeProperty('background-color');
    }
  });

  host.querySelectorAll<HTMLElement>('.docx-wrapper, section.docx, section.docx > article').forEach((el) => {
    el.style.setProperty('background', 'transparent', 'important');
    el.style.setProperty('background-color', 'transparent', 'important');
    el.style.setProperty('box-shadow', 'none', 'important');
  });

  const wrapper = host.querySelector<HTMLElement>('.docx-wrapper');
  if (wrapper) {
    wrapper.style.setProperty('padding', '0', 'important');
    wrapper.style.setProperty('align-items', 'stretch', 'important');
  }

  host.querySelectorAll<HTMLElement>('section.docx').forEach((el) => {
    el.style.setProperty('width', '100%', 'important');
    el.style.setProperty('max-width', '100%', 'important');
    el.style.setProperty('margin', '0 0 20px', 'important');
    el.style.setProperty('padding-left', '32px', 'important');
    el.style.setProperty('padding-right', '32px', 'important');
  });

  const styleId = 'source-preview-docx-theme';
  host.querySelector(`#${styleId}`)?.remove();
  const style = document.createElement('style');
  style.id = styleId;
  style.textContent = `
    .source-preview-docx .docx-wrapper {
      background: transparent !important;
      padding: 0 !important;
      align-items: stretch !important;
    }
    .source-preview-docx .docx-wrapper > section.docx,
    .source-preview-docx section.docx {
      width: 100% !important;
      max-width: 100% !important;
      margin: 0 0 20px !important;
      padding-left: 32px !important;
      padding-right: 32px !important;
      background: transparent !important;
      box-shadow: none !important;
    }
    .source-preview-docx section.docx > article,
    .source-preview-docx section.docx > header,
    .source-preview-docx section.docx > footer {
      background: transparent !important;
    }
  `;
  host.appendChild(style);
}

function isWhiteBackground(value: string) {
  const normalized = value.toLowerCase().replace(/\s/g, '');
  return (
    normalized === 'white' ||
    normalized === '#fff' ||
    normalized === '#ffffff' ||
    normalized === 'rgb(255,255,255)' ||
    normalized === 'rgba(255,255,255,1)'
  );
}

function clearDocx() {
  if (docxHost.value) {
    docxHost.value.innerHTML = '';
  }
}

function openSource() {
  if (!previewUrl.value) return;
  window.open(previewUrl.value, '_blank', 'noopener,noreferrer');
}
</script>

<style scoped>
.source-preview {
  display: grid;
  gap: 16px;
  min-height: min(70vh, 760px);
}

.source-preview-toolbar {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.source-preview-title {
  margin: 6px 0 0;
  color: var(--ink-2);
  font-family: var(--serif-body);
  font-size: 15px;
  line-height: 1.5;
}

.source-preview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
}

.source-preview-frame {
  width: 100%;
  min-height: min(68vh, 720px);
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.source-preview-docx-wrap {
  min-height: min(68vh, 720px);
  overflow: auto;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.source-preview-docx {
  min-width: 0;
  padding: 16px 20px;
  background: var(--paper-2);
}

.source-preview-docx :deep(.docx-wrapper) {
  padding: 0 !important;
  background: transparent !important;
  align-items: stretch !important;
}

.source-preview-docx :deep(.docx-wrapper > section.docx),
.source-preview-docx :deep(section.docx) {
  width: 100% !important;
  max-width: 100% !important;
  margin: 0 0 20px !important;
  padding-left: 32px !important;
  padding-right: 32px !important;
  box-shadow: none !important;
  background: transparent !important;
}

.source-preview-docx :deep(section.docx > article),
.source-preview-docx :deep(section.docx > footer),
.source-preview-docx :deep(section.docx > header) {
  background: transparent !important;
}

.source-preview-state {
  display: grid;
  place-items: center;
  align-content: center;
  gap: 14px;
  min-height: min(58vh, 560px);
  padding: 36px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
  text-align: center;
}

.source-preview-state h3 {
  margin: 0;
  color: var(--ink);
  font-family: var(--serif-display);
  font-size: 28px;
  font-style: italic;
  font-weight: 400;
}

.source-preview-state p {
  max-width: 54ch;
  margin: 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  line-height: 1.65;
}

.source-preview-mark {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border: 1px solid var(--ink);
  border-radius: 4px;
  color: var(--ink);
  font-size: var(--font-xs);
  letter-spacing: 0.08em;
}

@media (max-width: 760px) {
  .source-preview-toolbar {
    display: grid;
  }

  .source-preview-actions {
    justify-content: flex-start;
  }
}
</style>
