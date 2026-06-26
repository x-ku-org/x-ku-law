package cn.xku.law.law.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.convert.LawArticleConvert;
import cn.xku.law.law.domain.LawArticleDO;
import cn.xku.law.law.domain.dto.LawArticleCreateDTO;
import cn.xku.law.law.domain.dto.LawArticleQueryDTO;
import cn.xku.law.law.domain.vo.LawArticleVO;
import cn.xku.law.law.mapper.LawArticleMapper;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.service.LawArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LawArticleServiceImpl extends ServiceImpl<LawArticleMapper, LawArticleDO>
        implements LawArticleService {

    private final LawArticleConvert convert;
    private final LawDocumentMapper lawDocumentMapper;

    @Override
    public PageResult<LawArticleVO> pageArticles(LawArticleQueryDTO query) {
        LambdaQueryWrapper<LawArticleDO> wrapper = new LambdaQueryWrapper<LawArticleDO>()
                .eq(query.getDocumentId() != null, LawArticleDO::getDocumentId, query.getDocumentId())
                .eq(query.getVersionId() != null, LawArticleDO::getVersionId, query.getVersionId())
                .eq(query.getParentArticleId() != null, LawArticleDO::getParentArticleId, query.getParentArticleId())
                .like(StringUtils.hasText(query.getKeyword()), LawArticleDO::getContentText, query.getKeyword())
                .eq(StringUtils.hasText(query.getStatus()), LawArticleDO::getStatus, query.getStatus())
                .eq(query.getObligationFlag() != null, LawArticleDO::getObligationFlag, query.getObligationFlag())
                .eq(query.getPenaltyFlag() != null, LawArticleDO::getPenaltyFlag, query.getPenaltyFlag())
                .orderByAsc(LawArticleDO::getArticleOrder);
        IPage<LawArticleDO> page = this.page(query.toPage(), wrapper);
        List<LawArticleVO> list = convert.toVOList(page.getRecords());
        Map<Long, String> titles = LawTitleHelper.titlesByIds(lawDocumentMapper,
                list.stream().map(LawArticleVO::getDocumentId).collect(Collectors.toList()));
        list.forEach(v -> v.setDocumentTitle(titles.get(v.getDocumentId())));
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public LawArticleVO getArticleById(Long id) {
        LawArticleDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_ARTICLE_NOT_FOUND);
        }
        return convert.toVO(entity);
    }

    @Override
    public Long createArticle(LawArticleCreateDTO dto) {
        LawArticleDO entity = convert.toDO(dto);
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateArticle(Long id, LawArticleCreateDTO dto) {
        LawArticleDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_ARTICLE_NOT_FOUND);
        }
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeArticle(Long id) {
        if (!this.removeById(id)) {
            throw new AppException(ErrorCode.LAW_ARTICLE_NOT_FOUND);
        }
    }
}
