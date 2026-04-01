'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { getBoughtSnack, putBoughtSnack } from '@/lib/request';
import { initAuthState } from '@/lib/initAuth';

export default function EditBoughtSnackPage() {
  const router = useRouter();
  const params = useParams();
  const bsId = Number(params.bsId);

  const { accessToken, user, setAuth, setAccessToken, clearAuth } = useAuthStore();

  const [initialized, setInitialized] = useState(false);
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

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

  useEffect(() => {
    if (!initialized || !bsId) return;

    const load = async () => {
      setLoading(true);
      try {
        const res = await getBoughtSnack(bsId);
        if (res.success) {
          setName(res.data.name);
        } else {
          setError('과자를 찾을 수 없습니다.');
        }
      } catch (err: unknown) {
        const axiosErr = err as { response?: { status?: number; data?: { message?: string } } };
        if (axiosErr.response?.status === 404) {
          setError('과자를 찾을 수 없습니다.');
        } else {
          setError('과자 정보를 불러오지 못했습니다.');
        }
      } finally {
        setLoading(false);
      }
    };

    if (user?.role === 'ADMIN') {
      load();
    } else {
      setLoading(false);
    }
  }, [initialized, bsId, user?.role]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!name.trim()) {
      setError('과자 이름을 입력해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await putBoughtSnack(bsId, { name: name.trim() });
      if (res.success) {
        router.push('/');
      } else {
        setError(res.message || '수정에 실패했습니다.');
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number; data?: { message?: string } } };
      if (axiosErr.response?.status === 403) {
        alert(axiosErr.response.data?.message || '관리자만 수정할 수 있습니다.');
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

  // 관리자 권한 확인 방어 코드
  if (user?.role !== 'ADMIN') {
    return (
      <div className="page-wrapper">
        <div className="card" style={{ padding: '3rem', textAlign: 'center' }}>
          <h2>접근 권한이 없습니다.</h2>
          <p>관리자만 접근할 수 있는 페이지입니다.</p>
          <Link href="/" style={{ color: 'var(--brown-500)', textDecoration: 'underline', marginTop: '1rem', display: 'inline-block' }}>
            메인으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div style={{ width: '100%', maxWidth: '480px' }}>
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
          <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
            <Link href="/" style={{ display: 'inline-block' }}>
              <img src="/logo.svg" alt="Snack Overflow" className="logo-img" style={{ height: '2.2rem' }} />
            </Link>
          </div>
          <h1
            className="section-title"
            style={{ fontSize: '1.3rem', marginBottom: '0.4rem', textAlign: 'center', borderBottom: 'none' }}
          >
            구매 과자 수정
          </h1>
          <p className="heading-sub" style={{ marginBottom: '2rem' }}>
            구매 과자 정보를 수정합니다
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
                  placeholder="예: 홈런볼"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  maxLength={100}
                  disabled={submitting}
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
