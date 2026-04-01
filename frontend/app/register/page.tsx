'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { postRegister } from '@/lib/request';
import type { RegisterRequest } from '@/types/auth';

/**
 * plan.md 4-5 F-2 기준
 * - 입력: loginId (5자 이상, 영문+숫자), password (영문+숫자 각 1자 이상), nickname (1자 이상)
 * - 클라이언트 유효성 검증 후 서버 요청
 * - 성공: /login 이동, 성공 메시지 표시
 * - 실패: 서버 오류 메시지 표시
 */
export default function RegisterPage() {
  const router = useRouter();

  const [form, setForm] = useState<RegisterRequest>({
    loginId: '',
    password: '',
    nickname: '',
  });
  const [errors, setErrors] = useState<Partial<Record<keyof RegisterRequest, string>>>({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    const e: Partial<Record<keyof RegisterRequest, string>> = {};

    // loginId: 5자 이상, 영문+숫자
    if (!form.loginId) {
      e.loginId = '아이디를 입력해주세요.';
    } else if (form.loginId.length < 5 || !/^[a-zA-Z0-9]+$/.test(form.loginId)) {
      e.loginId = 'loginId는 5자 이상이어야 합니다.';
    }

    // password: 영문과 숫자 각 1자 이상 포함
    if (!form.password) {
      e.password = '비밀번호를 입력해주세요.';
    } else if (!/(?=.*[a-zA-Z])(?=.*[0-9])/.test(form.password)) {
      e.password = '비밀번호는 영문과 숫자를 포함해야 합니다.';
    }

    // nickname: 1자 이상
    if (!form.nickname || form.nickname.trim().length === 0) {
      e.nickname = 'nickname은 1자 이상이어야 합니다.';
    }

    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setLoading(true);
    try {
      const res = await postRegister(form);
      if (res.success) {
        // plan.md 7절: 회원가입 후 자동 로그인 없이 /login으로 이동
        router.push('/login?registered=1');
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setServerError(
        axiosErr?.response?.data?.message ?? '회원가입에 실패했습니다.'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-wrapper">
      <div className="card" style={{ width: '100%', maxWidth: 420 }}>
        {/* 헤더 */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <Link href="/" style={{ display: 'inline-block' }}>
            <img src="/logo.svg" alt="Snack Overflow" className="logo-img" style={{ height: '2.8rem' }} />
          </Link>
          <p className="heading-sub">회원가입</p>
        </div>

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
              placeholder="5자 이상, 영문+숫자"
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
              placeholder="영문과 숫자 각 1자 이상"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              autoComplete="new-password"
            />
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="nickname" className="form-label">닉네임</label>
            <input
              id="nickname"
              type="text"
              className="form-input"
              placeholder="화면에 표시될 이름"
              value={form.nickname}
              onChange={(e) => setForm({ ...form, nickname: e.target.value })}
            />
            {errors.nickname && <span className="form-error">{errors.nickname}</span>}
          </div>

          <button
            id="register-submit"
            type="submit"
            className="btn-primary"
            disabled={loading}
            style={{ marginTop: '0.5rem' }}
          >
            {loading ? '처리 중...' : '회원가입'}
          </button>
        </form>

        <div className="divider">or</div>

        <div style={{ textAlign: 'center' }}>
          <span style={{ fontSize: '0.88rem', color: 'var(--text-muted)' }}>
            이미 계정이 있으신가요?{' '}
          </span>
          <Link href="/login" className="link">
            로그인
          </Link>
        </div>
      </div>
    </div>
  );
}
