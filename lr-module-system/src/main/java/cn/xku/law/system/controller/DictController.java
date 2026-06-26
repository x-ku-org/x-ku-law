package cn.xku.law.system.controller;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.domain.dto.*;
import cn.xku.law.system.domain.vo.DictDataVO;
import cn.xku.law.system.domain.vo.DictTypeVO;
import cn.xku.law.system.service.DictDataService;
import cn.xku.law.system.service.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "字典管理")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;

    // ===== 字典类型 =====

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/types")
    public CommonResult<PageResult<DictTypeVO>> pageTypes(@Valid DictTypeQueryDTO query) {
        return CommonResult.success(dictTypeService.pageDictTypes(query));
    }

    @Operation(summary = "新建字典类型")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperLog(module = "字典管理", type = "create")
    @PostMapping("/types")
    public CommonResult<Long> createType(@Valid @RequestBody DictTypeCreateDTO dto) {
        return CommonResult.success(dictTypeService.createDictType(dto));
    }

    @Operation(summary = "更新字典类型")
    @PreAuthorize("hasAuthority('system:dict:update')")
    @OperLog(module = "字典管理", type = "update")
    @PutMapping("/types/{id}")
    public CommonResult<?> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeCreateDTO dto) {
        dictTypeService.updateDictType(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典管理", type = "delete")
    @DeleteMapping("/types/{id}")
    public CommonResult<?> removeType(@PathVariable Long id) {
        dictTypeService.removeDictType(id);
        return CommonResult.success();
    }

    // ===== 字典数据 =====

    @Operation(summary = "分页查询字典数据")
    @GetMapping("/data")
    public CommonResult<PageResult<DictDataVO>> pageData(@Valid DictDataQueryDTO query) {
        return CommonResult.success(dictDataService.pageDictData(query));
    }

    @Operation(summary = "按字典编码查询全部数据（前端下拉用）")
    @GetMapping("/data/list")
    public CommonResult<List<DictDataVO>> listByCode(@RequestParam String dictCode) {
        return CommonResult.success(dictDataService.listByDictCode(dictCode));
    }

    @Operation(summary = "批量按字典编码查询（前端一次性加载词表用）")
    @GetMapping("/data/batch")
    public CommonResult<Map<String, List<DictDataVO>>> batchByCodes(@RequestParam List<String> codes) {
        return CommonResult.success(dictDataService.listByDictCodes(codes));
    }

    @Operation(summary = "新建字典数据")
    @PreAuthorize("hasAuthority('system:dict:create')")
    @OperLog(module = "字典管理", type = "create")
    @PostMapping("/data")
    public CommonResult<Long> createData(@Valid @RequestBody DictDataCreateDTO dto) {
        return CommonResult.success(dictDataService.createDictData(dto));
    }

    @Operation(summary = "更新字典数据")
    @PreAuthorize("hasAuthority('system:dict:update')")
    @OperLog(module = "字典管理", type = "update")
    @PutMapping("/data/{id}")
    public CommonResult<?> updateData(@PathVariable Long id, @Valid @RequestBody DictDataCreateDTO dto) {
        dictDataService.updateDictData(id, dto);
        return CommonResult.success();
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @OperLog(module = "字典管理", type = "delete")
    @DeleteMapping("/data/{id}")
    public CommonResult<?> removeData(@PathVariable Long id) {
        dictDataService.removeDictData(id);
        return CommonResult.success();
    }
}
