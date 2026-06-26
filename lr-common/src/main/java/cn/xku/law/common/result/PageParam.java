package cn.xku.law.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 统一分页请求参数，业务 DTO 可继承此类 */
@Data
public class PageParam {

    public static final int PAGE_NO_DEFAULT = 1;
    public static final int PAGE_SIZE_DEFAULT = 10;
    public static final int PAGE_SIZE_MAX = 100;

    @Min(value = 1, message = "页码最小为 1")
    private Integer pageNo = PAGE_NO_DEFAULT;

    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = PAGE_SIZE_MAX, message = "每页条数最大为 100")
    private Integer pageSize = PAGE_SIZE_DEFAULT;

    /** 转换为 MyBatis-Plus IPage 对象，供 service 层直接使用 */
    public <T> IPage<T> toPage() {
        return new Page<>(pageNo, pageSize);
    }
}
