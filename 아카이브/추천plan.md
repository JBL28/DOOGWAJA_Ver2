# 추천글 도메인 구현 플랜

> **플래너 작성일:** 2026-04-01  
> **대상 기능:** 추천 게시글 CRUD + 추천 댓글 CRUD + 추천 게시글 피드백 + 추천 댓글 피드백  
> **근거 문서:**
> - `.agent/shared/requirements.md`
> - `.agent/shared/api_docs/api_spec.md`
> - `.agent/planner/README.md`
> - `.agent/planner/guidelines/zustand-auth-recovery.md`

---

## 0. 현재 상태 요약

- 백엔드: `auth`, `user`, `common`, `security` 패키지 구현 완료. `recommendation` 도메인 미존재.
- 프론트엔드: `/login`, `/register`, `/mypage` 페이지 구현 완료. 메인 페이지(`/`)는 Next.js 기본 템플릿 상태.
- Zustand 인증 상태 복구 이슈가 이전 스텝에서 발생한 바 있으므로, 인증이 필요한 모든 페이지의 진입 로직에 refresh 복구 패턴 적용 필수.

---

## 1. 구현 범위 (Scope)

### 1-1. 백엔드 (Spring Boot)

| 번호 | 메서드 | URL | 권한 | 설명 |
|---|---|---|---|---|
| R-1 | GET | `/api/recommendations?page=0&size=10` | USER, ADMIN | 추천글 목록 조회 |
| R-2 | GET | `/api/recommendations/{rcId}` | USER, ADMIN | 추천글 단건 조회 |
| R-3 | POST | `/api/recommendations` | USER only | 추천글 생성 |
| R-4 | PUT | `/api/recommendations/{rcId}` | USER (본인만) | 추천글 수정 |
| R-5 | DELETE | `/api/recommendations/{rcId}` | USER (본인만) | 추천글 삭제 |
| RC-1 | GET | `/api/recommendations/{rcId}/comments?page=0&size=10` | USER, ADMIN | 추천 댓글 전체 조회 |
| RC-2 | POST | `/api/recommendations/{rcId}/comments` | USER only | 추천 댓글 작성 |
| RC-3 | PUT | `/api/recommendations/{rcId}/comments/{rccId}` | USER (본인만) | 추천 댓글 수정 |
| RC-4 | DELETE | `/api/recommendations/{rcId}/comments/{rccId}` | USER (본인만) | 추천 댓글 삭제 |
| RF-1 | POST | `/api/recommendations/{rcId}/feedback` | USER only | 추천글 피드백 toggle |
| RCF-1 | POST | `/api/recommendations/{rcId}/comments/{rccId}/feedback` | USER only | 추천 댓글 피드백 toggle |

### 1-2. 프론트엔드 (Next.js)

| 번호 | 경로 | 설명 |
|---|---|---|
| F-1 | `/` (메인 페이지) | 추천글 리스트 + 댓글 preview + 피드백 표시 |
| F-2 | `/recommendations/new` | 추천글 작성 페이지 |
| F-3 | `/recommendations/[rcId]/edit` | 추천글 수정 페이지 |

> **참고:** 요구사항 기준 "과자 추천/구매 수정 페이지"는 단일 수정 페이지로 명시. 추천글 수정 라우트는 `/recommendations/[rcId]/edit`으로 설정.

---

## 2. 전제 조건 및 의존성

- **DB:** `recommendation`, `rc_comment`, `rc_feedback`, `rcc_feedback` 테이블이 ERD 명세대로 존재해야 한다.
- **선행 구현:** `user` 도메인 (Entity, Repository)이 완료되어 있어야 한다.
- **백엔드 공통 인프라:** `ApiResponse`, `GlobalExceptionHandler`, `JwtAuthenticationFilter`, `SecurityConfig`가 이전 플랜에서 이미 구현 완료되어 있어야 한다.
- **프론트엔드 기반:** `lib/axios.ts`, `lib/request.ts`, `store/authStore.ts`가 이미 구현 완료되어 있어야 한다.

---

## 3. 백엔드 구현 세부 계획

### 3-1. 패키지 구조

기존 패키지 구조를 유지하며, `recommendation` 도메인 패키지를 신규 추가한다.

