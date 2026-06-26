package cn.xku.law.law.controller;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.law.diff.VersionDiffResult;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.service.CompareRecordService;
import cn.xku.law.law.service.LawVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版本逐条对比查询。登录用户可读（与法规详情同级）。命中管线已算好的系统对比记录直接返回，
 * 否则实时计算并落库。
 */
@Tag(name = "法规版本对比")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/compare")
@RequiredArgsConstructor
public class LawCompareController {

    private final CompareRecordService compareRecordService;
    private final LawVersionService lawVersionService;

    @Operation(summary = "逐条对比两个法规版本")
    @GetMapping
    public CommonResult<VersionDiffResult> compare(
            @RequestParam Long baseVersionId,
            @RequestParam Long targetVersionId) {
        LawVersionDO target = lawVersionService.getById(targetVersionId);
        LawVersionDO base = lawVersionService.getById(baseVersionId);
        if (target == null || base == null) {
            throw new AppException(ErrorCode.LAW_VERSION_NOT_FOUND);
        }
        VersionDiffResult diff = compareRecordService.getOrComputeDiff(
                target.getDocumentId(), baseVersionId, targetVersionId);
        return CommonResult.success(diff);
    }
}
