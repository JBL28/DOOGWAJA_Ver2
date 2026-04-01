'use client';

/**
 * plan.md 4-5 기준
 * 추천글 작성 페이지
 * - 인증 복구 정책 적용
 * - ADMIN 역할 시 서버 403 응답 처리
 * - 제출 성공 시 router.push('/')
 */

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { postRecommendation } from '@/lib/request';
import { initAuthState } from '@/lib/initAuth';

export default function NewRecommendationPage() {
  const router = useRouter();
  const { accessToken, user, setAuth, setAccessToken, clearAuth } = useAuthStore();

  const [initialized, setInitialized] = useState(false);
  const [name, setName] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // ----- 인증 복구 -----
  // [중요] lib/initAuth.ts의 initAuthState() 참조
  // postRefresh() 후 getMe()까지 호출해 setAuth()로 완전한 인증 상태를 복구한다.
  useEffect(() => {
    const init = async () => {
      const ok = await initAuthState({ accessToken, user, setAuth, setAccessToken, clearAuth });
      if (ok) {
        setInitialized(true);
      } else {
        router.push('/login');
      }
    };
    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // 클라이언트 사이드 유효성 검증 (plan.md 4-5)
    if (!name.trim()) {
      setError('과자 이름을 입력해주세요.');
      return;
    }
    if (!reason.trim()) {
      setError('추천 이유를 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      const res = await postRecommendation({ name: name.trim(), reason: reason.trim() });
      if (res.success) {
        router.push('/');
      } else {
        setError(res.message || '추천글 등록에 실패했습니다.');
      }
    } catch (err: unknown) {
      // ADMIN 403 등 서버 에러 처리
      const axiosErr = err as { response?: { data?: { message?: string } } };
      const serverMsg = axiosErr.response?.data?.message;
      setError(serverMsg || '추천글 등록에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!initialized) {
    return (
      <div className="page-wrapper">
        <p style={{ color: 'var(--text-muted)', fontFamily: 'var(--font-body)' }}>인증 확인 중...</p>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div style={{ width: '100%', maxWidth: '480px' }}>
        {/* 뒤로 가기 */}
        <div style={{ marginBottom: '1rem' }}>
          <Link
            href="/"
            style={{
              fontSize: '0.85rem',
              color: 'var(--brown-500)',
              textDecoration: 'none',
            }}
          >
            ← 목록으로
          </Link>
        </div>

        <div className="card">
          <h1
            className="heading-logo"
            style={{ fontSize: '1.4rem', marginBottom: '0.3rem' }}
          >
            과자 추천하기
          </h1>
          <p className="heading-sub" style={{ marginBottom: '2rem' }}>
            좋아하는 과자를 추천해보세요
          </p>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="name" className="form-label">
                과자 이름
              </label>
              <input
                id="name"
                type="text"
                className="form-input"
                placeholder="예: 허니버터칩"
                value={name}
                onChange={(e) => setName(e.target.value)}
                maxLength={100}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="reason" className="form-label">
                추천 이유
              </label>
              <textarea
                id="reason"
                className="form-input"
                placeholder="이 과자를 추천하는 이유를 적어주세요"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                rows={5}
                disabled={loading}
                style={{ resize: 'vertical' }}
              />
            </div>

            <button
              id="btn-submit-recommendation"
              type="submit"
              className="btn-primary"
              disabled={loading}
            >
              {loading ? '등록 중...' : '추천글 등록'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
