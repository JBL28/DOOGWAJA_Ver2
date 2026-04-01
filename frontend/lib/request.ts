import axiosInstance from '@/lib/axios';
import type {
  ApiResponse,
  LoginData,
  LoginRequest,
  RegisterRequest,
  UpdateUserRequest,
  UserData,
} from '@/types/auth';
import type {
  CommentItemDTO,
  FeedbackResultDTO,
  LikedUsersResponseDTO,
  PagedData,
  RecommendationDetailDTO,
  RecommendationListItemDTO,
} from '@/types/recommendation';
import type {
  BoughtSnackDetailDTO,
  BoughtSnackListItemDTO,
} from '@/types/bought-snack';
import type { AdminUserListItem } from '@/types/admin';


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

// --- 추천글 ---

export const getRecommendations = (params: {
  page: number;
  size: number;
}): Promise<ApiResponse<PagedData<RecommendationListItemDTO>>> =>
  axiosInstance.get('/recommendations', { params }).then((res) => res.data);

export const getRecommendation = (
  rcId: number
): Promise<ApiResponse<RecommendationDetailDTO>> =>
  axiosInstance.get(`/recommendations/${rcId}`).then((res) => res.data);

export const postRecommendation = (data: {
  name: string;
  reason: string;
}): Promise<ApiResponse<{ rcId: number }>> =>
  axiosInstance.post('/recommendations', data).then((res) => res.data);

export const putRecommendation = (
  rcId: number,
  data: { name: string; reason: string }
): Promise<ApiResponse<{ rcId: number }>> =>
  axiosInstance.put(`/recommendations/${rcId}`, data).then((res) => res.data);

export const deleteRecommendation = (rcId: number): Promise<ApiResponse<null>> =>
  axiosInstance.delete(`/recommendations/${rcId}`).then((res) => res.data);

// --- 추천 댓글 ---

export const getRcComments = (
  rcId: number,
  params: { page: number; size: number }
): Promise<ApiResponse<PagedData<CommentItemDTO>>> =>
  axiosInstance.get(`/recommendations/${rcId}/comments`, { params }).then((res) => res.data);

export const postRcComment = (
  rcId: number,
  data: { content: string }
): Promise<ApiResponse<{ rccId: number }>> =>
  axiosInstance.post(`/recommendations/${rcId}/comments`, data).then((res) => res.data);

export const putRcComment = (
  rcId: number,
  rccId: number,
  data: { content: string }
): Promise<ApiResponse<{ rccId: number }>> =>
  axiosInstance
    .put(`/recommendations/${rcId}/comments/${rccId}`, data)
    .then((res) => res.data);

export const deleteRcComment = (
  rcId: number,
  rccId: number
): Promise<ApiResponse<null>> =>
  axiosInstance.delete(`/recommendations/${rcId}/comments/${rccId}`).then((res) => res.data);

// --- 추천글 피드백 ---

export const postRcFeedback = (
  rcId: number,
  data: { status: 'LIKE' | 'DISLIKE' }
): Promise<ApiResponse<FeedbackResultDTO>> =>
  axiosInstance.post(`/recommendations/${rcId}/feedback`, data).then((res) => res.data);

export const getRecommendationLikes = (
  rcId: number
): Promise<ApiResponse<LikedUsersResponseDTO>> =>
  axiosInstance.get(`/recommendations/${rcId}/likes`).then((res) => res.data);

// --- 추천 댓글 피드백 ---

export const postRccFeedback = (
  rcId: number,
  rccId: number,
  data: { status: 'LIKE' | 'DISLIKE' }
): Promise<ApiResponse<FeedbackResultDTO>> =>
  axiosInstance
    .post(`/recommendations/${rcId}/comments/${rccId}/feedback`, data)
    .then((res) => res.data);


// --- 구매 과자 ---

export const getBoughtSnacks = (params: {
  page: number;
  size: number;
}): Promise<ApiResponse<PagedData<BoughtSnackListItemDTO>>> =>
  axiosInstance.get('/bought-snacks', { params }).then((res) => res.data);