```
dev.ssafy
├── recommendation
│   ├── controller   RecommendationController.java
│   ├── service      RecommendationService.java
│   ├── repository   RecommendationRepository.java
│   ├── dto
│   │   ├── RecommendationCreateRequest.java
│   │   ├── RecommendationUpdateRequest.java
│   │   ├── RecommendationListItemDTO.java
│   │   ├── RecommendationDetailDTO.java
│   │   ├── AuthorDTO.java
│   │   ├── FeedbackSummaryDTO.java
│   │   └── CommentPreviewItemDTO.java
│   └── entity       Recommendation.java
├── rc_comment
│   ├── controller   RcCommentController.java  (RecommendationController에 통합 가능)
│   ├── service      RcCommentService.java
│   ├── repository   RcCommentRepository.java
│   ├── dto
│   │   ├── RcCommentCreateRequest.java
│   │   ├── RcCommentUpdateRequest.java
│   │   └── CommentItemDTO.java
│   └── entity       RcComment.java
├── rc_feedback
│   ├── service      RcFeedbackService.java
│   ├── repository   RcFeedbackRepository.java
│   ├── dto
│   │   ├── FeedbackRequest.java
│   │   └── FeedbackResultDTO.java
│   └── entity       RcFeedback.java
└── rcc_feedback
    ├── service      RccFeedbackService.java
    ├── repository   RccFeedbackRepository.java
    └── entity       RccFeedback.java
```

> **미정:** 패키지를 도메인별로 분리할지(`rc_comment`, `rc_feedback`, `rcc_feedback` 각각) vs `recommendation` 하나로 통합할지 generator가 판단한다. 단, ERD 기준 엔티티는 분리해야 한다.

### 3-2. Entity 정의

#### Recommendation.java
- `rcId` (PK, AUTO_INCREMENT)
- `user` (FK → User, LAZY)
- `name` (VARCHAR, NOT NULL)
- `reason` (TEXT, NOT NULL)
- `createdAt` (DATETIME, NOT NULL)
- `updatedAt` (DATETIME, NOT NULL)
- `@PrePersist` / `@PreUpdate`로 시간 자동 처리

#### RcComment.java
- `rccId` (PK, AUTO_INCREMENT)
- `recommendation` (FK → Recommendation, LAZY)
- `user` (FK → User, LAZY)
- `content` (TEXT, NOT NULL)
- `createdAt`, `updatedAt`

#### RcFeedback.java
- `id` (PK, AUTO_INCREMENT)
- `recommendation` (FK → Recommendation, LAZY)
- `user` (FK → User, LAZY)
- `status` (ENUM: LIKE / DISLIKE, NOT NULL)
- **UNIQUE 제약:** `(user_id, rc_id)`

#### RccFeedback.java
- `id` (PK, AUTO_INCREMENT)
- `rcComment` (FK → RcComment, LAZY)
- `user` (FK → User, LAZY)
- `status` (ENUM: LIKE / DISLIKE, NOT NULL)
- **UNIQUE 제약:** `(user_id, rcc_id)`

### 3-3. DTO 정의

API 명세서 기준 DTO를 아래와 같이 구성한다.

#### AuthorDTO
```json
{ "userId": 1, "nickname": "과자왕" }
```

#### FeedbackSummaryDTO
```json
{ "likeCount": 5, "dislikeCount": 1, "myFeedback": "LIKE" | "DISLIKE" | null }
```
- `myFeedback`은 비로그인 또는 피드백 없는 경우 `null`

#### CommentPreviewItemDTO (목록용 댓글 preview)
```json
{
  "id": 10,
  "content": "저도 먹고 싶어요!",
  "author": { "userId": 3, "nickname": "간식러버" },
  "feedbackSummary": { "likeCount": 2, "dislikeCount": 0, "myFeedback": null },
  "createdAt": "2024-01-02T11:00:00",
  "updatedAt": "2024-01-02T11:00:00"
}
```

