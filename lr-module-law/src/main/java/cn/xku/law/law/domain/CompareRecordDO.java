package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 版本对比记录，对应 lr_compare_record。
 *
 * <p>两类来源共用此表：管线自动生成的「相邻版本逐条对比」以 {@code userId=0}（系统）落库；
 * 用户主动发起的对比写各自 {@code userId}，按 user_id 天然隔离。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_compare_record")
public class CompareRecordDO extends BaseDO {

    /** 发起对比的用户ID；0 表示系统/管线自动生成（表列 NOT NULL）。 */
    private Long userId;
    private Long documentId;
    private Long baseVersionId;
    private Long targetVersionId;
    /** 对比类型：text/article/semantic */
    private String compareType;
    /** 对比结果 JSON（逐条明细），对应 result_json 列。 */
    private String resultJson;
    /** 导出文件ID，可空。 */
    private Long fileId;
}
