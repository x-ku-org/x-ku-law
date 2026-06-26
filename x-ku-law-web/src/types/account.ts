/** GET /account/profile：当前用户完整账号资料（比 CurrentUser 更厚） */
export interface AccountProfile {
  userId: number;
  username: string;
  realName: string | null;
  nickname: string | null;
  email: string | null;
  mobile: string | null;
  gender: string | null;
  avatarUrl: string | null;
  userType: string | null;
  tenantCode: string | null;
  tenantName: string | null;
  roles: string[];
  lastLoginTime: string | null;
  lastLoginIp: string | null;
  passwordUpdateTime: string | null;
  createTime: string | null;
}

/** PUT /account/profile：仅这些字段可由用户自助修改（头像走 /account/avatar 上传） */
export interface AccountProfileUpdate {
  realName?: string;
  nickname?: string;
  email?: string;
  mobile?: string;
  gender?: string;
}

/** POST /account/password */
export interface ChangePasswordPayload {
  oldPassword: string;
  newPassword: string;
}

/** 检索默认值偏好（落库于 lr_user_preference，key 前缀 search.） */
export interface SearchDefaults {
  sort: string;
  pageSize?: number;
  regionCode: string;
  effectLevel: string;
  status: string;
}
