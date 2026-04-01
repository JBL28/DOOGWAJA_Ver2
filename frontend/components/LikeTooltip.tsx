'use client';

import { useState, useRef, useEffect } from 'react';
import { LikedUserDTO, LikedUsersResponseDTO } from '@/types/recommendation';

interface LikeTooltipProps {
  likedCount: number;
  onFetch: () => Promise<{ success: boolean; data: LikedUsersResponseDTO }>;
  children: React.ReactNode;
}

/**
 * 좋아요 표시한 유저 목록을 보여주는 툴팁 컴포넌트
 */
export default function LikeTooltip({ likedCount, onFetch, children }: LikeTooltipProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [users, setUsers] = useState<LikedUserDTO[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(false);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const fetchData = async () => {
    if (likedCount === 0) return;
    setLoading(true);
    try {
      const res = await onFetch();
      if (res.success) {
        setUsers(res.data.users);
        setHasMore(res.data.hasMore);
      }
    } catch (err) {
      console.error('Failed to fetch liked users', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMouseEnter = () => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    setIsOpen(true);
    fetchData();
  };

  const handleMouseLeave = () => {
    timeoutRef.current = setTimeout(() => {
      setIsOpen(false);
    }, 300);
  };

  // 좋아요 클릭 등으로 숫자가 변했을 때, 열려있다면 데이터 갱신
  useEffect(() => {
    if (isOpen) {
      fetchData();
    }
  }, [likedCount]);

  return (
    <div 
      className="like-tooltip-container"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      style={{ position: 'relative', display: 'inline-block' }}
    >
      {children}
      {isOpen && likedCount > 0 && (
        <div 
          className="like-tooltip-overlay"
          style={{
            position: 'absolute',
            bottom: 'calc(100% + 10px)',
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'rgba(44, 31, 22, 0.95)', // 어두운 브라운 계열
            color: '#fff',
            padding: '0.6rem 0.8rem',
            borderRadius: '8px',
            fontSize: '0.75rem',
            whiteSpace: 'nowrap',
            zIndex: 1000,
            boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
            minWidth: '80px',
            textAlign: 'center',
            backdropFilter: 'blur(4px)',
            border: '1px solid var(--brown-200)'
          }}
        >
          {loading && users.length === 0 ? (
            <span style={{ color: 'var(--ivory-300)' }}>불러오는 중...</span>
          ) : users.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <div style={{ fontWeight: 700, borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '4px', marginBottom: '4px', color: 'var(--ivory-200)' }}>
                좋아요한 사람
              </div>
              {users.map(u => (
                <span key={u.userId} style={{ display: 'block' }}>{u.nickname}</span>
              ))}
              {hasMore && (
                <span style={{ fontSize: '0.7rem', opacity: 0.6, marginTop: '2px' }}>...외 더 있음</span>
              )}
            </div>
          ) : (
            <span>목록이 없습니다.</span>
          )}
          {/* Arrow */}
          <div style={{
            position: 'absolute',
            top: '100%',
            left: '50%',
            marginLeft: '-6px',
            borderWidth: '6px',
            borderStyle: 'solid',
            borderColor: 'rgba(44, 31, 22, 0.95) transparent transparent transparent'
          }} />
        </div>
      )}
    </div>
  );
}
