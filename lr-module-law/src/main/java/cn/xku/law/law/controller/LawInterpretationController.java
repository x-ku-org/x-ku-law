package cn.xku.law.law.controller;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawInterpretationDO;
import cn.xku.law.law.service.LawDocumentService;
import cn.xku.law.law.service.LawInterpretationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 法规 AI 解读查询。登录用户可读（与法规详情同级）。
 * 传 versionId 取指定版本解读；只传 documentId 时取该文档现行版的解读。
 * 解读由 AI 旁路（InterpretationStage）异步生成；尚未生成时返回 null，前端据此提示「生成中/暂无」。
 */
@Tag(name = "法规解读")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/interpretation")
@RequiredArgsConstructor
public class LawInterpretationController {

    private final LawInterpretationService interpretationService;
    private final LawDocumentService lawDocumentService;

    @Operation(summary = "查询法规解读", description = "传 versionId 取指定版本；否则取 documentId 的现行版")
    @GetMapping
    public CommonResult<LawInterpretationDO> get(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Long versionId) {
        Long targetVersionId = versionId;
        if (targetVersionId == null) {
            if (documentId == null) {
                throw new AppException(ErrorCode.PARAM_ERROR, "documentId 与 versionId 不能同时为空");
            }
            LawDocumentDO doc = lawDocumentService.getById(documentId);
            if (doc == null) {
                throw new AppException(ErrorCode.LAW_DOCUMENT_NOT_FOUND);
            }
            targetVersionId = doc.getCurrentVersionId();
            if (targetVersionId == null) {
                return CommonResult.success(null);
            }
        }
        return CommonResult.success(interpretationService.getByVersionId(targetVersionId));
    }
}
