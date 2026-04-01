/**
 * [인증 상태 복구 공통 유틸리티]
 *
 * 배경 (zustand-auth-recovery.md 참조):
 *  - Zustand는 JS 런타임 메모리에만 존재하므로, 새로고침/URL 직접 입력 시 초기화된다.
 *  - Refresh Token(HttpOnly 쿠키)은 살아있어도 accessToken이 없으면 인증 실패가 된다.
 *  - postRefresh()는 accessToken만 반환하고 user 정보(role 등)는 반환하지 않는다.
 *    → setAccessToken()만 하면 user: null 상태가 되어 불완전한 인증 상태가 된다.
 *    → 반드시 postRefresh() 이후 getMe()를 호출해 setAuth()로 완전한 상태를 복구해야 한다.
 *
 * 올바른 복구 순서:
 *  1. accessToken + user 모두 있음  → 완전한 상태, 그대로 진행 (true 반환)
 *  2. accessToken 있음 + user 없음  → getMe()로 user 정보 보강 후 진행 (true 반환)
 *  3. accessToken 없음              → postRefresh() 시도
 *     - 성공 → setAccessToken() → getMe() → setAuth() → 진행 (true 반환)
 *     - 실패 → 인증 불가 (false 반환) → 호출 측에서 /login 리다이렉트
 */

import { getMe, postRefresh } from '@/lib/request';

interface UserInfo {
  userId: number;
  loginId: string;
  nickname: string;
  role: string;
}

interface AuthActions {
  accessToken: string | null;
  user: UserInfo | null;
  setAuth: (token: string, user: UserInfo) => void;
  setAccessToken: (token: string) => void;
  clearAuth: () => void;
}

let authInitPromise: Promise<boolean> | null = null;

/**
 * 인증 상태를 완전하게 복구한다.
 * @returns true  - 인증 복구 성공 (페이지 진행 가능)
 * @returns false - 인증 복구 실패 (/login으로 리다이렉트 필요)
 */
export async function initAuthState(auth: AuthActions): Promise<boolean> {
  const { accessToken, user, setAuth, setAccessToken, clearAuth } = auth;

  // --- 케이스 1: 완전한 인증 상태 ---
  // accessToken과 user 모두 존재 → 그대로 진행
  if (accessToken && user) {
    return true;
  }

  // --- 케이스 2: accessToken은 있으나 user 없음 ---
  // 이전 refresh에서 setAccessToken()만 하고 getMe()를 빠뜨린 경우
  if (accessToken && !user) {
    try {
      const meRes = await getMe();
      if (meRes.success && meRes.data) {
        setAuth(accessToken, {
          userId: meRes.data.userId,
          loginId: meRes.data.loginId,
          nickname: meRes.data.nickname,
          role: meRes.data.role,
        });
      }
    } catch {
      // user 정보 보강 실패해도 accessToken이 있으므로 진행
    }
    return true;
  }

  // --- 케이스 3: accessToken 없음 → Refresh Token 쿠키로 복구 시도 ---
  if (authInitPromise) {
    return authInitPromise;
  }

  authInitPromise = (async () => {
    try {
      const refreshRes = await postRefresh();
      if (!refreshRes.success || !refreshRes.data?.accessToken) {
        clearAuth();
        return false;
      }

      const newToken = refreshRes.data.accessToken;

      // [중요] setAccessToken() 먼저 호출 → 이후 getMe() 시 axios 인터셉터가 Bearer 헤더를 추가할 수 있음
      setAccessToken(newToken);

      // user 정보도 보강 → setAuth()로 완전한 인증 상태로 전환
      try {
        const meRes = await getMe();
        if (meRes.success && meRes.data) {
          setAuth(newToken, {
            userId: meRes.data.userId,
            loginId: meRes.data.loginId,
            nickname: meRes.data.nickname,
            role: meRes.data.role,
          });
        }
      } catch {
        // getMe 실패해도 accessToken은 저장됨. 진행 가능.
      }

      return true;
    } catch {
      clearAuth();
      return false;
    } finally {
      authInitPromise = null;
    }
  })();

  return authInitPromise;
}
