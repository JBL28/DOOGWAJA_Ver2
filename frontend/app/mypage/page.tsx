'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getMe, putMe, deleteMe, postLogout, postRefresh } from '@/lib/request';
import { useAuthStore } from '@/store/authStore';
import type { UpdateUserRequest, UserData } from '@/types/auth';

/**
 * plan.md 4-5 F-3 기준
 * - 인증되지 않은 사용자: /login으로 리다이렉트
 * - 닉네임 수정
 * - 비밀번호 변경
 * - 회원 탈퇴: 비밀번호 확인 후 → 로그아웃 → 메인 이동
 */
export default function MyPage() {
  const router = useRouter();
  const { accessToken, setAccessToken, clearAuth } = useAuthStore();

  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  // 닉네임 수정
  const [nickname, setNickname] = useState('');
  const [nicknameMsg, setNicknameMsg] = useState('');
  const [nicknameError, setNicknameError] = useState('');

  // 비밀번호 변경
  const [pwForm, setPwForm] = useState({
    currentPassword: '',
    newPassword: '',
  });
  const [pwMsg, setPwMsg] = useState('');
  const [pwError, setPwError] = useState('');

  // 회원 탈퇴
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  // 인증 확인 및 유저 정보 로드
  // 새로고침으로 Zustand가 초기화됐어도 Refresh Token 쿠키가 있으면 자동 복구
  useEffect(() => {
    const load = async () => {
      try {
        // accessToken이 없으면 refresh 먼저 시도
        if (!accessToken) {
          const refreshRes = await postRefresh();
          if (!refreshRes.success || !refreshRes.data?.accessToken) {
            router.replace('/login');
            return;
          }
          setAccessToken(refreshRes.data.accessToken);
        }

        // 유저 정보 조회
        const meRes = await getMe();
        if (meRes.success) {
          setUserData(meRes.data);
          setNickname(meRes.data.nickname);
        }
      } catch {
        router.replace('/login');
      } finally {
        setLoading(false);
      }
    };

    load();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 닉네임 수정
  async function handleNicknameUpdate(e: React.FormEvent) {
    e.preventDefault();
    setNicknameMsg('');
    setNicknameError('');
    if (!nickname.trim()) {
      setNicknameError('닉네임을 입력해주세요.');
      return;
    }
    try {
      const res = await putMe({ nickname } as UpdateUserRequest);
      if (res.success) {
        setNicknameMsg('닉네임이 변경되었습니다.');
        setUserData((prev) => prev ? { ...prev, nickname: res.data.nickname } : prev);
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setNicknameError(axiosErr?.response?.data?.message ?? '변경에 실패했습니다.');
    }
  }

  // 비밀번호 변경
  async function handlePasswordUpdate(e: React.FormEvent) {
    e.preventDefault();
    setPwMsg('');
    setPwError('');

    if (!pwForm.currentPassword || !pwForm.newPassword) {
      setPwError('현재 비밀번호와 새 비밀번호를 모두 입력해주세요.');
      return;
    }
    if (!/(?=.*[a-zA-Z])(?=.*[0-9])/.test(pwForm.newPassword)) {
      setPwError('새 비밀번호는 영문과 숫자를 포함해야 합니다.');
      return;
    }

    try {
      const res = await putMe({
        currentPassword: pwForm.currentPassword,
        newPassword: pwForm.newPassword,
      } as UpdateUserRequest);
      if (res.success) {
        setPwMsg('비밀번호가 변경되었습니다.');
        setPwForm({ currentPassword: '', newPassword: '' });
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setPwError(axiosErr?.response?.data?.message ?? '변경에 실패했습니다.');
    }
  }

  // 회원 탈퇴
  async function handleDelete(e: React.FormEvent) {
    e.preventDefault();
    setDeleteError('');
    if (!deletePassword) {
      setDeleteError('비밀번호를 입력해주세요.');
      return;
    }
    try {
      const res = await deleteMe({ password: deletePassword });
      if (res.success) {
        // 로그아웃 처리 후 메인 이동
        await postLogout().catch(() => {});
        clearAuth();
        router.push('/');
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setDeleteError(axiosErr?.response?.data?.message ?? '탈퇴에 실패했습니다.');
    }
  }

  if (loading) {
    return (
      <div className="page-wrapper">
        <p style={{ color: 'var(--text-muted)' }}>불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="page-wrapper" style={{ alignItems: 'flex-start', paddingTop: '3rem' }}>
      <div className="card" style={{ width: '100%', maxWidth: 480 }}>
        {/* 헤더 */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 className="heading-logo">🍪 내 정보</h1>
          {userData && (
            <p className="heading-sub">{userData.loginId} · {userData.role}</p>
          )}
        </div>

        {/* 닉네임 수정 */}
        <section style={{ marginBottom: '2rem' }}>
          <h2 className="section-title">닉네임 수정</h2>
          <form onSubmit={handleNicknameUpdate}>
            <div className="form-group">
              <label htmlFor="nickname" className="form-label">닉네임</label>
              <input
                id="nickname"
                type="text"
                className="form-input"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="새 닉네임"
              />
              {nicknameError && <span className="form-error">{nicknameError}</span>}
              {nicknameMsg && (
                <span style={{ fontSize: '0.8rem', color: 'var(--success)' }}>{nicknameMsg}</span>
              )}
            </div>
            <button id="nickname-submit" type="submit" className="btn-ghost" style={{ width: '100%' }}>
              닉네임 변경
            </button>
          </form>
        </section>

        {/* 비밀번호 변경 */}
        <section style={{ marginBottom: '2rem' }}>
          <h2 className="section-title">비밀번호 변경</h2>
          <form onSubmit={handlePasswordUpdate}>
            <div className="form-group">
              <label htmlFor="currentPassword" className="form-label">현재 비밀번호</label>
              <input
                id="currentPassword"
                type="password"
                className="form-input"
                value={pwForm.currentPassword}
                onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })}
                placeholder="현재 비밀번호"
                autoComplete="current-password"
              />
            </div>
            <div className="form-group">
              <label htmlFor="newPassword" className="form-label">새 비밀번호</label>
              <input
                id="newPassword"
                type="password"
                className="form-input"
                value={pwForm.newPassword}
                onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
                placeholder="영문과 숫자 각 1자 이상"
                autoComplete="new-password"
              />
              {pwError && <span className="form-error">{pwError}</span>}
              {pwMsg && (
                <span style={{ fontSize: '0.8rem', color: 'var(--success)' }}>{pwMsg}</span>
              )}
            </div>
            <button id="password-submit" type="submit" className="btn-ghost" style={{ width: '100%' }}>
              비밀번호 변경
            </button>
          </form>
        </section>

        {/* 회원 탈퇴 */}
        <section>
          <h2 className="section-title" style={{ color: 'var(--error)', borderColor: 'rgba(192,57,43,0.15)' }}>
            회원 탈퇴
          </h2>
          {!showDeleteConfirm ? (
            <button
              id="delete-account-btn"
              type="button"
              className="btn-danger"
              style={{ width: '100%' }}
              onClick={() => setShowDeleteConfirm(true)}
            >
              탈퇴하기
            </button>
          ) : (
            <form onSubmit={handleDelete}>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.8rem' }}>
                탈퇴하시려면 비밀번호를 입력해주세요. 탈퇴 후 복구는 불가능합니다.
              </p>
              <div className="form-group">
                <label htmlFor="delete-password" className="form-label">비밀번호 확인</label>
                <input
                  id="delete-password"
                  type="password"
                  className="form-input"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  placeholder="현재 비밀번호"
                  autoComplete="current-password"
                />
                {deleteError && <span className="form-error">{deleteError}</span>}
              </div>
              <div style={{ display: 'flex', gap: '0.8rem' }}>
                <button
                  type="button"
                  className="btn-ghost"
                  style={{ flex: 1 }}
                  onClick={() => { setShowDeleteConfirm(false); setDeletePassword(''); setDeleteError(''); }}
                >
                  취소
                </button>
                <button
                  id="delete-confirm-btn"
                  type="submit"
                  className="btn-danger"
                  style={{ flex: 1 }}
                >
                  탈퇴 확인
                </button>
              </div>
            </form>
          )}
        </section>
      </div>
    </div>
  );
}
