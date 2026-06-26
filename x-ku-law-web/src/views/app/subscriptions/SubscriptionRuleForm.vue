<template>
  <XModal
    :open="open"
    :title="rule ? '编辑订阅规则' : '新建订阅规则'"
    kicker="§ Subscription"
    description="按关键词与条件维护法规变更提醒，命中后即时推送到预警中心。"
    max-width="760px"
    @update:open="close"
  >
    <form id="rule-form" class="rule-form" @submit.prevent="submit">
      <fieldset class="grp">
        <legend>规则</legend>
        <div class="grid">
          <XFormField label="规则名称" required :error="errors.ruleName" class="wide">
            <XInput v-model="form.ruleName" placeholder="如：环保领域法规变更" :invalid="Boolean(errors.ruleName)" />
          </XFormField>
          <XFormField label="规则类型">
            <XSelect v-model="form.ruleType" :options="ruleTypeOptions" />
          </XFormField>
          <XFormField label="状态">
            <XSelect v-model="form.status" :options="statusOptions" />
          </XFormField>
          <XFormField label="关键词" hint="命中标题/摘要包含任一关键词，多个用空格或逗号分隔" class="wide">
            <XInput v-model="form.keyword" placeholder="如：环境保护 排污 大气" />
          </XFormField>
          <XFormField label="送达渠道">
            <XSelect v-model="form.deliveryChannel" :options="deliveryChannelOptions" />
          </XFormField>
        </div>
      </fieldset>

      <fieldset class="grp">
        <legend>条件过滤（可选，缺省不限）</legend>
        <div class="grid">
          <XFormField label="效力级别" hint="仅匹配该效力级别的法规">
            <XSelect v-model="filters.effectLevel" :options="effectLevelWithAll" placeholder="不限" />
          </XFormField>
          <XFormField label="适用地区" hint="仅匹配该地区的地方性法规/规章">
            <XSelect v-model="filters.regionCode" :options="regionWithAll" placeholder="不限" />
          </XFormField>
          <XFormField label="发布机关" hint="发布机关包含该词即命中，如「国务院」">
            <XInput v-model="filters.authority" placeholder="不限" />
          </XFormField>
        </div>
      </fieldset>
    </form>

    <template #footer>
      <span v-if="message" class="form-message">{{ message }}</span>
      <XButton type="button" variant="ghost" @click="close">取消</XButton>
      <XButton variant="primary" type="submit" form="rule-form" :loading="submitting">保存</XButton>
    </template>
  </XModal>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import XButton from '@/components/common/XButton.vue';
import XFormField from '@/components/common/XFormField.vue';
import XInput from '@/components/common/XInput.vue';
import XModal from '@/components/common/XModal.vue';
import XSelect from '@/components/common/XSelect.vue';
import type { OptionItem } from '@/types/api';
import type { SubscriptionRule } from '@/types/workspace';
import { createSubscriptionRule, updateSubscriptionRule } from '@/api/workspace';
import { composeSubscriptionPayload, ruleFiltersToForm } from '@/utils/subscriptionRule';
import { effectLevelOptions, regionOptions } from '@/utils/labels';
import { resolveApiError } from '@/utils/apiError';
import { useToast } from '@/composables/useToast';

const props = defineProps<{ open: boolean; rule: SubscriptionRule | null }>();
const emit = defineEmits<{ 'update:open': [value: boolean]; saved: [] }>();
const toast = useToast();

const ruleTypeOptions: OptionItem[] = [{ label: '法规变更', value: 'law_update' }];
const statusOptions: OptionItem[] = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];
const deliveryChannelOptions: OptionItem[] = [
  { label: '站内信', value: 'station' },
  { label: '邮件（暂未开通）', value: 'email' }
];
const allOption: OptionItem = { label: '不限', value: '' };
const effectLevelWithAll: OptionItem[] = [allOption, ...effectLevelOptions];
const regionWithAll: OptionItem[] = [allOption, ...regionOptions];

const form = reactive({
  ruleName: '',
  ruleType: 'law_update',
  keyword: '',
  deliveryChannel: 'station',
  status: 'enabled'
});
const filters = reactive({ effectLevel: '', regionCode: '', authority: '' });
const errors = reactive<{ ruleName?: string }>({});
const message = ref('');
const submitting = ref(false);

watch(
  () => props.open,
  (open) => {
    if (!open) return;
    message.value = '';
    errors.ruleName = '';
    const r = props.rule;
    form.ruleName = r?.ruleName ?? '';
    form.ruleType = r?.ruleType || 'law_update';
    form.keyword = r?.keyword ?? '';
    form.deliveryChannel = r?.deliveryChannel || 'station';
    form.status = r?.status || 'enabled';
    const f = ruleFiltersToForm(r?.filtersJson);
    filters.effectLevel = f.effectLevel;
    filters.regionCode = f.regionCode;
    filters.authority = f.authority;
  }
);

function close() {
  emit('update:open', false);
}

async function submit() {
  errors.ruleName = '';
  if (!form.ruleName.trim()) {
    errors.ruleName = '规则名称不能为空。';
    return;
  }
  const payload = composeSubscriptionPayload({ ...form, ruleName: form.ruleName.trim() }, { ...filters });
  submitting.value = true;
  try {
    if (props.rule) {
      await updateSubscriptionRule(props.rule.id, payload);
      toast.success('规则已更新。');
    } else {
      await createSubscriptionRule(payload);
      toast.success('规则已创建。');
    }
    emit('saved');
    close();
  } catch (err) {
    message.value = resolveApiError(err, '保存失败。');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.rule-form {
  display: grid;
  gap: 18px;
}

.grp {
  margin: 0;
  padding: 0;
  border: 0;
}

.grp + .grp {
  padding-top: 16px;
  border-top: 1px solid var(--rule);
}

.grp legend {
  margin-bottom: 12px;
  padding: 0;
  color: var(--muted);
  font-family: var(--sans);
  font-size: var(--font-xxs);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 20px;
}

.wide {
  grid-column: 1 / -1;
}

.form-message {
  margin-right: auto;
  color: var(--rose);
  font-size: 12px;
}

@media (max-width: 860px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