#### RecommendationListItemDTO
```json
{
  "rcId": 1,
  "name": "허니버터칩",
  "reason": "달콤하고 짠 맛의 조화가 일품입니다.",
  "author": { "userId": 2, "nickname": "과자왕" },
  "feedbackSummary": { "likeCount": 5, "dislikeCount": 1, "myFeedback": "LIKE" },
  "commentCount": 8,
  "commentPreview": [ /* CommentPreviewItemDTO × 최대 3개 (최신순) */ ],
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

#### RecommendationDetailDTO
- 구조는 `RecommendationListItemDTO`와 동일 (현재 MVP 기준)
- 추후 상세 전용 필드 확장 시 이 DTO에서 확장

#### CommentItemDTO (댓글 전체 조회용)
- `CommentPreviewItemDTO`와 동일한 구조

#### FeedbackRequest
```json
{ "status": "LIKE" }  // "LIKE" | "DISLIKE"
```

#### FeedbackResultDTO
```json
{ "myFeedback": "LIKE" | null, "likeCount": 6, "dislikeCount": 1 }
```

### 3-4. 엔드포인트별 상세 정책

---

#### R-1. GET `/api/recommendations?page=0&size=10` — 추천글 목록 조회

- **권한:** USER, ADMIN
- **입력:** `page` (기본값 0), `size` (기본값 10) 쿼리 파라미터
- **출력:** 페이지네이션 응답, `content` 원소: `RecommendationListItemDTO`
- **정렬 기준:** 명세에 명시 없음 → **미정 사항** 참조
- **commentPreview:** 최대 3개, 최신순 (`createdAt DESC` 기준 3개)
- **feedbackSummary.myFeedback:** 현재 로그인한 유저의 피드백 상태 (없으면 null)
- **실패 케이스:** 없음 (인증 실패 시 401은 공통 처리)

---

#### R-2. GET `/api/recommendations/{rcId}` — 추천글 단건 조회

- **권한:** USER, ADMIN
- **입력:** `rcId` (URL path)
- **출력:** `RecommendationDetailDTO`
- **commentPreview:** 최대 3개 (목록과 동일)
- **실패 케이스:**
  - `rcId`에 해당하는 게시글 없음 → `404 "추천 게시글을 찾을 수 없습니다."`

---

#### R-3. POST `/api/recommendations` — 추천글 생성

- **권한:** USER only (ADMIN 불가)
- **입력:**
  ```json
  { "name": "허니버터칩", "reason": "달콤하고 짠 맛의 조화가 일품입니다." }
  ```
- **유효성 검증:** `name`, `reason` 모두 필수 (공백 불가)
- **처리:**
  1. SecurityContext에서 인증된 userId 추출
  2. Recommendation 엔티티 생성 후 저장
- **출력:** `201 Created`, `data: { "rcId": 1 }`, `"추천글이 등록되었습니다."`
- **실패 케이스:**
  - ADMIN 계정 요청 → `403 "접근 권한이 없습니다."`
  - 인증 없이 요청 → `401` (공통 처리)

---

#### R-4. PUT `/api/recommendations/{rcId}` — 추천글 수정

- **권한:** USER (작성자 본인만)
- **입력:**
  ```json
  { "name": "허니버터칩 (수정)", "reason": "수정된 추천 이유입니다." }
  ```
- **유효성 검증:** `name`, `reason` 모두 필수 (공백 불가)
- **처리:**
  1. `rcId`로 게시글 조회 → 없으면 404
  2. `SecurityContext userId == recommendation.userId` 확인 → 불일치 시 403
  3. `name`, `reason` 업데이트
- **출력:** `200 OK`, `data: { "rcId": 1 }`, `"추천글이 수정되었습니다."`
- **실패 케이스:**
  - `rcId` 없음 → `404 "추천 게시글을 찾을 수 없습니다."`
  - 본인 아님 → `403 "본인의 게시글만 수정할 수 있습니다."`

---

#### R-5. DELETE `/api/recommendations/{rcId}` — 추천글 삭제

- **권한:** USER (작성자 본인만)
- **처리:**
  1. `rcId`로 게시글 조회 → 없으면 404
  2. 본인 확인 → 불일치 시 403
  3. 삭제 처리
- **삭제 방식:** hard delete (요구사항/명세에 soft delete 명시 없음)
- **연관 데이터 처리:** `rc_comment`, `rc_feedback` 데이터는 CASCADE로 삭제 (DB 레벨 or 서비스 레벨 처리 — generator 판단)
- **출력:** `200 OK`, `data: null`, `"추천글이 삭제되었습니다."`
- **실패 케이스:**
  - `rcId` 없음 → `404 "추천 게시글을 찾을 수 없습니다."`
  - 본인 아님 → `403 "본인의 게시글만 삭제할 수 있습니다."`

---

#### RC-1. GET `/api/recommendations/{rcId}/comments?page=0&size=10` — 추천 댓글 전체 조회

- **권한:** USER, ADMIN
- **입력:** `rcId` (URL path), `page`, `size` (쿼리)
- **정렬:** 오래된 순 (`createdAt ASC`)
- **출력:** 페이지네이션 응답, `content` 원소: `CommentItemDTO`
- **실패 케이스:**
  - `rcId` 없음 → `404 "추천 게시글을 찾을 수 없습니다."`

---

#### RC-2. POST `/api/recommendations/{rcId}/comments` — 추천 댓글 작성

- **권한:** USER only (ADMIN 불가 — "추천글은 일반 사용자 간의 교류")
- **입력:** `{ "content": "저도 먹고 싶어요!" }`
- **유효성 검증:** `content` 필수 (공백 불가)
- **처리:**
  1. `rcId`로 게시글 존재 확인 → 없으면 404
  2. RcComment 엔티티 생성 후 저장
- **출력:** `201 Created`, `data: { "rccId": 10 }`, `"댓글이 작성되었습니다."`
- **실패 케이스:**
  - ADMIN 요청 → `403 "접근 권한이 없습니다."`
  - `rcId` 없음 → `404 "추천 게시글을 찾을 수 없습니다."`

---

#### RC-3. PUT `/api/recommendations/{rcId}/comments/{rccId}` — 추천 댓글 수정

- **권한:** USER (작성자 본인만)
- **입력:** `{ "content": "수정된 댓글입니다." }`
- **유효성 검증:** `content` 필수 (공백 불가)
- **처리:**
  1. `rccId`로 댓글 조회 → 없으면 404
  2. 본인 확인 → 불일치 시 403
  3. `content` 업데이트
- **출력:** `200 OK`, `data: { "rccId": 10 }`, `"댓글이 수정되었습니다."`
- **실패 케이스:**
  - 댓글 없음 → `404 "댓글을 찾을 수 없습니다."`
  - 본인 아님 → `403 "본인의 댓글만 수정할 수 있습니다."`

> **주의:** URL의 `rcId`와 댓글의 `recommendation.rcId` 일치 여부를 검증할지 여부는 **미정 사항** 참조.

---

#### RC-4. DELETE `/api/recommendations/{rcId}/comments/{rccId}` — 추천 댓글 삭제

- **권한:** USER (작성자 본인만)
- **처리:**
  1. `rccId`로 댓글 조회 → 없으면 404
  2. 본인 확인 → 불일치 시 403
  3. 삭제 처리 (hard delete)
  4. 연관 `rcc_feedback` 데이터 CASCADE 삭제
- **출력:** `200 OK`, `data: null`, `"댓글이 삭제되었습니다."`
- **실패 케이스:**
  - 댓글 없음 → `404 "댓글을 찾을 수 없습니다."`
  - 본인 아님 → `403 "본인의 댓글만 삭제할 수 있습니다."`

---

#### RF-1. POST `/api/recommendations/{rcId}/feedback` — 추천글 피드백 toggle

- **권한:** USER only (ADMIN 불가)
- **입력:** `{ "status": "LIKE" }` — `"LIKE"` | `"DISLIKE"`
- **Toggle 정책:**

  | 기존 상태 | 요청 | 결과 |
  |---|---|---|
  | 없음 | LIKE | LIKE 생성 |
  | LIKE | LIKE | 삭제 (toggle off) |
  | LIKE | DISLIKE | DISLIKE로 변경 |
  | DISLIKE | DISLIKE | 삭제 (toggle off) |
  | DISLIKE | LIKE | LIKE로 변경 |

- **UNIQUE 제약:** `(user_id, rc_id)` — DB 레벨에서 보장
- **처리:**
  1. `rcId`로 게시글 존재 확인 → 없으면 404
  2. 기존 `rc_feedback` 조회 (`userId + rcId` 기준)
  3. Toggle 정책에 따라 생성/변경/삭제
  4. 최신 likeCount, dislikeCount 계산하여 반환
- **출력:** `200 OK`
  ```json
  { "myFeedback": "LIKE" | null, "likeCount": 6, "dislikeCount": 1 }
  ```
- **실패 케이스:**
  - ADMIN 요청 → `403 "접근 권한이 없습니다."`
  - `rcId` 없음 → `404 "추천 게시글을 찾을 수 없습니다."`

---

#### RCF-1. POST `/api/recommendations/{rcId}/comments/{rccId}/feedback` — 추천 댓글 피드백 toggle

- **권한:** USER only (ADMIN 불가)
- **입력:** `{ "status": "DISLIKE" }` — `"LIKE"` | `"DISLIKE"`
- **Toggle 정책:** RF-1과 동일 (대상이 댓글)
- **UNIQUE 제약:** `(user_id, rcc_id)`
- **처리:**
  1. `rccId`로 댓글 존재 확인 → 없으면 404
  2. 기존 `rcc_feedback` 조회 (`userId + rccId` 기준)
  3. Toggle 정책에 따라 생성/변경/삭제
  4. 최신 likeCount, dislikeCount 반환
- **출력:** `200 OK`
  ```json
  { "myFeedback": "DISLIKE" | null, "likeCount": 2, "dislikeCount": 1 }
  ```
- **실패 케이스:**
  - ADMIN 요청 → `403 "접근 권한이 없습니다."`
  - 댓글 없음 → `404 "댓글을 찾을 수 없습니다."`

---

### 3-5. SecurityConfig 추가 설정

기존 SecurityConfig의 `permitAll` 경로는 유지하며, 아래 추천 관련 경로는 **인증 필요**로 처리 (기본적으로 인증 필요이므로 별도 추가 설정 불필요).

추가 고려사항:
- `POST /api/recommendations`, `POST /api/recommendations/{rcId}/comments`, `POST /api/recommendations/{rcId}/feedback`, `POST /api/recommendations/{rcId}/comments/{rccId}/feedback` — **ADMIN 역할 차단** 로직을 서비스 레이어에서 처리한다. (`hasRole` 어노테이션 또는 서비스 내 역할 확인)

---

### 3-6. 예외 처리 추가

기존 `GlobalExceptionHandler`에 아래 케이스를 추가한다 (이미 처리되어 있으면 재사용):
- `EntityNotFoundException` (`rcId`, `rccId` 없음) → `404`
- `AccessDeniedException` (본인 아님, ADMIN 접근 금지) → `403`
- `FeedbackStatusInvalidException` (잘못된 status 값) → `400` (명세에는 없으나 enum 외 값 입력 시 처리 필요)

---

## 4. 프론트엔드 구현 세부 계획

### 4-1. 추가/수정 디렉토리 구조

```
frontend/app
├── page.tsx                          ← 메인 페이지 (추천글 리스트 표시로 전면 개편)
├── recommendations
│   ├── new
│   │   └── page.tsx                  ← 추천글 작성 페이지 [NEW]
│   └── [rcId]
│       └── edit
│           └── page.tsx              ← 추천글 수정 페이지 [NEW]
frontend/lib
├── request.ts                        ← 추천 관련 API 함수 추가 [MODIFY]
frontend/types
├── recommendation.ts                 ← 추천 관련 타입 정의 [NEW]
```

### 4-2. 추가 API 함수 (`lib/request.ts`)

기존 파일에 아래 함수를 추가한다.

```typescript
// --- 추천글 ---
getRecommendations(params: { page: number; size: number }): Promise<ApiResponse<PagedData<RecommendationListItemDTO>>>
getRecommendation(rcId: number): Promise<ApiResponse<RecommendationDetailDTO>>
postRecommendation(data: { name: string; reason: string }): Promise<ApiResponse<{ rcId: number }>>
putRecommendation(rcId: number, data: { name: string; reason: string }): Promise<ApiResponse<{ rcId: number }>>
deleteRecommendation(rcId: number): Promise<ApiResponse<null>>

