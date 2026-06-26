package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.LawDocumentDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** lr_law_document 数据访问层，基础 CRUD 由 MyBatis-Plus BaseMapper 提供 */
@Mapper
public interface LawDocumentMapper extends BaseMapper<LawDocumentDO> {

    /** 现行法规收录数：状态为有效（effective）的法规计数，用于首页门户「现行法规收录」统计 */
    @Select("SELECT COUNT(*) FROM lr_law_document WHERE deleted=0 AND status='effective'")
    long countEffective();

    /** 今日法规更新数：发布日期为今天的法规计数 */
    @Select("SELECT COUNT(*) FROM lr_law_document WHERE deleted=0 AND publish_date = CURDATE()")
    long countUpdatedToday();

    /** 效力层级覆盖数：去重的 legal_level 数量 */
    @Select("SELECT COUNT(DISTINCT legal_level) FROM lr_law_document " +
            "WHERE deleted=0 AND legal_level IS NOT NULL AND legal_level <> ''")
    int countDistinctLevel();

    /** 地区规则覆盖数：去重的 region_code 数量 */
    @Select("SELECT COUNT(DISTINCT region_code) FROM lr_law_document " +
            "WHERE deleted=0 AND region_code IS NOT NULL AND region_code <> ''")
    int countDistinctRegion();

    /** 最新收录的有效法规，按发布日期倒序取前 N 条（仅取首页展示所需列） */
    @Select("SELECT id, title, law_type, legal_level, status, publish_date FROM lr_law_document " +
            "WHERE deleted=0 AND status='effective' ORDER BY publish_date DESC, id DESC LIMIT #{limit}")
    List<LawDocumentDO> selectLatest(@Param("limit") int limit);
}
