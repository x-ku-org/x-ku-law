export interface LoginRequest {
  tenantCode: string;
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  mobile: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface AuthSession extends LoginResponse {
  tenantCode: string;
  username: string;
  loginAt: number;
}

/** GET /auth/me：当前登录用户资料 + 角色码 + 权限码 */
export interface CurrentUser {
  userId: number;
  username: string;
  realName: string | null;
  userType: string | null;
  /** 头像取流相对路径（/account/avatar/{id}?v=…），无头像为 null */
  avatarUrl: string | null;
  tenantCode: string | null;
  tenantName: string | null;
  roles: string[];
  permissions: string[];
}
