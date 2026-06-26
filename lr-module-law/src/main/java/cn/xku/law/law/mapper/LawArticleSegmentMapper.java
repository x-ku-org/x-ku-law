package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.LawArticleSegmentDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** lr_law_article_segment 数据访问层 */
@Mapper
public interface LawArticleSegmentMapper extends BaseMapper<LawArticleSegmentDO> {

    /**
     * 物理删除某版本的全部分片。分段阶段重跑前清场用（同 {@link LawArticleMapper#physicalDeleteByVersion}，
     * 须物理删除以释放 uk_article_segment 唯一键）。lr_law_article_segment 在租户白名单内，不受租户过滤。
     */
    @Delete("DELETE FROM lr_law_article_segment WHERE version_id = #{versionId}")
    int physicalDeleteByVersion(@Param("versionId") Long versionId);
}
