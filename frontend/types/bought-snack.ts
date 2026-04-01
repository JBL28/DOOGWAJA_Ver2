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

export interface CommentItemDTO extends CommentPreviewItemDTO {}

export interface BoughtSnackListItemDTO {
  bsId: number;
  name: string;
  status: 'SHIPPING' | 'IN_STOCK' | 'OUT_OF_STOCK';
  statusLabel: string;
  feedbackSummary: FeedbackSummaryDTO;
  commentCount: number;
  commentPreview: CommentPreviewItemDTO[];
  createdAt: string;
  updatedAt: string;
}

export interface BoughtSnackDetailDTO extends BoughtSnackListItemDTO {}
