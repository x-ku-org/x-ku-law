package cn.xku.law.process;

import cn.xku.law.law.domain.LawProcessTaskDO;
import cn.xku.law.law.mapper.LawProcessTaskMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** LawProcessTaskProcessor 单测：阶段按 order 升序执行（Mockito，无 Spring）。 */
class LawProcessTaskProcessorTest {

    /** 记录执行顺序的假阶段 */
    private static class RecordingStage implements LawProcessingStage {
        private final String name;
        private final int order;
        private final List<String> log;
        private final boolean ai;

        RecordingStage(String name, int order, List<String> log) {
            this(name, order, log, false);
        }

        RecordingStage(String name, int order, List<String> log, boolean ai) {
            this.name = name;
            this.order = order;
            this.log = log;
            this.ai = ai;
        }

        @Override public String name() { return name; }
        @Override public int order() { return order; }
        @Override public boolean requiresAi() { return ai; }
        @Override public void process(LawProcessingContext ctx) { log.add(name); }
    }

    @Test
    void processPendingTasks_runsStagesInOrder() {
        LawProcessTaskMapper mapper = mock(LawProcessTaskMapper.class);
        LawProcessTaskDO task = new LawProcessTaskDO();
        task.setId(1L);
        task.setVersionId(100L);
        task.setDocumentId(10L);
        task.setRetryCount(0);
        when(mapper.selectList(any())).thenReturn(List.of(task));
        when(mapper.claimTask(eq(1L))).thenReturn(1);

        List<String> callLog = new ArrayList<>();
        // 故意乱序传入；混入一个 AI 阶段，结构化主管线应将其排除
        List<LawProcessingStage> stages = List.of(
                new RecordingStage("publish", 30, callLog),
                new RecordingStage("interpretation", 40, callLog, true),
                new RecordingStage("extract", 10, callLog),
                new RecordingStage("segment", 20, callLog));

        LawProcessTaskProcessor processor = new LawProcessTaskProcessor(mapper, stages, 1);
        processor.processPendingTasks();

        // AI 阶段（interpretation）不在结构化主管线内联执行
        assertThat(callLog).containsExactly("extract", "segment", "publish");
    }

    @Test
    void processPendingTasks_skipsWhenClaimLost() {
        LawProcessTaskMapper mapper = mock(LawProcessTaskMapper.class);
        LawProcessTaskDO task = new LawProcessTaskDO();
        task.setId(2L);
        task.setRetryCount(0);
        when(mapper.selectList(any())).thenReturn(List.of(task));
        when(mapper.claimTask(eq(2L))).thenReturn(0); // 被其它实例抢占

        List<String> callLog = new ArrayList<>();
        LawProcessTaskProcessor processor = new LawProcessTaskProcessor(
                mapper, List.of(new RecordingStage("extract", 10, callLog)), 1);
        processor.processPendingTasks();

        assertThat(callLog).isEmpty();
    }
}
