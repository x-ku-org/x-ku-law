package cn.xku.law.openapi.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 开放 API 客户端，对应 lr_api_client。
 * 此包下其余 DO（ApiAccessLogDO / DataExportApplyDO）结构相同，TODO: 按需补全。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_api_client")
public class ApiClientDO extends BaseDO {

    private String clientName;
    private String appKey;
    private String secretKey;
    private String ipWhitelist;
    private Integer qpsLimit;
    private String status;
}
