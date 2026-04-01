/**
 * plan.md 4-1 types/auth.ts 기준
 * api_spec.md 1, 2절 기반 타입 정의
 */

export interface RegisterRequest {
  loginId: string;
  password: string;
  nickname: string;
}

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface LoginData {
  accessToken: string;
  userId: number;
  loginId: string;
  nickname: string;
  role: string;
}

export interface UpdateUserRequest {
  nickname?: string;
  currentPassword?: string;
  newPassword?: string;
}

export interface UserData {
  userId: number;
  loginId: string;
  nickname: string;
  role: string;
  status: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}
