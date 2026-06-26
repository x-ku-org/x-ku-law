/** 首页检索门户聚合数据，对应后端 HomeOverviewVO */

export interface PortalHighlight {
  documentId?: number;
  tag?: string;
  title?: string;
  summary?: string;
  date?: string;
}

export interface PortalTrending {
  keyword: string;
  heat: number;
}

export interface PortalLatest {
  documentId?: number;
  title?: string;
  tag?: string;
  badge?: string;
  date?: string;
}

export interface HomeOverview {
  corpusCount: number;
  todayUpdateCount: number;
  levelCount: number;
  regionCount: number;
  todayHighlight?: PortalHighlight | null;
  trending: PortalTrending[];
  latest: PortalLatest[];
}
