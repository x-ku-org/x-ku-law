package cn.xku.law.law.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawDocumentQueryDTO;
import cn.xku.law.law.domain.vo.LawDocumentVO;
import cn.xku.law.law.service.LawDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 法规文件 REST API（完整示例，作为各业务模块开发模板） */
@Tag(name = "法规文件", description = "法规主数据增删改查")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/law/documents")
@RequiredArgsConstructor
public class LawDocumentController {

    private final LawDocumentService lawDocumentService;

    @Operation(summary = "分页查询法规列表")
    @GetMapping
    public CommonResult<PageResult<LawDocumentVO>> page(@Valid LawDocumentQueryDTO query) {
        return CommonResult.success(lawDocumentService.pageDocuments(query));
    }

    @Operation(summary = "查询法规详情")
    @GetMapping("/{id}")
    public CommonResult<LawDocumentVO> get(@PathVariable Long id) {
        return CommonResult.success(lawDocumentService.getDocumentById(id));
    }

    @Operation(summary = "新建法规")
    @PreAuthorize("hasAuthority('law:document:create')")
    @OperLog(module = "法规管理", type = "create")
    @PostMapping
    public CommonResult<Long> create(@Valid @RequestBody LawDocumentCreateDTO dto) {
        return CommonResult.success(lawDocumentService.createDocument(dto));
    }

    @Operation(summary = "更新法规元信息")
    @PreAuthorize("hasAuthority('law:document:update')")
    @OperLog(module = "法规管理", type = "update")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id,
                                  @Valid @RequestBody LawDocumentCreateDTO dto) {
        lawDocumentService.updateDocument(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除法规（逻辑删除）")
    @PreAuthorize("hasAuthority('law:document:delete')")
    @OperLog(module = "法规管理", type = "delete")
    @DeleteMapping("/{id}")
    public CommonResult<?> remove(@PathVariable Long id) {
        lawDocumentService.removeDocument(id);
        return CommonResult.success();
    }
}
