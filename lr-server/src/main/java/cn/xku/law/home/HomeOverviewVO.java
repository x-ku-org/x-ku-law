package cn.xku.law.home;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页检索门户聚合数据：覆盖范围统计、今日重点、热点检索与最新更新。
 * 由 {@link HomeOverviewService} 一次性编排返回，供前端 PortalView 渲染。
 */
@Data
public class HomeOverviewVO {

    /** 现行法规收录（status=effective 计数） */
    private long corpusCount;
    /** 今日法规更新（publish_date=今天 计数） */
    private long todayUpdateCount;
    /** 效力层级覆盖（去重 legal_level 数） */
    private int levelCount;
    /** 地区规则覆盖（去重 region_code 数） */
    private int regionCount;

    /** 今日重点（取最新一条法规作头条；无数据时为 null） */
    private Highlight todayHighlight;
    /** 热点检索（近 7 日检索词 top N，无数据回退配置词） */
    private List<Trending> trending = new ArrayList<>();
    /** 最新更新（按发布日期倒序的有效法规 top N） */
    private List<Latest> latest = new ArrayList<>();

    /** 今日重点头条 */
    @Data
    public static class Highlight {
        private Long documentId;
        /** 类型标签，如「司法解释」「行政法规」 */
        private String tag;
        private String title;
        private String summary;
        private LocalDate date;
    }

    /** 热点检索词 */
    @Data
    public static class Trending {
        private String keyword;
        /** 热度（检索次数） */
        private int heat;
    }

    /** 最新更新条目 */
    @Data
    public static class Latest {
        private Long documentId;
        private String title;
        /** 类型标签（取 legalLevel，缺省 lawType） */
        private String tag;
        /** 角标，如「新增」（近 7 日发布）；无则为 null */
        private String badge;
        private LocalDate date;
    }
}
