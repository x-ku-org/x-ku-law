import { ref, watch, type Ref } from 'vue';
import { getLawArticles, getLawDocument, getLawInterpretation, getLawRelations, getLawVersions, type LawInterpretation } from '@/api/law';
import type { LawArticle, LawDocument, LawRelation, LawVersion } from '@/types/law';
import { resolveApiError } from '@/utils/apiError';
import { setLastLawDocumentId } from '@/utils/recentLaw';

/**
 * 法规详情数据装载：把原本散落在视图 onMounted 里的串并行请求与状态收敛到一处。
 * - documentId 变化：重载文档 + 版本 + 关联，并按生效版本载入条款
 * - selectedVersionId 变化（用户切换版本）：仅重载条款
 */
export function useLawDetail(documentId: Ref<string>, selectedVersionId: Ref<string>) {
  const loading = ref(false);
  const articlesLoading = ref(false);
  const error = ref('');
  const document = ref<LawDocument | null>(null);
  const articles = ref<LawArticle[]>([]);
  const versions = ref<LawVersion[]>([]);
  const relations = ref<LawRelation[]>([]);
  const interpretation = ref<LawInterpretation | null>(null);

  function effectiveVersionId(): string | number | undefined {
    if (selectedVersionId.value) return selectedVersionId.value;
    return document.value?.currentVersionId || versions.value[0]?.id;
  }

  async function loadArticles() {
    const vid = effectiveVersionId();
    const result = await getLawArticles(documentId.value, vid);
    articles.value = result.list;
  }

  /** 解读按版本载入；未生成时后端返回 null。失败不阻断详情页其余内容。 */
  async function loadInterpretation() {
    try {
      interpretation.value = await getLawInterpretation({
        documentId: documentId.value,
        versionId: effectiveVersionId()
      });
    } catch {
      interpretation.value = null;
    }
  }

  async function loadAll() {
    if (!documentId.value) return;
    loading.value = true;
    error.value = '';
    try {
      const doc = await getLawDocument(documentId.value);
      document.value = doc;
      setLastLawDocumentId(doc.id);
      const [versionResult, relationResult] = await Promise.allSettled([
        getLawVersions(documentId.value),
        getLawRelations(documentId.value)
      ]);
      if (versionResult.status === 'fulfilled') versions.value = versionResult.value.list;
      if (relationResult.status === 'fulfilled') relations.value = relationResult.value.list;
      await loadArticles();
      await loadInterpretation();
    } catch (err) {
      error.value = resolveApiError(err, '法规详情读取失败。');
    } finally {
      loading.value = false;
    }
  }

  watch(documentId, loadAll, { immediate: true });

  watch(selectedVersionId, async (next, prev) => {
    if (next === prev || !document.value) return;
    articlesLoading.value = true;
    error.value = '';
    try {
      await loadArticles();
      await loadInterpretation();
    } catch (err) {
      error.value = resolveApiError(err, '条款读取失败。');
    } finally {
      articlesLoading.value = false;
    }
  });

  return { loading, articlesLoading, error, document, articles, versions, relations, interpretation };
}
