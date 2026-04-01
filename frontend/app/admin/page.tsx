'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import { getAdminUsers, patchAdminUserStatus, deleteAdminUser } from '@/lib/request';
import { initAuthState } from '@/lib/initAuth';
import type { AdminUserListItem } from '@/types/admin';
import type { PagedData } from '@/types/recommendation';

export default function AdminPage() {
  const router = useRouter();
  const { accessToken, user, setAuth, setAccessToken, clearAuth } = useAuthStore();
  const [initialized, setInitialized] = useState(false);

  // Users State
  const [users, setUsers] = useState<AdminUserListItem[]>([]);
  const [pagedInfo, setPagedInfo] = useState<Omit<PagedData<unknown>, 'content'>>({
    page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
  });
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // 권한 체크 및 초기화
  useEffect(() => {
    const init = async () => {
      const ok = await initAuthState({ accessToken, user, setAuth, setAccessToken, clearAuth });
      if (!ok) {
        router.push('/login');
      } else {
        setInitialized(true);
      }
    };
    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 유저 목록 로드
  const loadUsers = useCallback(async (page: number) => {
    setLoading(true);
    setError('');
    try {
      const res = await getAdminUsers({ page, size: 10 });
      if (res.success) {
        const { content, ...rest } = res.data;
        setUsers(content);
        setPagedInfo(rest);
        setCurrentPage(page);
      }
    } catch {
      setError('유저 목록을 불러오지 못했습니다. (권한이 없거나 서버 오류)');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (initialized) {
      if (user?.role !== 'ADMIN') {
        alert('관리자 권한이 없습니다.');
        router.push('/');
        return;
      }
      loadUsers(0);
    }
  }, [initialized, loadUsers, user, router]);

  const handleStatusToggle = async (userId: number, currentStatus: string) => {
    if (currentStatus === 'DELETED') {
        alert('삭제된 계정은 상태를 변경할 수 없습니다.');
        return;
    }
    const nextStatus = currentStatus === 'ACTIVATED' ? 'DEACTIVATED' : 'ACTIVATED';
    if (!confirm(`해당 유저의 상태를 ${nextStatus}로 변경하시겠습니까?`)) return;

    try {
      const res = await patchAdminUserStatus(userId, { status: nextStatus });
      if (res.success) {
         loadUsers(currentPage);
      }
    } catch {
      alert('상태 변경에 실패했습니다.');
    }
  };

  const handleDelete = async (userId: number) => {
    if (!confirm('이 유저를 정말로 삭제(이용 정지) 처리하시겠습니까? 영구 삭제는 아니며 상태만 DELETED로 변경됩니다.')) return;
    try {
      const res = await deleteAdminUser(userId);
      if (res.success) {
         loadUsers(currentPage);
      }
    } catch {
      alert('삭제 처리에 실패했습니다.');
    }
  };

  if (!initialized) {
    return <div className="page-wrapper"><p>인증 확인 중...</p></div>;
  }

  if (user?.role !== 'ADMIN') return null;

  return (
    <div className="page-wrapper" style={{ padding: '2rem 1rem' }}>
      <div style={{ maxWidth: '900px', width: '100%', margin: '0 auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ fontSize: '1.6rem', color: 'var(--brown-700)', fontWeight: 700 }}>어드민 시스템</h1>
            <p style={{ color: 'var(--text-muted)' }}>전체 유저 관리 및 제어</p>
          </div>
          <Link href="/" style={{ padding: '0.45rem 1rem', background: 'var(--brown-700)', color: 'white', borderRadius: '3px', textDecoration: 'none', fontSize: '0.9rem' }}>
            돌아가기
          </Link>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>로딩 중...</div>
        ) : (
          <div className="card" style={{ padding: '1.5rem', overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid var(--brown-100)', textAlign: 'left' }}>
                  <th style={{ padding: '0.8rem' }}>ID</th>
                  <th style={{ padding: '0.8rem' }}>계정 (LoginId)</th>
                  <th style={{ padding: '0.8rem' }}>닉네임</th>
                  <th style={{ padding: '0.8rem' }}>역할</th>
                  <th style={{ padding: '0.8rem' }}>상태</th>
                  <th style={{ padding: '0.8rem' }}>가입일</th>
                  <th style={{ padding: '0.8rem', textAlign: 'center' }}>관리</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.userId} style={{ borderBottom: '1px solid var(--brown-50)' }}>
                    <td style={{ padding: '0.8rem' }}>{u.userId}</td>
                    <td style={{ padding: '0.8rem' }}>{u.loginId}</td>
                    <td style={{ padding: '0.8rem' }}>{u.nickname}</td>
                    <td style={{ padding: '0.8rem' }}>
                      <span style={{ 
                          background: u.role === 'ADMIN' ? '#fadbd8' : '#eaf2f8', 
                          color: u.role === 'ADMIN' ? '#c0392b' : '#2980b9',
                          padding: '0.2rem 0.6rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold' 
                      }}>
                        {u.role}
                      </span>
                    </td>
                    <td style={{ padding: '0.8rem' }}>
                      <span style={{ 
                          background: u.status === 'ACTIVATED' ? '#d5f5e3' : u.status === 'DEACTIVATED' ? '#fdebd0' : '#eaeded', 
                          color: u.status === 'ACTIVATED' ? '#27ae60' : u.status === 'DEACTIVATED' ? '#d68910' : '#7f8c8d',
                          padding: '0.2rem 0.6rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold' 
                      }}>
                        {u.status}
                      </span>
                    </td>
                    <td style={{ padding: '0.8rem' }}>{new Date(u.createdAt).toLocaleDateString('ko-KR')}</td>
                    <td style={{ padding: '0.8rem', textAlign: 'center' }}>
                      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                        <button
                          onClick={() => handleStatusToggle(u.userId, u.status)}
                          disabled={u.status === 'DELETED' || u.role === 'ADMIN'}
                          style={{
                            padding: '0.3rem 0.6rem', fontSize: '0.8rem',
                            background: 'white', border: '1px solid var(--brown-200)', borderRadius: '3px',
                            cursor: (u.status === 'DELETED' || u.role === 'ADMIN') ? 'not-allowed' : 'pointer',
                            color: 'var(--text-secondary)'
                          }}
                        >
                          {u.status === 'ACTIVATED' ? '정지' : '활성'}
                        </button>
                        <button
                          onClick={() => handleDelete(u.userId)}
                          disabled={u.status === 'DELETED' || u.role === 'ADMIN'}
                          style={{
                            padding: '0.3rem 0.6rem', fontSize: '0.8rem',
                            background: (u.status === 'DELETED' || u.role === 'ADMIN') ? '#f2f4f4' : '#e74c3c', 
                            border: 'none', borderRadius: '3px',
                            cursor: (u.status === 'DELETED' || u.role === 'ADMIN') ? 'not-allowed' : 'pointer',
                            color: (u.status === 'DELETED' || u.role === 'ADMIN') ? '#bdc3c7' : 'white'
                          }}
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                     <td colSpan={7} style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>유저가 없습니다.</td>
                  </tr>
                )}
              </tbody>
            </table>

            {/* Pagination */}
            {pagedInfo.totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.4rem', marginTop: '2rem' }}>
                <button onClick={() => loadUsers(currentPage - 1)} disabled={pagedInfo.first} style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem', background: 'transparent', border: 'none', cursor: pagedInfo.first ? 'not-allowed' : 'pointer' }}>이전</button>
                {Array.from({ length: pagedInfo.totalPages }, (_, i) => (
                  <button key={i} onClick={() => loadUsers(i)} style={{ padding: '0.4rem 0.7rem', fontSize: '0.85rem', borderRadius: '3px', border: '1.5px solid', borderColor: i === currentPage ? 'var(--brown-700)' : 'var(--brown-100)', background: i === currentPage ? 'var(--brown-700)' : 'transparent', color: i === currentPage ? 'var(--ivory-100)' : 'var(--text-secondary)', cursor: 'pointer' }}>{i + 1}</button>
                ))}
                <button onClick={() => loadUsers(currentPage + 1)} disabled={pagedInfo.last} style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem', background: 'transparent', border: 'none', cursor: pagedInfo.last ? 'not-allowed' : 'pointer' }}>다음</button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
