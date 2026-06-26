package cn.xku.law.law.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.domain.dto.LawArticleCreateDTO;
import cn.xku.law.law.domain.dto.LawArticleQueryDTO;
import cn.xku.law.law.domain.vo.LawArticleVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LawArticleService extends IService<LawArticleDO> {

    PageResult<LawArticleVO> pageArticles(LawArticleQueryDTO query);

    LawArticleVO getArticleById(Long id);

    Long createArticle(LawArticleCreateDTO dto);

    void updateArticle(Long id, LawArticleCreateDTO dto);

    void removeArticle(Long id);
}
