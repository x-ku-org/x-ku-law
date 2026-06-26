import { http, unwrap } from './http';
import type { HomeOverview } from '@/types/portal';

/** 首页检索门户聚合数据（覆盖统计、今日重点、热点检索、最新更新） */
export function getHomeOverview() {
  return unwrap<HomeOverview>(http.get('/home/overview'));
}
