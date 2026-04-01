'use client';

/**
 * plan.md 4-4, 6-1 기준
 * 메인 페이지: 추천글 리스트 + 구매 과자 현황 리스트 + 탭 구성 + 인증 복구 정책
 */

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/authStore';
import {
  getRecommendations,
  postRcFeedback,
  deleteRecommendation,
  postLogout,
  getBoughtSnacks,
  postBsFeedback,
  deleteBoughtSnack,
  patchBoughtSnackStatus
} from '@/lib/request';
import { initAuthState } from '@/lib/initAuth';
import type {
  RecommendationListItemDTO,
  FeedbackResultDTO,
  PagedData,
} from '@/types/recommendation';
import type {
  BoughtSnackListItemDTO,
} from '@/types/bought-snack';
import CommentSection from '@/components/CommentSection';

type TabType = 'RECOMMEND' | 'BOUGHT';

export default function HomePage() {
  const router = useRouter();
  const { accessToken, user, setAuth, setAccessToken, clearAuth } = useAuthStore();

  const [initialized, setInitialized] = useState(false);
  const [activeTab, setActiveTab] = useState<TabType>('BOUGHT');

  // 추천글 상태
  const [recommendations, setRecommendations] = useState<RecommendationListItemDTO[]>([]);
  const [pagedInfo, setPagedInfo] = useState<Omit<PagedData<unknown>, 'content'>>({
    page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
  });
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);

  // 구매 과자 상태
  const [boughtSnacks, setBoughtSnacks] = useState<BoughtSnackListItemDTO[]>([]);
  const [bsPagedInfo, setBsPagedInfo] = useState<Omit<PagedData<unknown>, 'content'>>({
    page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
  });
  const [bsCurrentPage, setBsCurrentPage] = useState(0);
  const [bsLoading, setBsLoading] = useState(true);

  const [error, setError] = useState('');

  // ----- 인증 복구 -----
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

  // ----- 추천글 로드 -----
  const loadRecommendations = useCallback(async (page: number) => {
    setLoading(true);
    setError('');
    try {
      const res = await getRecommendations({ page, size: 10 });
      if (res.success) {
        const { content, ...rest } = res.data;
        setRecommendations(content);
        setPagedInfo(rest);
        setCurrentPage(page);
      }
    } catch {
      setError('추천글 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  // ----- 구매 과자 로드 -----
  const loadBoughtSnacks = useCallback(async (page: number) => {
    setBsLoading(true);
    setError('');
    try {
      const res = await getBoughtSnacks({ page, size: 10 });
      if (res.success) {
        const { content, ...rest } = res.data;
        setBoughtSnacks(content);
        setBsPagedInfo(rest);
        setBsCurrentPage(page);
      }
    } catch {
      setError('구매 과자 목록을 불러오지 못했습니다.');
    } finally {
      setBsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (initialized) {
      if (activeTab === 'RECOMMEND') loadRecommendations(0);
      else loadBoughtSnacks(0);
    }
  }, [initialized, activeTab, loadRecommendations, loadBoughtSnacks]);

  // ----- 추천글 피드백 & 삭제 -----
  const handleRcFeedback = async (rcId: number, status: 'LIKE' | 'DISLIKE') => {
    try {
      const res = await postRcFeedback(rcId, { status });
      if (res.success) {
        const result = res.data;
        setRecommendations((prev) => prev.map((rc) =>
          rc.rcId === rcId
            ? { ...rc, feedbackSummary: { likeCount: result.likeCount, dislikeCount: result.dislikeCount, myFeedback: result.myFeedback } }
            : rc
        ));
      }
    } catch { } // 에러 무시
  };

  const handleRcDelete = async (rcId: number) => {
    if (!confirm('추천글을 삭제하시겠습니까?')) return;
    try {
      await deleteRecommendation(rcId);
      loadRecommendations(currentPage);
    } catch {
      alert('삭제에 실패했습니다.');
    }
  };

  // ----- 구매 과자 피드백 & 삭제 & 상태변경 -----
  const handleBsFeedback = async (bsId: number, status: 'LIKE' | 'DISLIKE') => {
    try {
      const res = await postBsFeedback(bsId, { status });
      if (res.success) {
        const result = res.data;
        setBoughtSnacks((prev) => prev.map((bs) =>
          bs.bsId === bsId
            ? { ...bs, feedbackSummary: { likeCount: result.likeCount, dislikeCount: result.dislikeCount, myFeedback: result.myFeedback } }
            : bs
        ));
      }
    } catch { } // 에러 무시
  };

  const handleBsDelete = async (bsId: number) => {
    if (!confirm('이 과자를 삭제하시겠습니까?')) return;
    try {
      await deleteBoughtSnack(bsId);
      loadBoughtSnacks(bsCurrentPage);
    } catch {
      alert('삭제에 실패했습니다.');
    }
  };

  const handleBsStatusChange = async (bsId: number, statusStr: string) => {
    try {
      const res = await patchBoughtSnackStatus(bsId, { status: statusStr as any });
      if (res.success) {
        setBoughtSnacks((prev) => prev.map((bs) =>
          bs.bsId === bsId ? { ...bs, status: res.data.status, statusLabel: res.data.statusLabel } : bs
        ));
      }
    } catch {
      alert('상태 변경에 실패했습니다.');
    }
  };

  // ----- 로그아웃 -----
  const handleLogout = async () => {
    try {
      await postLogout();
    } catch { } finally {
      clearAuth();
      router.push('/login');
    }
  };

  if (!initialized) {
    return (
      <div className="page-wrapper">
        <p style={{ color: 'var(--text-muted)', fontFamily: 'var(--font-body)' }}>인증 확인 중...</p>
      </div>
    );
  }

  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="page-wrapper" style={{ alignItems: 'flex-start', padding: '2rem 1rem' }}>
      <div style={{ width: '100%', maxWidth: '760px', margin: '0 auto' }}>
        {/* 헤더 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h1 className="heading-logo" style={{ textAlign: 'left', fontSize: '1.6rem' }}>🍪 두과자</h1>
            <p className="heading-sub" style={{ textAlign: 'left' }}>간식 추천 및 구매 현황</p>
          </div>
          <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
            {user && (
              <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                {user.nickname} {isAdmin && '(관리자)'}
              </span>
            )}
            {isAdmin && (
              <Link href="/admin" style={{ fontSize: '0.82rem', color: 'var(--error)', textDecoration: 'underline' }}>
                관리자 페이지
              </Link>
            )}
            <Link href="/mypage" style={{ fontSize: '0.82rem', color: 'var(--brown-500)', textDecoration: 'underline' }}>
              마이페이지
            </Link>
            <button
              id="btn-logout"
              onClick={handleLogout}
              style={{
                padding: '0.45rem 0.9rem', background: 'none', border: '1.5px solid var(--brown-200)',
                borderRadius: '3px', fontSize: '0.82rem', color: 'var(--text-secondary)', cursor: 'pointer',
              }}
            >
              로그아웃
            </button>
            <Link
              id="btn-new-recommendation"
              href="/recommendations/new"
              style={{
                padding: '0.45rem 1rem', background: 'var(--brown-700)', color: 'var(--ivory-100)',
                borderRadius: '3px', fontSize: '0.85rem', fontWeight: 700, textDecoration: 'none',
              }}
            >
              + 추천하기
            </Link>
          </div>
        </div>

        {/* 탭 네비게이션 */}
        <div style={{ display: 'flex', gap: '1rem', borderBottom: '2px solid var(--brown-100)', marginBottom: '1.5rem' }}>
          <button
            onClick={() => setActiveTab('BOUGHT')}
            style={{
              padding: '0.5rem 1rem', background: 'none', cursor: 'pointer',
              border: 'none', borderBottom: activeTab === 'BOUGHT' ? '2px solid var(--brown-700)' : 'none',
              color: activeTab === 'BOUGHT' ? 'var(--brown-700)' : 'var(--text-muted)',
              fontWeight: activeTab === 'BOUGHT' ? 700 : 400,
              fontSize: '1.05rem', marginBottom: '-2px'
            }}
          >
            구매 현황
          </button>
          <button
            onClick={() => setActiveTab('RECOMMEND')}
            style={{
              padding: '0.5rem 1rem', background: 'none', cursor: 'pointer',
              border: 'none', borderBottom: activeTab === 'RECOMMEND' ? '2px solid var(--brown-700)' : 'none',
              color: activeTab === 'RECOMMEND' ? 'var(--brown-700)' : 'var(--text-muted)',
              fontWeight: activeTab === 'RECOMMEND' ? 700 : 400,
              fontSize: '1.05rem', marginBottom: '-2px'
            }}
          >
            간식 추천
          </button>
        </div>

        {/* 에러 */}
        {error && <div className="alert alert-error">{error}</div>}

        {/* ===================== 추천 탭 ===================== */}
        {activeTab === 'RECOMMEND' && (
          <>
            {loading && <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>로딩 중...</div>}
            {!loading && recommendations.length === 0 && (
              <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                아직 추천글이 없습니다.
              </div>
            )}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              {recommendations.map((rc) => {
                const isOwner = user?.userId === rc.author.userId;
                const canDelete = isOwner || isAdmin;

                return (
                  <div key={rc.rcId} className="card" style={{ padding: '1.5rem 1.8rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--brown-700)' }}>{rc.name}</h2>
                      {(isOwner || canDelete) && (
                        <div style={{ display: 'flex', gap: '0.4rem' }}>
                          {isOwner && (
                            <Link href={`/recommendations/${rc.rcId}/edit`} style={{ fontSize: '0.75rem', color: 'var(--brown-500)', textDecoration: 'underline' }}>
                              수정
                            </Link>
                          )}
                          {canDelete && (
                            <button onClick={() => handleRcDelete(rc.rcId)} style={{ fontSize: '0.75rem', color: 'var(--error)', background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline' }}>
                              삭제
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                    <p style={{ fontSize: '0.92rem', color: 'var(--text-secondary)', marginBottom: '0.8rem', whiteSpace: 'pre-wrap' }}>{rc.reason}</p>
                    <div style={{ display: 'flex', gap: '0.8rem', fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '0.8rem' }}>
                      <span>✍️ {rc.author.nickname}</span>
                      <span>{new Date(rc.createdAt).toLocaleDateString('ko-KR')}</span>
                    </div>
                    <hr style={{ border: 'none', borderTop: '1px solid var(--brown-100)', margin: '0 0 1rem 0' }} />
                    <div style={{ display: 'flex', gap: '0.6rem', marginBottom: '1rem', alignItems: 'center' }}>
                      <button onClick={() => handleRcFeedback(rc.rcId, 'LIKE')} style={{ padding: '0.3rem 0.7rem', borderRadius: '20px', border: '1.5px solid', borderColor: rc.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-500)' : 'var(--brown-100)', background: rc.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-100)' : 'transparent', cursor: 'pointer', fontSize: '0.82rem', color: rc.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-700)' : 'var(--text-muted)' }}>
                        👍 {rc.feedbackSummary.likeCount}
                      </button>
                      <button onClick={() => handleRcFeedback(rc.rcId, 'DISLIKE')} style={{ padding: '0.3rem 0.7rem', borderRadius: '20px', border: '1.5px solid', borderColor: rc.feedbackSummary.myFeedback === 'DISLIKE' ? 'var(--error)' : 'var(--brown-100)', background: rc.feedbackSummary.myFeedback === 'DISLIKE' ? 'rgba(192,57,43,0.08)' : 'transparent', cursor: 'pointer', fontSize: '0.82rem', color: rc.feedbackSummary.myFeedback === 'DISLIKE' ? 'var(--error)' : 'var(--text-muted)' }}>
                        👎 {rc.feedbackSummary.dislikeCount}
                      </button>
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginLeft: '0.4rem' }}>댓글 {rc.commentCount}개</span>
                    </div>
                    <CommentSection rcId={rc.rcId} initialCommentCount={rc.commentCount} initialCommentPreview={rc.commentPreview} user={user} onRefresh={() => loadRecommendations(currentPage)} />
                  </div>
                );
              })}
            </div>
            {!loading && pagedInfo.totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.4rem', marginTop: '2rem' }}>
                <button onClick={() => loadRecommendations(currentPage - 1)} disabled={pagedInfo.first} className="btn-ghost" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>이전</button>
                {Array.from({ length: pagedInfo.totalPages }, (_, i) => (
                  <button key={i} onClick={() => loadRecommendations(i)} style={{ padding: '0.4rem 0.7rem', fontSize: '0.85rem', borderRadius: '3px', border: '1.5px solid', borderColor: i === currentPage ? 'var(--brown-700)' : 'var(--brown-100)', background: i === currentPage ? 'var(--brown-700)' : 'transparent', color: i === currentPage ? 'var(--ivory-100)' : 'var(--text-secondary)', cursor: 'pointer' }}>{i + 1}</button>
                ))}
                <button onClick={() => loadRecommendations(currentPage + 1)} disabled={pagedInfo.last} className="btn-ghost" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>다음</button>
              </div>
            )}
          </>
        )}

        {/* ===================== 구매 탭 ===================== */}
        {activeTab === 'BOUGHT' && (
          <>
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
              {isAdmin && (
                <Link
                  href="/bought-snacks/new"
                  style={{
                    padding: '0.45rem 1rem', background: '#2c3e50', color: 'white',
                    borderRadius: '3px', fontSize: '0.85rem', fontWeight: 700, textDecoration: 'none',
                  }}
                >
                  + 구매 간식 추가 (관리자용)
                </Link>
              )}
            </div>
            {bsLoading && <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>로딩 중...</div>}
            {!bsLoading && boughtSnacks.length === 0 && (
              <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                구매된 간식이 없습니다.
              </div>
            )}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              {boughtSnacks.map((bs) => (
                <div key={bs.bsId} className="card" style={{ padding: '1.5rem 1.8rem', borderLeft: bs.status === 'SHIPPING' ? '4px solid #f39c12' : bs.status === 'OUT_OF_STOCK' ? '4px solid #e74c3c' : '4px solid #2ecc71' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
                      <h2 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--brown-700)', margin: 0 }}>{bs.name}</h2>
                      <span style={{
                        fontSize: '0.75rem', fontWeight: 600, padding: '0.2rem 0.5rem', borderRadius: '12px',
                        background: bs.status === 'SHIPPING' ? '#fdebd0' : bs.status === 'OUT_OF_STOCK' ? '#fadbd8' : '#d5f5e3',
                        color: bs.status === 'SHIPPING' ? '#d68910' : bs.status === 'OUT_OF_STOCK' ? '#c0392b' : '#27ae60'
                      }}>
                        {bs.statusLabel}
                      </span>
                    </div>

                    <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                      <select
                        value={bs.status}
                        onChange={(e) => handleBsStatusChange(bs.bsId, e.target.value)}
                        style={{ fontSize: '0.75rem', padding: '0.1rem', marginRight: '0.4rem', borderRadius: '3px', border: '1px solid var(--brown-200)', background: 'white' }}
                      >
                        <option value="SHIPPING">배송중</option>
                        <option value="IN_STOCK">재고있음</option>
                        <option value="OUT_OF_STOCK">재고없음</option>
                      </select>

                      {isAdmin && (
                        <>
                          <Link href={`/bought-snacks/${bs.bsId}/edit`} style={{ fontSize: '0.75rem', color: 'var(--brown-500)', textDecoration: 'underline' }}>
                            수정
                          </Link>
                          <button onClick={() => handleBsDelete(bs.bsId)} style={{ fontSize: '0.75rem', color: 'var(--error)', background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline' }}>
                            삭제
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '0.8rem' }}>
                    등록일: {new Date(bs.createdAt).toLocaleDateString('ko-KR')}
                  </div>
                  <hr style={{ border: 'none', borderTop: '1px solid var(--brown-100)', margin: '0 0 1rem 0' }} />
                  <div style={{ display: 'flex', gap: '0.6rem', marginBottom: '1rem', alignItems: 'center' }}>
                    <button onClick={() => handleBsFeedback(bs.bsId, 'LIKE')} style={{ padding: '0.3rem 0.7rem', borderRadius: '20px', border: '1.5px solid', borderColor: bs.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-500)' : 'var(--brown-100)', background: bs.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-100)' : 'transparent', cursor: 'pointer', fontSize: '0.82rem', color: bs.feedbackSummary.myFeedback === 'LIKE' ? 'var(--brown-700)' : 'var(--text-muted)' }}>
                      😍 맛있어요 {bs.feedbackSummary.likeCount}
                    </button>
                    <button onClick={() => handleBsFeedback(bs.bsId, 'DISLIKE')} style={{ padding: '0.3rem 0.7rem', borderRadius: '20px', border: '1.5px solid', borderColor: bs.feedbackSummary.myFeedback === 'DISLIKE' ? 'var(--error)' : 'var(--brown-100)', background: bs.feedbackSummary.myFeedback === 'DISLIKE' ? 'rgba(192,57,43,0.08)' : 'transparent', cursor: 'pointer', fontSize: '0.82rem', color: bs.feedbackSummary.myFeedback === 'DISLIKE' ? 'var(--error)' : 'var(--text-muted)' }}>
                      🤔 별로에요 {bs.feedbackSummary.dislikeCount}
                    </button>
                    <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginLeft: '0.4rem' }}>댓글 {bs.commentCount}개</span>
                  </div>
                  <CommentSection bsId={bs.bsId} initialCommentCount={bs.commentCount} initialCommentPreview={bs.commentPreview} user={user} onRefresh={() => loadBoughtSnacks(bsCurrentPage)} />
                </div>
              ))}
            </div>
            {!bsLoading && bsPagedInfo.totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.4rem', marginTop: '2rem' }}>
                <button onClick={() => loadBoughtSnacks(bsCurrentPage - 1)} disabled={bsPagedInfo.first} className="btn-ghost" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>이전</button>
                {Array.from({ length: bsPagedInfo.totalPages }, (_, i) => (
                  <button key={i} onClick={() => loadBoughtSnacks(i)} style={{ padding: '0.4rem 0.7rem', fontSize: '0.85rem', borderRadius: '3px', border: '1.5px solid', borderColor: i === bsCurrentPage ? 'var(--brown-700)' : 'var(--brown-100)', background: i === bsCurrentPage ? 'var(--brown-700)' : 'transparent', color: i === bsCurrentPage ? 'var(--ivory-100)' : 'var(--text-secondary)', cursor: 'pointer' }}>{i + 1}</button>
                ))}
                <button onClick={() => loadBoughtSnacks(bsCurrentPage + 1)} disabled={bsPagedInfo.last} className="btn-ghost" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>다음</button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
