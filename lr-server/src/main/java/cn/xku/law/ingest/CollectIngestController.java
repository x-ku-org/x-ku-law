package cn.xku.law.ingest;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采集接入手动触发 API。仅在 {@code app.collect.enabled=true} 且 Bean 已创建时可执行扫描。
 */
@Tag(name = "采集接入", description = "手动触发 MinIO 采集批次扫描与接入")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/collect/ingest")
@RequiredArgsConstructor
public class CollectIngestController {

    private final ObjectProvider<CollectIngestProcessor> processorProvider;

    @Operation(summary = "手动触发采集扫描",
            description = "立即扫描 MinIO 中的 fglaw*/gblaw* 运行文件夹并接入。sourceCode 可选：flk / gb；batchSize 可临时覆盖单次处理的文件夹数。")
    @PreAuthorize("hasAuthority('collect:ingest:trigger')")
    @OperLog(module = "采集接入", type = "trigger")
    @PostMapping("/scan")
    public CommonResult<CollectIngestScanResult> triggerScan(
            @RequestParam(required = false) String sourceCode,
            @RequestParam(required = false) Integer batchSize) {
        return CommonResult.success(requireProcessor().scanAndIngest(sourceCode, batchSize));
    }

    @Operation(summary = "重置卡住的采集标记",
            description = "将超时仍处于 processing 的 lr_collect_record 重置为 pending，便于下轮重试。")
    @PreAuthorize("hasAuthority('collect:ingest:trigger')")
    @OperLog(module = "采集接入", type = "recover")
    @PostMapping("/recover-stuck")
    public CommonResult<Integer> recoverStuck() {
        return CommonResult.success(requireProcessor().recoverStuckRecordsNow());
    }

    private CollectIngestProcessor requireProcessor() {
        CollectIngestProcessor processor = processorProvider.getIfAvailable();
        if (processor == null) {
            throw new AppException(ErrorCode.PARAM_ERROR,
                    "采集接入未启用，请设置 app.collect.enabled=true 后重启");
        }
        return processor;
    }
}
