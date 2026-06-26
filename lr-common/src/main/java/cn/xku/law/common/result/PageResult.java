package cn.xku.law.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 分页结果封装，由 IPage 转换而来 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private List<T> list;

    /**
     * 本页被服务端二次过滤剔除的条数（默认 0）。
     * 用于检索等“先由外部引擎分页、再回查可信源剔脏”的场景：
     * {@code total} 仍为引擎对全结果集的估计，{@code list.size() + filteredCount ≤ pageSize}。
     */
    private Integer filteredCount = 0;

    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public PageResult(Long total, List<T> list, Integer filteredCount) {
        this.total = total;
        this.list = list;
        this.filteredCount = filteredCount;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }

    public static <T> PageResult<T> of(Long total, List<T> list, Integer filteredCount) {
        return new PageResult<>(total, list, filteredCount);
    }
}
