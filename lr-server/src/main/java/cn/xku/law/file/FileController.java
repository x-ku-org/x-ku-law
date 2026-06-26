package cn.xku.law.file;

import cn.xku.law.common.annotation.OperLog;
import cn.xku.law.common.result.CommonResult;
import cn.xku.law.file.dto.FilePresignDTO;
import cn.xku.law.file.vo.FileObjectVO;
import cn.xku.law.file.vo.FilePresignVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文件直传 REST API。
 * 流程:presign 申请预签名 URL -> 前端 PUT 直传对象存储 -> complete 确认落库 -> url 取临时访问地址。
 */
@Tag(name = "文件直传", description = "前端直传对象存储:预签名 / 完成确认 / 访问地址")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "申请预签名直传 URL")
    @PostMapping("/presign")
    public CommonResult<FilePresignVO> presign(@Valid @RequestBody FilePresignDTO dto) {
        return CommonResult.success(fileService.presign(dto));
    }

    @Operation(summary = "确认上传完成（HEAD 校验后落库）")
    @OperLog(module = "文件", type = "create")
    @PostMapping("/{id}/complete")
    public CommonResult<FileObjectVO> complete(@PathVariable Long id) {
        return CommonResult.success(fileService.complete(id));
    }

    @Operation(summary = "获取文件临时访问 URL")
    @GetMapping("/{id}/url")
    public CommonResult<String> accessUrl(@PathVariable Long id) {
        return CommonResult.success(fileService.getAccessUrl(id));
    }

    @Operation(summary = "查询文件元信息")
    @GetMapping("/{id}")
    public CommonResult<FileObjectVO> get(@PathVariable Long id) {
        return CommonResult.success(fileService.getFile(id));
    }
}
