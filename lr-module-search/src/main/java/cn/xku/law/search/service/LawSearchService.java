package cn.xku.law.search.service;

import cn.xku.law.search.domain.dto.LawSearchQueryDTO;
import cn.xku.law.search.domain.vo.LawSearchPageVO;

public interface LawSearchService {
    LawSearchPageVO search(LawSearchQueryDTO query);
}
