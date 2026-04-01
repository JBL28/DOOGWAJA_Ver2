import axiosInstance from '@/lib/axios';
import type {
  ApiResponse,
  LoginData,
  LoginRequest,
  RegisterRequest,
  UpdateUserRequest,
  UserData,
} from '@/types/auth';

/**
 * plan.md 4-3 lib/request.ts 기준
 * 모든 API 호출 함수 모음
 */

// --- 인증 ---

export const postRegister = (data: RegisterRequest): Promise<ApiResponse<null>> =>
  axiosInstance.post('/auth/register', data).then((res) => res.data);

export const postLogin = (data: LoginRequest): Promise<ApiResponse<LoginData>> =>
  axiosInstance.post('/auth/login', data).then((res) => res.data);

export const postLogout = (): Promise<ApiResponse<null>> =>
  axiosInstance.post('/auth/logout').then((res) => res.data);

export const postRefresh = (): Promise<ApiResponse<{ accessToken: string }>> =>
  axiosInstance.post('/auth/refresh').then((res) => res.data);

// --- 유저 ---

export const getMe = (): Promise<ApiResponse<UserData>> =>
  axiosInstance.get('/users/me').then((res) => res.data);

export const putMe = (
  data: UpdateUserRequest
): Promise<ApiResponse<{ userId: number; nickname: string }>> =>
  axiosInstance.put('/users/me', data).then((res) => res.data);

export const deleteMe = (data: { password: string }): Promise<ApiResponse<null>> =>
  axiosInstance.delete('/users/me', { data }).then((res) => res.data);
