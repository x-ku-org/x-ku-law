package cn.xku.law.search.controller;

import cn.xku.law.common.result.CommonResult;
import cn.xku.law.search.domain.dto.LawSearchQueryDTO;
import cn.xku.law.search.domain.vo.LawSearchPageVO;
import cn.xku.law.search.service.LawSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "法规全文检索")
@RestController
@RequestMapping("/search/laws")
@RequiredArgsConstructor
public class LawSearchController {

    private final LawSearchService lawSearchService;

    @Operation(summary = "全文检索法规（需登录，返回本租户可见范围内的法规）")
    @GetMapping
    public CommonResult<LawSearchPageVO> search(@Valid LawSearchQueryDTO query) {
        return CommonResult.success(lawSearchService.search(query));
    }
}