// --- 추천 댓글 ---
getRcComments(rcId: number, params: { page: number; size: number }): Promise<ApiResponse<PagedData<CommentItemDTO>>>
postRcComment(rcId: number, data: { content: string }): Promise<ApiResponse<{ rccId: number }>>
putRcComment(rcId: number, rccId: number, data: { content: string }): Promise<ApiResponse<{ rccId: number }>>
deleteRcComment(rcId: number, rccId: number): Promise<ApiResponse<null>>

// --- 추천글 피드백 ---
postRcFeedback(rcId: number, data: { status: 'LIKE' | 'DISLIKE' }): Promise<ApiResponse<FeedbackResultDTO>>

// --- 추천 댓글 피드백 ---
postRccFeedback(rcId: number, rccId: number, data: { status: 'LIKE' | 'DISLIKE' }): Promise<ApiResponse<FeedbackResultDTO>>
```

### 4-3. 타입 정의 (`types/recommendation.ts`) [NEW]

```typescript
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
```

### 4-4. 메인 페이지 (`/`) 개편 — [MODIFY]

현재 메인 페이지는 Next.js 기본 템플릿 상태. 추천글 리스트를 표시하도록 전면 개편.

#### 컴포넌트 구성
- **`'use client'` 선언 필수** (Zustand 접근, useEffect 사용)
- 페이지 진입 시 인증 상태 복구 로직 적용 (아래 정책 참조)
- 추천글 카드 목록 (페이지네이션: 10개씩)
- 각 카드에 feedbackSummary + commentPreview(최대 3개) 표시
- 댓글 3개 초과 시 펼치기/접기 버튼 (default: 접기, 접은 상태에서도 3개는 보임)

#### 인증 상태 복구 정책 (zustand-auth-recovery.md 준수)
```
페이지 진입 시:
1. accessToken(Zustand)이 있으면 → 그대로 진행
2. accessToken이 없으면 → postRefresh() 호출 (쿠키 자동 전송)
   - refresh 성공 → 새 accessToken을 Zustand에 저장 → 진행
   - refresh 실패 → /login으로 리다이렉트
