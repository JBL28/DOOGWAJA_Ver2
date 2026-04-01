'use client';

import { useState } from 'react';
import {
  getRcComments, postRcComment, deleteRcComment, postRccFeedback,
  getBsComments, postBsComment, deleteBsComment, postBscFeedback
} from '@/lib/request';
import type { CommentPreviewItemDTO, CommentItemDTO, FeedbackResultDTO } from '@/types/recommendation';

interface CommentSectionProps {
  rcId?: number;
  bsId?: number;
  initialCommentCount: number;
  initialCommentPreview: CommentPreviewItemDTO[];
  user: any; // Zustand user object
  onRefresh: () => void;
}

export default function CommentSection({
  rcId,
  bsId,
  initialCommentCount,
  initialCommentPreview,
  user,
  onRefresh,
}: CommentSectionProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [expandedComments, setExpandedComments] = useState<CommentItemDTO[]>([]);
  const [expandedLoading, setExpandedLoading] = useState(false);

  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const displayComments = isExpanded ? expandedComments : initialCommentPreview;

  const loadExpandedComments = async () => {
    setExpandedLoading(true);
    try {
      let res;
      if (rcId) {
        res = await getRcComments(rcId, { page: 0, size: 100 });
      } else if (bsId) {
        res = await getBsComments(bsId, { page: 0, size: 100 });
      }
      if (res?.success) {
        setExpandedComments(res.data.content);
        setIsExpanded(true);
      }
    } catch {
      alert('전체 댓글을 불러오지 못했습니다.');
    } finally {
      setExpandedLoading(false);
    }
  };

  const handleExpand = async () => {
    if (isExpanded) {
      setIsExpanded(false);
      return;
    }
    await loadExpandedComments();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) {
      alert('댓글을 입력해주세요.');
      return;
    }
    setSubmitting(true);
    try {
      let res;
      if (rcId) res = await postRcComment(rcId, { content: newComment.trim() });
      else if (bsId) res = await postBsComment(bsId, { content: newComment.trim() });
      
      if (res?.success) {
        setNewComment('');
        // Reload expanded comments if already expanded
        if (isExpanded) {
          await loadExpandedComments();
        }
        // Always refresh main list to update initial count & preview
        onRefresh();
      }
    } catch {
      alert('댓글 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId: number) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;
    try {
      let res;
      if (rcId) res = await deleteRcComment(rcId, commentId);
      else if (bsId) res = await deleteBsComment(bsId, commentId);

      if (res?.success) {
        if (isExpanded) {
          await loadExpandedComments();
        }
        onRefresh();
      }
    } catch {
      alert('삭제에 실패했습니다.');
    }
  };

  // 댓글 피드백 처리
  const handleCommentFeedback = async (commentId: number, status: 'LIKE' | 'DISLIKE') => {
    if (!user) {
      alert('댓글 피드백을 남기려면 로그인해주세요.');
      return;
    }
    try {
      let res;
      if (rcId) res = await postRccFeedback(rcId, commentId, { status });
      else if (bsId) res = await postBscFeedback(bsId, commentId, { status });

      if (res?.success) {
        const result: FeedbackResultDTO = res.data;
        const applyFeedback = (c: CommentItemDTO | CommentPreviewItemDTO) =>
          c.id === commentId
            ? {
                ...c,
                feedbackSummary: {
                  likeCount: result.likeCount,
                  dislikeCount: result.dislikeCount,
                  myFeedback: result.myFeedback,
                },
              }
            : c;
        if (isExpanded) {
          // 확장 모드일 때: expandedComments 부분 갱신
          setExpandedComments((prev) => prev.map(applyFeedback) as CommentItemDTO[]);
        } else {
          // 프리뷰 모드일 때: 상위 onRefresh 호출로 initialCommentPreview 갱신
          onRefresh();
        }
      }
    } catch {
      // 서버 403 등은 axios 인터셉터에서 처리
    }
  };

  return (
    <div style={{ marginTop: '1rem' }}>
      {/* 댓글 작성 폼 */}
      {user ? (
        <form
          onSubmit={handleSubmit}
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '0.6rem',
            marginBottom: '1.2rem',
            alignItems: 'flex-end',
          }}
        >
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="추천글에 대한 생각이나 의견을 남겨보세요..."
            style={{
              width: '100%',
              boxSizing: 'border-box',
              padding: '0.7rem',
              borderRadius: '4px',
              border: '1px solid var(--brown-200)',
              resize: 'vertical',
              minHeight: '44px',
              fontFamily: 'var(--font-body)',
              fontSize: '0.85rem',
              outline: 'none',
            }}
            disabled={submitting}
          />
          <button
            type="submit"
            disabled={submitting}
            className="btn-primary"
            style={{
              padding: '0.7rem 1.2rem',
              fontSize: '0.85rem',
              whiteSpace: 'nowrap',
              height: '44px',
            }}
          >
            {submitting ? '등록 중' : '등록'}
          </button>
        </form>
      ) : (
        <div
          style={{
            padding: '1rem',
            marginBottom: '1.2rem',
            background: 'var(--ivory-300)',
            borderRadius: '4px',
            textAlign: 'center',
            fontSize: '0.85rem',
            color: 'var(--text-muted)',
          }}
        >
          댓글을 작성하려면 로그인해주세요.
        </div>
      )}

      {/* 댓글 리스트 */}
      {displayComments.length > 0 && (
        <div
          style={{
            borderTop: '1px solid var(--brown-100)',
            paddingTop: '0.8rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.6rem',
          }}
        >
          {displayComments.map((c) => {
            const isCommentOwner = user?.userId === c.author.userId;
            const isAdmin = user?.role === 'ADMIN';
            const canDelete = isCommentOwner || isAdmin;

            return (
              <div
                key={c.id}
                style={{
                  padding: '0.8rem',
                  background: 'rgba(244,235,220,0.5)',
                  borderRadius: '4px',
                  fontSize: '0.85rem',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    marginBottom: '0.4rem',
                    alignItems: 'center',
                  }}
                >
                  <span style={{ fontWeight: 700, color: 'var(--brown-500)' }}>
                    {c.author.nickname}
                  </span>
                  {canDelete && (
                    <button
                      onClick={() => handleDelete(c.id)}
                      style={{
                        background: 'none',
                        border: 'none',
                        color: 'var(--error)',
                        fontSize: '0.75rem',
                        cursor: 'pointer',
                        textDecoration: 'underline',
                        fontFamily: 'var(--font-body)',
                      }}
                    >
                      삭제
                    </button>
                  )}
                </div>
                <div
                  style={{
                    color: 'var(--text-secondary)',
                    whiteSpace: 'pre-wrap',
                    lineHeight: '1.5',
                  }}
                >
                  {c.content}
                </div>
                <div
                  style={{
                    marginTop: '0.5rem',
                    display: 'flex',
                    gap: '0.4rem',
                    alignItems: 'center',
                  }}
                >
                  <button
                    onClick={() => handleCommentFeedback(c.id, 'LIKE')}
                    disabled={!user}
                    style={{
                      padding: '0.2rem 0.55rem',
                      borderRadius: '20px',
                      border: '1.5px solid',
                      borderColor:
                        c.feedbackSummary.myFeedback === 'LIKE'
                          ? 'var(--brown-500)'
                          : 'var(--brown-100)',
                      background:
                        c.feedbackSummary.myFeedback === 'LIKE'
                          ? 'var(--brown-100)'
                          : 'transparent',
                      cursor: !user ? 'not-allowed' : 'pointer',
                      fontSize: '0.75rem',
                      fontFamily: 'var(--font-body)',
                      color:
                        c.feedbackSummary.myFeedback === 'LIKE'
                          ? 'var(--brown-700)'
                          : 'var(--text-muted)',
                      transition: 'all 0.15s',
                    }}
                  >
                    👍 {c.feedbackSummary.likeCount}
                  </button>
                  <button
                    onClick={() => handleCommentFeedback(c.id, 'DISLIKE')}
                    disabled={!user}
                    style={{
                      padding: '0.2rem 0.55rem',
                      borderRadius: '20px',
                      border: '1.5px solid',
                      borderColor:
                        c.feedbackSummary.myFeedback === 'DISLIKE'
                          ? 'var(--error)'
                          : 'var(--brown-100)',
                      background:
                        c.feedbackSummary.myFeedback === 'DISLIKE'
                          ? 'rgba(192,57,43,0.08)'
                          : 'transparent',
                      cursor: !user ? 'not-allowed' : 'pointer',
                      fontSize: '0.75rem',
                      fontFamily: 'var(--font-body)',
                      color:
                        c.feedbackSummary.myFeedback === 'DISLIKE'
                          ? 'var(--error)'
                          : 'var(--text-muted)',
                      transition: 'all 0.15s',
                    }}
                  >
                    👎 {c.feedbackSummary.dislikeCount}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* 댓글 더보기 / 접기 */}
      {initialCommentCount > 3 && (
        <div style={{ marginTop: '0.8rem', textAlign: 'center' }}>
          <button
            onClick={handleExpand}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: '0.82rem',
              color: 'var(--brown-700)',
              fontFamily: 'var(--font-body)',
              textDecoration: 'underline',
              fontWeight: 600,
            }}
          >
            {expandedLoading
              ? '로딩 중...'
              : isExpanded
                ? '댓글 접기'
                : `댓글 더 보기 (${initialCommentCount}개)`}
          </button>
        </div>
      )}
    </div>
  );
}
