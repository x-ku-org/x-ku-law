package cn.xku.law.collect.source;

import java.util.Map;
import java.util.Set;

/**
 * 数据源适配器：封装<b>某一个采集数据源</b>的所有源专属处理——元数据字段映射、
 * 文档/版本标识规则、正文文件命名匹配。FLK 是当前的<b>默认</b>实现，但架构上不是唯一一个；
 * 接入新数据源只需新增一个本接口的 Spring Bean（并为它种 lr_content_source/lr_collect_task）。
 *
 * <p>本接口位于 lr-module-collect，仅依赖 lr-common：所有方法都是<b>纯映射</b>，
 * 不触碰 FileService 或法规域服务（那些在 lr-server 的编排器里）。{@link #matchFileKey}
 * 只在传入的对象 key 集合中做只读字符串匹配，返回命中的 key，由编排器去注册文件。
 */
public interface SourceAdapter {

    /** 源编码，如 "flk" / "gb"。与 lr_collect_task.parser_code 对应，用于注册表解析。 */
    String sourceCode();

    /** 对象存储中本源运行文件夹的前缀，如 "fglaw" / "gblaw"。 */
    String folderPrefix();

    /** 本源「运行完成」信号文件名（最后上传），如 "laws_metadata.json"。 */
    String metadataFileName();

    /** 与 lr_content_source.source_name 一致的来源名，用于解析 sourceId。 */
    String sourceName();

    /** 把一行原始元数据映射为归一化字段（含 lawUid 文档分组键、versionKey 版本键）。 */
    MappedLaw map(Map<String, Object> item);

    /**
     * 按本源的文件命名规则，在对象 key 集合 {@code keys} 中匹配该法规的正文文件。
     * 命中返回完整对象 key（含文件夹前缀），否则返回 null（由编排器置为元数据，不挂文件）。
     * 实现须保证「宁可不挂，绝不错挂」：无法消歧时返回 null。
     */
    String matchFileKey(MappedLaw m, String folder, Set<String> keys);
}
