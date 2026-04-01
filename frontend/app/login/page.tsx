'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { postLogin } from '@/lib/request';
import { useAuthStore } from '@/store/authStore';
import type { LoginRequest } from '@/types/auth';

/**
 * plan.md 4-5 F-1 기준
 * - 입력: loginId, password
 * - 성공: accessToken 저장, Zustand에 유저 정보 저장 → / 이동
 * - 실패: 서버 오류 메시지 표시
 * - 회원가입 링크 포함
 */
export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const registered = searchParams.get('registered') === '1';
  const setAuth = useAuthStore((s) => s.setAuth);

  const [form, setForm] = useState<LoginRequest>({ loginId: '', password: '' });
  const [errors, setErrors] = useState<Partial<LoginRequest>>({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    const e: Partial<LoginRequest> = {};
    if (!form.loginId) e.loginId = '아이디를 입력해주세요.';
    if (!form.password) e.password = '비밀번호를 입력해주세요.';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setLoading(true);
    try {
      const res = await postLogin(form);
      if (res.success && res.data) {
        setAuth(res.data.accessToken, {
          userId: res.data.userId,
          loginId: res.data.loginId,
          nickname: res.data.nickname,
          role: res.data.role,
        });
        router.push('/');
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setServerError(
        axiosErr?.response?.data?.message ?? '로그인에 실패했습니다.'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-wrapper">
      <div className="card" style={{ width: '100%', maxWidth: 400 }}>
        {/* 헤더 */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 className="heading-logo">🍪 두과자</h1>
          <p className="heading-sub">우리 반 간식 공유 서비스</p>
        </div>

        {/* 회원가입 성공 메시지 */}
        {registered && (
          <div className="alert alert-success" role="status">
            회원가입이 완료되었습니다. 로그인해주세요.
          </div>
        )}

        {/* 서버 에러 */}
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="loginId" className="form-label">아이디</label>
            <input
              id="loginId"
              type="text"
              className="form-input"
              placeholder="아이디를 입력하세요"
              value={form.loginId}
              onChange={(e) => setForm({ ...form, loginId: e.target.value })}
              autoComplete="username"
            />
            {errors.loginId && <span className="form-error">{errors.loginId}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">비밀번호</label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="비밀번호를 입력하세요"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              autoComplete="current-password"
            />
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <button
            id="login-submit"
            type="submit"
            className="btn-primary"
            disabled={loading}
            style={{ marginTop: '0.5rem' }}
          >
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="divider">or</div>

        <div style={{ textAlign: 'center' }}>
          <span style={{ fontSize: '0.88rem', color: 'var(--text-muted)' }}>
            계정이 없으신가요?{' '}
          </span>
          <Link href="/register" className="link">
            회원가입
          </Link>
        </div>
      </div>
    </div>
  );
}
