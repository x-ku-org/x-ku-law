package cn.xku.law.law.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawDocumentQueryDTO;
import cn.xku.law.law.domain.vo.LawDocumentVO;
import com.baomidou.mybatisplus.extension.service.IService;

/** 法规文件核心业务接口 */
public interface LawDocumentService extends IService<LawDocumentDO> {

    /** 分页查询法规（支持关键词 + 多维度过滤） */
    PageResult<LawDocumentVO> pageDocuments(LawDocumentQueryDTO query);

    /** 查询法规详情，不存在时抛出 AppException(LAW_DOCUMENT_NOT_FOUND) */
    LawDocumentVO getDocumentById(Long id);

    /** 新建法规主记录，返回新建 ID */
    Long createDocument(LawDocumentCreateDTO dto);

    /** 更新法规主记录元信息 */
    void updateDocument(Long id, LawDocumentCreateDTO dto);

    /** 逻辑删除法规（MyBatis-Plus 自动将 deleted 置为 1） */
    void removeDocument(Long id);

    /**
     * 重算文档「现行版」：在已发布版本中取公布日最新者（并列取 id 最大），同步到
     * currentVersionId 及文档的 publish_date/effective_date/timeliness_status。
     * 与版本到达顺序无关，乱序/多批接入最终一致。法规时效 status（effective/repealed 等）
     * 由接入入口在文档落库时写入，不在此覆盖。无已发布版本时不改动。
     */
    void recomputeCurrentVersion(Long documentId);
}
