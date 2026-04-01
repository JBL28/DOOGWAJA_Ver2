'use client';

/**
 * plan.md 4-6 기준
 * 추천글 수정 페이지
 * - 인증 복구 정책 적용
 * - 기존 데이터 pre-fill
 * - 본인 아닌 경우 서버 403 → 메인으로 이동
 * - 수정 성공 시 router.push('/')
 */

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { getRecommendation, putRecommendation } from '@/lib/request';
import { initAuthState } from '@/lib/initAuth';

export default function EditRecommendationPage() {
  const router = useRouter();
  const params = useParams();
  const rcId = Number(params.rcId);

  const { accessToken, user, setAuth, setAccessToken, clearAuth } = useAuthStore();

  const [initialized, setInitialized] = useState(false);
  const [name, setName] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

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

  // ----- 기존 데이터 pre-fill -----
  useEffect(() => {
    if (!initialized || !rcId) return;

    const load = async () => {
      setLoading(true);
      try {
        const res = await getRecommendation(rcId);
        if (res.success) {
          setName(res.data.name);
          setReason(res.data.reason);
        } else {
          setError('게시글을 찾을 수 없습니다.');
        }
      } catch (err: unknown) {
        const axiosErr = err as { response?: { status?: number; data?: { message?: string } } };
        if (axiosErr.response?.status === 404) {
          setError('게시글을 찾을 수 없습니다.');
        } else {
          setError('게시글 정보를 불러오지 못했습니다.');
        }
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [initialized, rcId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!name.trim()) {
      setError('과자 이름을 입력해주세요.');
      return;
    }
    if (!reason.trim()) {
      setError('추천 이유를 입력해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await putRecommendation(rcId, { name: name.trim(), reason: reason.trim() });
      if (res.success) {
        router.push('/');
      } else {
        setError(res.message || '수정에 실패했습니다.');
      }
    } catch (err: unknown) {
      // 본인 아닌 경우 서버 403 → 메인으로 이동 (plan.md 4-6)
      const axiosErr = err as { response?: { status?: number; data?: { message?: string } } };
      if (axiosErr.response?.status === 403) {
        alert(axiosErr.response.data?.message || '본인의 게시글만 수정할 수 있습니다.');
        router.push('/');
        return;
      }
      const serverMsg = axiosErr.response?.data?.message;
      setError(serverMsg || '수정에 실패했습니다.');
    } finally {
      setSubmitting(false);
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
            추천글 수정
          </h1>
          <p className="heading-sub" style={{ marginBottom: '2rem' }}>
            추천 내용을 수정하세요
          </p>

          {error && <div className="alert alert-error">{error}</div>}

          {loading ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
              로딩 중...
            </div>
          ) : (
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
                  disabled={submitting}
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
                  disabled={submitting}
                  style={{ resize: 'vertical' }}
                />
              </div>

              <button
                id="btn-submit-edit"
                type="submit"
                className="btn-primary"
                disabled={submitting}
              >
                {submitting ? '수정 중...' : '수정 완료'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
