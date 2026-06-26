package cn.xku.law.process.stage;

import cn.xku.law.law.domain.LawAiTaskDO;
import cn.xku.law.law.mapper.LawAiTaskMapper;
import cn.xku.law.process.LawProcessingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** AiTaskEnqueueStage 单测：开关关闭不入队 / 开启且无在途任务则入队 / 已有在途任务则去重跳过。 */
class AiTaskEnqueueStageTest {

    private LawAiTaskMapper mapper;
    private AiTaskEnqueueStage stage;

    @BeforeEach
    void setUp() {
        mapper = mock(LawAiTaskMapper.class);
        stage = new AiTaskEnqueueStage(mapper);
    }

    private LawProcessingContext ctx() {
        return new LawProcessingContext(1L, 10L, 100L, 5L);
    }

    @Test
    void disabled_doesNotEnqueue() {
        ReflectionTestUtils.setField(stage, "aiEnabled", false);
        stage.process(ctx());
        verify(mapper, never()).insert(any(LawAiTaskDO.class));
    }

    @Test
    void enabledNoActive_enqueues() {
        ReflectionTestUtils.setField(stage, "aiEnabled", true);
        when(mapper.countActiveByVersion(anyLong())).thenReturn(0);
        stage.process(ctx());
        verify(mapper).insert(any(LawAiTaskDO.class));
    }

    @Test
    void enabledWithActive_skips() {
        ReflectionTestUtils.setField(stage, "aiEnabled", true);
        when(mapper.countActiveByVersion(anyLong())).thenReturn(1);
        stage.process(ctx());
        verify(mapper, never()).insert(any(LawAiTaskDO.class));
    }
}
