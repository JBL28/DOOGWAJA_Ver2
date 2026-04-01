/**
 * plan.md 4-3 기준
 * 추천 도메인 타입 정의
 */

export interface AuthorDTO {
  userId: number;
  nickname: string;
}

export interface FeedbackSummaryDTO {
  likeCount: number;
  dislikeCount: number;
  myFeedback: 'LIKE' | 'DISLIKE' | null;
}

export interface CommentPreviewItemDTO {
  id: number;
  content: string;
  author: AuthorDTO;
  feedbackSummary: FeedbackSummaryDTO;
  createdAt: string;
  updatedAt: string;
}

export type CommentItemDTO = CommentPreviewItemDTO;

export interface RecommendationListItemDTO {
  rcId: number;
  name: string;
  reason: string;
  author: AuthorDTO;
  feedbackSummary: FeedbackSummaryDTO;
  commentCount: number;
  commentPreview: CommentPreviewItemDTO[];
  createdAt: string;
  updatedAt: string;
}

export type RecommendationDetailDTO = RecommendationListItemDTO;

export interface FeedbackResultDTO {
  myFeedback: 'LIKE' | 'DISLIKE' | null;
  likeCount: number;
  dislikeCount: number;
}

export interface PagedData<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