```

#### UI 요구사항
- 디자인 컨셉: 제빵소 종이 메뉴판 / 아이보리 메인 색상 (기존 globals.css 컨셉 유지)
- 추천글 카드에 표시할 항목: 과자 이름, 추천 이유, 작성자, 좋아요/싫어요 수 + 현재 유저 상태, 댓글 preview
- 피드백 버튼 (👍/👎): 클릭 시 즉시 UI 반영 (optimistic update 여부는 **미정 사항** 참조)
- 추천글 생성 버튼: 메인 페이지에 표시, `/recommendations/new`로 이동 (`<Link>` 사용)
- 본인 게시글의 경우 수정/삭제 버튼 표시

#### 댓글 펼치기/접기 정책
- 기본: 접힌 상태, preview(최대 3개) 표시
- 댓글 3개 이하: 펼치기 버튼 미표시
- 댓글 3개 초과: "댓글 더 보기" 버튼 표시 → 클릭 시 `/api/recommendations/{rcId}/comments` API 호출로 전체 댓글 로드
- 접기 버튼: 전체 댓글 표시 상태에서 표시, 클릭 시 다시 preview(3개)만 표시

### 4-5. 추천글 작성 페이지 (`/recommendations/new`) [NEW]

- **`'use client'` 선언 필수**
- 인증 복구 정책 적용 (미인증 → `/login`)
- ADMIN 역할 로그인 상태에서 접근 시 접근 불가 처리 (서버 403 응답 처리 또는 프론트 역할 체크)
- 입력 필드: 과자 이름, 추천 이유
- 유효성 검증: 빈 입력 방지 (클라이언트 사이드)
- 제출 성공 시: 메인 페이지(`/`)로 이동 (`<Link>` 또는 `router.push`)
- 이동 방식: `router.push('/')` (클라이언트 사이드 라우팅, Zustand 유지)

### 4-6. 추천글 수정 페이지 (`/recommendations/[rcId]/edit`) [NEW]

- **`'use client'` 선언 필수**
- 인증 복구 정책 적용 (미인증 → `/login`)
- 페이지 진입 시 `rcId`로 기존 데이터 조회 → 입력 필드에 pre-fill
- 본인 게시글 아닌 경우: 서버 403 응답을 받아 메인 페이지로 이동
- 입력 필드: 과자 이름, 추천 이유 (기존 값 pre-fill)
- 수정 성공 시: 메인 페이지(`/`)로 이동

### 4-7. 내부 페이지 이동 규칙

모든 내부 페이지 이동은 `<Link href="...">` 사용. `<a href="...">` 사용 금지.

---

## 5. 구현 순서 (generator 참고용)

### Phase 1: 백엔드 Entity & Repository

1. `Recommendation.java` Entity 작성 (User FK 포함)
2. `RcComment.java` Entity 작성 (Recommendation FK 포함)
3. `RcFeedback.java` Entity 작성 (UNIQUE 제약 포함)
4. `RccFeedback.java` Entity 작성 (UNIQUE 제약 포함)
5. `RecommendationRepository.java` 작성
6. `RcCommentRepository.java` 작성
7. `RcFeedbackRepository.java` 작성
8. `RccFeedbackRepository.java` 작성

### Phase 2: 백엔드 DTO

9. `AuthorDTO.java` (공통 — 이미 있으면 재사용)
10. `FeedbackSummaryDTO.java`
11. `CommentPreviewItemDTO.java`
12. `CommentItemDTO.java`
13. `RecommendationListItemDTO.java`
14. `RecommendationDetailDTO.java`
15. `RecommendationCreateRequest.java`, `RecommendationUpdateRequest.java`
16. `FeedbackRequest.java`, `FeedbackResultDTO.java`

### Phase 3: 백엔드 Service & Controller

17. `RecommendationService.java` — 목록/단건 조회, 생성, 수정, 삭제
18. `RcCommentService.java` — 댓글 조회, 작성, 수정, 삭제
19. `RcFeedbackService.java` — Toggle 피드백
20. `RccFeedbackService.java` — Toggle 피드백
21. `RecommendationController.java` — 모든 엔드포인트 등록

### Phase 4: 프론트엔드 타입 & API

22. `types/recommendation.ts` 작성
23. `lib/request.ts`에 추천 관련 함수 추가

### Phase 5: 프론트엔드 페이지

24. 메인 페이지 (`app/page.tsx`) 개편 — 추천글 리스트 컴포넌트
25. 추천글 작성 페이지 (`app/recommendations/new/page.tsx`)
26. 추천글 수정 페이지 (`app/recommendations/[rcId]/edit/page.tsx`)

---

## 6. 완료 조건 (tester 검수 기준)

### 백엔드

- [ ] `GET /api/recommendations` — 로그인 유저가 호출 시 `RecommendationListItemDTO` 배열 + 페이지네이션 응답
- [ ] `GET /api/recommendations` — `commentPreview`에 최대 3개만 포함 (`commentCount`는 실제 총 댓글 수)
- [ ] `GET /api/recommendations` — 비로그인 접근 시 `401`
- [ ] `GET /api/recommendations/{rcId}` — 존재하지 않는 `rcId` 요청 시 `404 "추천 게시글을 찾을 수 없습니다."`
- [ ] `POST /api/recommendations` — USER로 요청 시 `201`, `data.rcId` 반환
- [ ] `POST /api/recommendations` — ADMIN으로 요청 시 `403 "접근 권한이 없습니다."`
- [ ] `PUT /api/recommendations/{rcId}` — 작성자 본인 수정 성공, `200`
- [ ] `PUT /api/recommendations/{rcId}` — 타인 수정 시 `403 "본인의 게시글만 수정할 수 있습니다."`
- [ ] `DELETE /api/recommendations/{rcId}` — 작성자 본인 삭제 성공, `200`
- [ ] `DELETE /api/recommendations/{rcId}` — 타인 삭제 시 `403 "본인의 게시글만 삭제할 수 있습니다."`
- [ ] `DELETE /api/recommendations/{rcId}` — 삭제 후 관련 `rc_comment`, `rc_feedback` 데이터도 함께 삭제됨
- [ ] `GET /api/recommendations/{rcId}/comments` — 오래된 순 (`createdAt ASC`) 정렬
- [ ] `POST /api/recommendations/{rcId}/comments` — USER 요청 시 `201`, `data.rccId` 반환
- [ ] `POST /api/recommendations/{rcId}/comments` — ADMIN 요청 시 `403 "접근 권한이 없습니다."`
- [ ] `PUT /api/recommendations/{rcId}/comments/{rccId}` — 본인 수정 `200`, 타인 수정 `403`
- [ ] `DELETE /api/recommendations/{rcId}/comments/{rccId}` — 본인 삭제 `200`, 타인 `403`
- [ ] `POST /api/recommendations/{rcId}/feedback` — LIKE → LIKE 재요청 시 feedback 삭제, `myFeedback: null` 응답
- [ ] `POST /api/recommendations/{rcId}/feedback` — LIKE → DISLIKE 요청 시 DISLIKE로 변경
- [ ] `POST /api/recommendations/{rcId}/feedback` — ADMIN 요청 시 `403`
- [ ] `POST /api/recommendations/{rcId}/comments/{rccId}/feedback` — Toggle 정책 동일하게 동작
- [ ] 모든 응답이 `{ success, data, message }` 형식 준수

### 프론트엔드

- [ ] 메인 페이지에서 추천글 목록이 10개씩 페이지네이션으로 표시됨
- [ ] 각 추천글 카드에 feedbackSummary (likeCount, dislikeCount, myFeedback) 표시
- [ ] 각 추천글 카드에 commentPreview 최대 3개 표시
- [ ] 댓글 3개 초과 시 "더 보기" 버튼으로 전체 댓글 로드 가능
- [ ] 피드백 버튼 클릭 시 toggle API 호출 및 UI 반영
- [ ] 본인 게시글에만 수정/삭제 버튼 표시
- [ ] 추천글 생성 성공 후 메인 페이지로 이동
- [ ] 추천글 수정 페이지에서 기존 데이터 pre-fill
- [ ] 미인증 상태에서 페이지 새로고침 후 refresh API 성공 시 정상 진입
- [ ] Refresh Token 만료 상태에서만 `/login`으로 리다이렉트
- [ ] 모든 내부 페이지 이동이 `<Link>` 컴포넌트로 구현됨
- [ ] 모든 API 호출은 `lib/request.ts`에서 관리

---

## 7. 미정 사항 (generator 임의 결정 금지)

| 항목 | 내용 |
|---|---|
| 추천글 목록 정렬 기준 | API 명세에 정렬 기준 미명시. 댓글 조회는 `createdAt ASC`로 명시되어 있으나, 추천글 목록 기준은 없음. **generator는 추정하지 말고, 이 항목을 확인 후 구현** |
| 추천 댓글 수정 시 `rcId` 검증 | URL의 `rcId`와 댓글의 실제 `rc_id` 일치 여부를 서비스 레이어에서 검증할지 여부가 명세에 없음 |
| 피드백 optimistic update | 프론트엔드에서 피드백 클릭 시 API 응답 전 UI를 먼저 바꿀지 여부 (UX 방향) |
| 댓글 페이지네이션 프론트 기본값 | "더 보기" 클릭 시 전체 댓글을 한 번에 불러올지, `size=10`씩 불러올지 — 명세에 page/size 파라미터가 있으므로 분할 로드 가능하나 UX 방향이 불명확 |
| ADMIN 권한 차단 구현 방식 | Spring Security의 `@PreAuthorize("hasRole('USER')")` 어노테이션 방식 vs 서비스 레이어 내 역할 체크 방식 중 선택 필요 |

---

## 8. 명시적 제외 범위

이번 플랜에서 **포함하지 않는** 기능:
- 구매한 과자 도메인 (`bought_snack`, `bs_comment`, `bs_feedback`, `bsc_feedback`)
- 관리자 유저 관리 API (`/api/admin/*`)
- JUnit 테스트 코드 (별도 플랜으로 분리)
- Vitest 테스트 코드 (별도 플랜으로 분리)
- 대댓글 기능 (MVP 제외)
- 검색/정렬/필터 기능 (MVP 제외)
- 추천글 상세 페이지 전용 확장 응답 (현재 MVP에서는 `ListItemDTO`와 동일 구조)

---

## 9. generator 전달 메모

- 인증 필요 페이지(`/`, `/recommendations/new`, `/recommendations/[rcId]/edit`)는 반드시 `'use client'` 선언
- accessToken이 없을 때 즉시 redirect하지 말고 `postRefresh()`를 먼저 시도할 것 (zustand-auth-recovery.md 참조)
- 내부 페이지 링크는 `<a href>` 대신 Next.js `<Link>` 사용
- ADMIN 역할 차단은 서버 403 응답을 기준으로 처리 (프론트에서 역할 체크만으로 보안 대체 불가)
- `commentPreview`는 최대 3개 최신순 — 4개 이상 반환 금지
- 피드백 toggle에서 취소(삭제) 시 `myFeedback: null`로 응답해야 함
- 미정 사항 항목은 임의 구현하지 말고 구현 전 확인 요청

---

## 10. tester 전달 메모

- 추천글 삭제 후 해당 게시글의 `rc_comment`, `rc_feedback` 데이터 잔존 여부를 반드시 DB에서 직접 확인
- 피드백 toggle: 동일 상태 재요청 → null, 반대 상태 요청 → 변경 — 각 케이스 모두 검수
- ADMIN 계정으로 추천글 생성/댓글 작성/피드백 시도 시 403 응답 확인
- 로그인 → 메인 페이지 → 새로고침 → 정상 진입되는지 확인 (refresh 토큰 복구 정상 동작 여부)
- Refresh Token 만료 상태에서만 `/login`으로 이동하는지 확인
- `commentPreview`가 3개를 초과하지 않는지 확인 (4개 이상의 댓글이 있는 게시글에서 검수)
