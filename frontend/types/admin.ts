export interface AdminUserListItem {
  userId: number;
  loginId: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
  status: 'ACTIVATED' | 'DEACTIVATED' | 'DELETED';
  createdAt: string;
}
