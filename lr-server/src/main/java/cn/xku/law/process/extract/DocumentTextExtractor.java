package cn.xku.law.process.extract;

/** 二进制法规文件 → 纯文本抽取器。按文件扩展名分发到具体解析实现。 */
public interface DocumentTextExtractor {

    /** 是否支持该扩展名（小写，不含点，如 "docx"/"pdf"） */
    boolean supports(String ext);

    /**
     * 抽取正文纯文本。
     *
     * @param bytes    文件字节
     * @param ext      小写扩展名（不含点）
     * @param fileName 原始文件名（仅用于日志/容错）
     * @return 抽取的正文；无法抽取时返回空串（不抛异常给上层中断管线的判断交由调用方）
     */
    String extract(byte[] bytes, String ext, String fileName);
}
