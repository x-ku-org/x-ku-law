export interface CommonResult<T> {
  code: number;
  msg: string;
  data: T;
}

export interface PageResult<T> {
  total: number;
  list: T[];
  filteredCount?: number;
}

export interface PageQuery {
  pageNo?: number;
  pageSize?: number;
}

export type RecordValue = string | number | boolean | null | undefined;

export interface OptionItem {
  label: string;
  value: string | number | boolean;
}

export interface ApiErrorPayload {
  code: number;
  msg: string;
  status?: number;
}
