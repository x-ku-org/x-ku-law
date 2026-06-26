<template>
  <section class="page ingest">
    <header class="ingest-head hairline-strong">
      <div class="section-kicker">§ Upload Ingest</div>
      <h1 class="h1 h1--italic">法规上传接入</h1>
      <p>上传法规原文，填写元信息，系统会创建法规版本并进入处理管线。</p>
    </header>

    <form class="form" @submit.prevent="submit">
      <XFormField label="法规文件" required hint="支持平台允许的文件类型与大小。">
        <input ref="fileInput" class="file" type="file" @change="onFileChange" />
        <span v-if="file" class="file-meta mono">{{ file.name }} · {{ fileSizeLabel }}</span>
      </XFormField>
      <XFormField label="法规标题" required>
        <XInput v-model="form.title" />
      </XFormField>
      <XFormField label="文号">
        <XInput v-model="form.documentNo" />
      </XFormField>
      <XFormField label="法规类型">
        <XSelect v-model="form.lawType" :options="lawTypeOptions" />
      </XFormField>
      <XFormField label="效力层级">
        <XSelect v-model="form.legalLevel" :options="effectLevelOptions" placeholder="请选择层级" />
      </XFormField>
      <XFormField label="发布机关">
        <XInput v-model="form.issuingOrg" />
      </XFormField>
      <XFormField label="适用地区">
        <XSelect v-model="form.regionCode" :options="regionOptions" placeholder="请选择地区" />
      </XFormField>
      <XFormField label="时效状态">
        <XSelect v-model="form.status" :options="statusOptions" />
      </XFormField>
      <XFormField label="公布日期">
        <XInput v-model="form.publishDate" type="date" />
      </XFormField>
      <XFormField label="生效日期">
        <XInput v-model="form.effectiveDate" type="date" />
      </XFormField>
      <XFormField label="官方来源">
        <XInput v-model="form.sourceUrl" />
      </XFormField>
      <div class="actions">
        <span :class="{ error: messageType === 'error' }">{{ message }}</span>
        <RouterLink v-if="lastVersionId" class="version-link" :to="{ name: 'admin.lawVersions' }">前往法规版本 →</RouterLink>
        <XButton variant="primary" type="submit" :loading="loading">上传并接入</XButton>
      </div>
    </form>

    <aside class="ingest-brief">
      <div class="section-kicker">§ Intake path</div>
      <ol>
        <li><span class="mono">01</span><strong>上传原文</strong><small>通过预签名地址写入对象存储。</small></li>
        <li><span class="mono">02</span><strong>创建版本</strong><small>保存法规元信息并关联文件 ID。</small></li>
        <li><span class="mono">03</span><strong>进入管线</strong><small>后续由处理、索引和向量任务接手。</small></li>
      </ol>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { completeFile, ingestUploadedLaw, presignFile } from '@/api/admin';
import XButton from '@/components/common/XButton.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XSelect from '@/components/common/XSelect.vue';
import { useToast } from '@/composables/useToast';
import { resolveApiError } from '@/utils/apiError';
import { effectLevelOptions, lawTypeOptions, regionOptions, timelinessOptions } from '@/utils/labels';

const file = ref<File | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const loading = ref(false);
const message = ref('');
const messageType = ref<'info' | 'error'>('info');
const lastVersionId = ref<number | null>(null);
const toast = useToast();

const defaultForm = () => ({
  title: '',
  documentNo: '',
  lawType: 'regulation',
  legalLevel: '',
  issuingOrg: '',
  regionCode: '',
  status: 'effective',
  publishDate: '',
  effectiveDate: '',
  sourceUrl: ''
});
const form = reactive(defaultForm());
const fileSizeLabel = computed(() => {
  if (!file.value) return '';
  if (file.value.size < 1024 * 1024) return `${Math.max(1, Math.round(file.value.size / 1024))} KB`;
  return `${(file.value.size / 1024 / 1024).toFixed(1)} MB`;
});

function resetForm() {
  Object.assign(form, defaultForm());
  file.value = null;
  if (fileInput.value) fileInput.value.value = '';
}

const statusOptions = [...timelinessOptions];

function onFileChange(event: Event) {
  file.value = (event.target as HTMLInputElement).files?.[0] || null;
}

async function submit() {
  if (!file.value || !form.title) {
    message.value = '请选择文件并填写法规标题。';
    messageType.value = 'error';
    return;
  }
  loading.value = true;
  message.value = '';
  messageType.value = 'info';
  lastVersionId.value = null;
  try {
    message.value = '① 申请上传地址…';
    const presign = await presignFile({
      originalName: file.value.name,
      contentType: file.value.type || 'application/octet-stream',
      fileSize: file.value.size,
      refType: 'law_version'
    });
    message.value = '② 上传原文到对象存储…';
    const uploadResponse = await fetch(presign.uploadUrl, {
      method: presign.method || 'PUT',
      headers: presign.headers || { 'Content-Type': file.value.type || 'application/octet-stream' },
      body: file.value
    });
    if (!uploadResponse.ok) {
      throw new Error('文件上传失败，请稍后重试。');
    }
    message.value = '③ 确认文件并创建版本…';
    const completed = await completeFile(presign.fileId);
    const versionId = await ingestUploadedLaw({ ...form, fileId: completed.id || presign.fileId });
    resetForm();
    lastVersionId.value = versionId;
    message.value = `已接入，版本 ID：${versionId}。可继续上传下一份。`;
    messageType.value = 'info';
    toast.success('法规文件已上传并进入接入流程。');
  } catch (err) {
    message.value = resolveApiError(err, '上传接入失败。');
    messageType.value = 'error';
    toast.error(message.value);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.ingest {
  display: grid;
  grid-template-columns: minmax(0, 720px) minmax(280px, 1fr);
  gap: 24px;
}

header {
  grid-column: 1 / -1;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--ink);
}

header p {
  max-width: 64ch;
  margin: 10px 0 0;
  color: var(--ink-3);
  font-family: var(--serif-body);
  font-size: 16px;
}

.ingest-head {
  padding-bottom: 24px;
  margin-bottom: 8px;
}

.form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 720px;
}

.file {
  width: 100%;
  min-height: var(--control-h);
  padding: 7px 10px;
  border: 1px solid var(--rule-strong);
  border-radius: 4px;
  background: var(--paper);
}

.file-meta {
  color: var(--ink-3);
  font-size: var(--font-xs);
}

.ingest-brief {
  display: grid;
  gap: 18px;
  align-content: start;
  padding: 20px;
  border: 1px solid var(--rule);
  border-radius: 4px;
  background: var(--paper-2);
}

.ingest-brief ol {
  display: grid;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.ingest-brief li {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 4px 12px;
}

.ingest-brief li .mono {
  grid-row: span 2;
  color: var(--muted);
  font-size: 12px;
}

.ingest-brief strong {
  color: var(--ink);
  font-family: var(--serif-body);
  font-size: 15px;
  font-weight: 500;
}

.ingest-brief small {
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.45;
}

.actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: flex-end;
  padding-top: 14px;
  border-top: 1px solid var(--rule);
  color: var(--muted);
}

.error {
  color: var(--rose);
}

.version-link {
  color: var(--accent-deep);
  font-size: 13px;
  text-decoration: none;
}

.version-link:hover {
  color: var(--ink);
}

@media (max-width: 900px) {
  .ingest {
    grid-template-columns: 1fr;
  }

  .form {
    grid-template-columns: 1fr;
  }
}
</style>