export const getBoughtSnack = (
  bsId: number
): Promise<ApiResponse<BoughtSnackDetailDTO>> =>
  axiosInstance.get(`/bought-snacks/${bsId}`).then((res) => res.data);

export const postBoughtSnack = (data: {
  name: string;
}): Promise<ApiResponse<{ bsId: number }>> =>
  axiosInstance.post('/bought-snacks', data).then((res) => res.data);

export const putBoughtSnack = (
  bsId: number,
  data: { name: string }
): Promise<ApiResponse<{ bsId: number }>> =>
  axiosInstance.put(`/bought-snacks/${bsId}`, data).then((res) => res.data);

export const deleteBoughtSnack = (bsId: number): Promise<ApiResponse<null>> =>
  axiosInstance.delete(`/bought-snacks/${bsId}`).then((res) => res.data);

export const patchBoughtSnackStatus = (
  bsId: number,
  data: { status: 'SHIPPING' | 'IN_STOCK' | 'OUT_OF_STOCK' }
): Promise<ApiResponse<BoughtSnackDetailDTO>> =>
  axiosInstance.patch(`/bought-snacks/${bsId}/status`, data).then((res) => res.data);

// --- 구매 과자 댓글 ---

export const getBsComments = (
  bsId: number,
  params: { page: number; size: number }
): Promise<ApiResponse<PagedData<CommentItemDTO>>> =>
  axiosInstance.get(`/bought-snacks/${bsId}/comments`, { params }).then((res) => res.data);

export const postBsComment = (
  bsId: number,
  data: { content: string }
): Promise<ApiResponse<{ bscId: number }>> =>
  axiosInstance.post(`/bought-snacks/${bsId}/comments`, data).then((res) => res.data);

export const putBsComment = (
  bsId: number,
  bscId: number,
  data: { content: string }
): Promise<ApiResponse<{ bscId: number }>> =>
  axiosInstance
    .put(`/bought-snacks/${bsId}/comments/${bscId}`, data)
    .then((res) => res.data);

export const deleteBsComment = (
  bsId: number,
  bscId: number
): Promise<ApiResponse<null>> =>
  axiosInstance.delete(`/bought-snacks/${bsId}/comments/${bscId}`).then((res) => res.data);

// --- 구매 과자 피드백 ---

export const postBsFeedback = (
  bsId: number,
  data: { status: 'LIKE' | 'DISLIKE' }
): Promise<ApiResponse<FeedbackResultDTO>> =>
  axiosInstance.post(`/bought-snacks/${bsId}/feedback`, data).then((res) => res.data);

export const getBoughtSnackLikes = (
  bsId: number
): Promise<ApiResponse<LikedUsersResponseDTO>> =>
  axiosInstance.get(`/bought-snacks/${bsId}/likes`).then((res) => res.data);

// --- 구매 과자 댓글 피드백 ---

export const postBscFeedback = (
  bsId: number,
  bscId: number,
  data: { status: 'LIKE' | 'DISLIKE' }
): Promise<ApiResponse<FeedbackResultDTO>> =>
  axiosInstance
    .post(`/bought-snacks/${bsId}/comments/${bscId}/feedback`, data)
    .then((res) => res.data);

// --- 관리자 (ADMIN) ---

export const getAdminUsers = (params: {
  page: number;
  size: number;
}): Promise<ApiResponse<PagedData<AdminUserListItem>>> =>
  axiosInstance.get('/admin/users', { params }).then((res) => res.data);

export const patchAdminUserStatus = (
  userId: number,
  data: { status: 'ACTIVATED' | 'DEACTIVATED' }
): Promise<ApiResponse<null>> =>
  axiosInstance.patch(`/admin/users/${userId}/status`, data).then((res) => res.data);

export const deleteAdminUser = (userId: number): Promise<ApiResponse<null>> =>
  axiosInstance.delete(`/admin/users/${userId}`).then((res) => res.data);

