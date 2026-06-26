package cn.xku.law.process.stage;

import cn.xku.law.law.diff.ArticleDiffService;
import cn.xku.law.law.diff.VersionDiffResult;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.service.CompareRecordService;
import cn.xku.law.law.service.LawVersionService;
import cn.xku.law.process.LawProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** ChangeAnalysisStage 单测：首版跳过 / 有上一版时 diff→落库→写 diffSummary（Mockito，无 Spring）。 */
class ChangeAnalysisStageTest {

    private LawVersionService versionService;
    private ArticleDiffService diffService;
    private CompareRecordService compareRecordService;
    private ChangeAnalysisStage stage;

    @BeforeEach
    void setUp() {
        versionService = mock(LawVersionService.class);
        diffService = mock(ArticleDiffService.class);
        compareRecordService = mock(CompareRecordService.class);
        stage = new ChangeAnalysisStage(versionService, diffService, compareRecordService);
    }

    private LawProcessingContext ctx(long versionId) {
        return new LawProcessingContext(1L, 10L, versionId, null);
    }

    @Test
    void firstVersion_noPrevious_skips() {
        LawVersionDO current = new LawVersionDO();
        current.setId(100L);
        current.setDocumentId(10L);
        current.setPublishDate(LocalDate.of(2020, 1, 1));
        when(versionService.getById(100L)).thenReturn(current);
        when(versionService.getOne(any())).thenReturn(null); // 无上一版

        stage.process(ctx(100L));

        verify(diffService, never()).diff(any(), any());
        verify(compareRecordService, never()).saveSystemDiff(any(), any());
        verify(versionService, never()).updateById(any());
    }

    @Test
    void hasPrevious_runsDiff_persists_andWritesSummary() {
        LawVersionDO current = new LawVersionDO();
        current.setId(200L);
        current.setDocumentId(10L);
        current.setPublishDate(LocalDate.of(2022, 6, 1));
        LawVersionDO previous = new LawVersionDO();
        previous.setId(150L);
        previous.setDocumentId(10L);

        when(versionService.getById(200L)).thenReturn(current);
        when(versionService.getOne(any())).thenReturn(previous);

        VersionDiffResult diff = new VersionDiffResult();
        diff.setBaseVersionId(150L);
        diff.setTargetVersionId(200L);
        diff.setChangeCount(2);
        diff.setSummary("新增1条、修改1条");
        when(diffService.diff(150L, 200L)).thenReturn(diff);

        stage.process(ctx(200L));

        verify(diffService, times(1)).diff(150L, 200L);
        verify(compareRecordService, times(1)).saveSystemDiff(eq(10L), eq(diff));

        ArgumentCaptor<LawVersionDO> captor = ArgumentCaptor.forClass(LawVersionDO.class);
        verify(versionService).updateById(captor.capture());
        assertThat(captor.getValue().getDiffSummary()).isEqualTo("新增1条、修改1条");
    }

    @Test
    void versionNotFound_skips() {
        when(versionService.getById(999L)).thenReturn(null);
        stage.process(ctx(999L));
        verify(diffService, never()).diff(any(), any());
    }
}
