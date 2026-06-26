package cn.xku.law.search.mapper;

import cn.xku.law.search.domain.SearchHistoryDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** lr_search_history 数据访问层 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistoryDO> {

    /**
     * 热点检索：统计 since 之后各关键词的检索次数，按热度倒序取前 N。
     * 返回行包含 {@code keyword}（String）与 {@code heat}（Long，检索次数）。
     */
    @Select("SELECT keyword AS keyword, COUNT(*) AS heat FROM lr_search_history " +
            "WHERE deleted=0 AND keyword IS NOT NULL AND keyword <> '' AND search_time >= #{since} " +
            "GROUP BY keyword ORDER BY heat DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTrending(@Param("since") LocalDateTime since, @Param("limit") int limit);
}
