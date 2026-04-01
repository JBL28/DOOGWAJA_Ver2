import { create } from 'zustand';

/**
 * plan.md 4-4 Zustand 스토어 기준
 * - Access Token: Zustand 메모리 보관 (XSS 방어)
 * - Refresh Token: HttpOnly 쿠키로만 관리 (프론트 접근 불가)
 */

interface UserInfo {
  userId: number;
  loginId: string;
  nickname: string;
  role: string;
}

interface AuthState {
  accessToken: string | null;
  user: UserInfo | null;
  setAuth: (token: string, user: UserInfo) => void;
  setAccessToken: (token: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,

  setAuth: (token, user) => set({ accessToken: token, user }),

  // plan.md 4-2 refresh 후 토큰만 갱신
  setAccessToken: (token) => set((state) => ({ ...state, accessToken: token })),

  clearAuth: () => set({ accessToken: null, user: null }),
}));
