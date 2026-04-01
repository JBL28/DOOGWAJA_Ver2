# DOGWAJA API 명세서

## 목차

1. [공통 규칙](#공통-규칙)
2. [공통 DTO 정의](#공통-dto-정의)
3. [인증 / 인가](#1-인증--인가)
4. [유저](#2-유저)
5. [추천 게시글](#3-추천-게시글)
6. [추천 댓글](#4-추천-댓글)
7. [추천 게시글 피드백](#5-추천-게시글-피드백)
8. [추천 댓글 피드백](#6-추천-댓글-피드백)
9. [구매한 과자](#7-구매한-과자)
10. [구매 과자 댓글](#8-구매-과자-댓글)
11. [구매 과자 피드백](#9-구매-과자-피드백)
12. [구매 과자 댓글 피드백](#10-구매-과자-댓글-피드백)
13. [관리자](#11-관리자)
14. [에러 코드 정의](#에러-코드-정의)
15. [엔드포인트 요약표](#엔드포인트-요약표)

---

## 공통 규칙

### Base URL
```
http://localhost:8080/api
```

### 인증 방식

| 토큰 | 전달 방식 | 유효기간 |
|---|---|---|
| Access Token | `Authorization: Bearer {token}` (Request Header) | 30분 |
| Refresh Token | `HttpOnly Cookie` (`refreshToken`) | 7일 |

### 공통 응답 형식

**성공**
```json
{ "success": true, "data": { ... }, "message": "요청이 성공적으로 처리되었습니다." }
```

**실패**
```json
{ "success": false, "data": null, "message": "에러 메시지" }
```

### 페이지네이션 응답 형식

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 35,
    "totalPages": 4,
    "first": true,
    "last": false
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

**공통 쿼리 파라미터**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `page` | int | 0 | 페이지 번호 (0-indexed) |
| `size` | int | 10 | 페이지당 항목 수 |

---

## 공통 DTO 정의

> 목록용과 상세용 DTO를 명확히 분리한다.
> 목록 응답에는 `commentPreview`(최대 3개) + `commentCount`만 포함한다.
> 전체 댓글이 필요한 경우 별도 댓글 조회 API를 사용한다.

---

### AuthorDTO
```json
{
  "userId": 1,
  "nickname": "과자왕"
}
```

---

### FeedbackSummaryDTO
```json
{
  "likeCount": 5,
  "dislikeCount": 1,
  "myFeedback": "LIKE"   // "LIKE" | "DISLIKE" | null (비로그인 or 미반응)
}
```

---

### CommentPreviewItemDTO
> 목록 응답의 `commentPreview` 배열 원소. 피드백 정보 **포함** (메인 화면에서 바로 표시).

```json
{
  "id": 10,           // rccId 또는 bscId
  "content": "저도 먹고 싶어요!",
  "author": { "userId": 3, "nickname": "간식러버" },
  "feedbackSummary": {
    "likeCount": 2,
    "dislikeCount": 0,
    "myFeedback": null
  },
  "createdAt": "2024-01-02T11:00:00",
  "updatedAt": "2024-01-02T11:00:00"
}
```

### CommentItemDTO
> 별도 댓글 조회 API(`/comments`) 응답 원소. `CommentPreviewItemDTO`와 동일한 구조.

```json
{
  "id": 10,
  "content": "저도 먹고 싶어요!",
  "author": { "userId": 3, "nickname": "간식러버" },
  "feedbackSummary": {
    "likeCount": 2,
    "dislikeCount": 0,
    "myFeedback": null
  },
  "createdAt": "2024-01-02T11:00:00",
  "updatedAt": "2024-01-02T11:00:00"
}
```

---

### RecommendationListItemDTO
> `GET /api/recommendations` 목록 응답에 사용.

```json
{
  "rcId": 1,
  "name": "허니버터칩",
  "reason": "달콤하고 짠 맛의 조화가 일품입니다.",
  "author": { "userId": 2, "nickname": "과자왕" },
  "feedbackSummary": {
    "likeCount": 5,
    "dislikeCount": 1,
    "myFeedback": "LIKE"
  },
  "commentCount": 8,
  "commentPreview": [
    /* CommentPreviewItemDTO × 최대 3개 (최신순) */
  ],
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### RecommendationDetailDTO
> `GET /api/recommendations/{rcId}` 단건 응답에 사용.
> 댓글 전체는 `GET /api/recommendations/{rcId}/comments` 에서 별도 조회.

```json
{
  "rcId": 1,
  "name": "허니버터칩",
  "reason": "달콤하고 짠 맛의 조화가 일품입니다.",
  "author": { "userId": 2, "nickname": "과자왕" },
  "feedbackSummary": {
    "likeCount": 5,
    "dislikeCount": 1,
    "myFeedback": "LIKE"
  },
  "commentCount": 8,
  "commentPreview": [
    /* CommentPreviewItemDTO × 최대 3개 (최신순) */
  ],
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

> `ListItemDTO`와 필드 구조는 동일하나 의미상 분리. 추후 상세 전용 필드(예: 이미지, 태그 등) 추가 시 여기서 확장.

---

### BoughtSnackListItemDTO
> `GET /api/bought-snacks` 목록 응답에 사용.

```json
{
  "bsId": 1,
  "name": "새우깡",
  "status": "IN_STOCK",
  "statusLabel": "재고있음",
  "feedbackSummary": {
    "likeCount": 10,
    "dislikeCount": 2,
    "myFeedback": "LIKE"
  },
  "commentCount": 5,
  "commentPreview": [
    /* CommentPreviewItemDTO × 최대 3개 (최신순) */
  ],
  "createdAt": "2024-01-01T09:00:00",
  "updatedAt": "2024-01-01T09:00:00"
}
```

### BoughtSnackDetailDTO
> `GET /api/bought-snacks/{bsId}` 단건 응답에 사용.
> 댓글 전체는 `GET /api/bought-snacks/{bsId}/comments` 에서 별도 조회.

```json
{
  "bsId": 1,
  "name": "새우깡",
  "status": "IN_STOCK",
  "statusLabel": "재고있음",
  "feedbackSummary": {
    "likeCount": 10,
    "dislikeCount": 2,
    "myFeedback": "LIKE"
  },
  "commentCount": 5,
  "commentPreview": [
    /* CommentPreviewItemDTO × 최대 3개 (최신순) */
  ],
  "createdAt": "2024-01-01T09:00:00",
  "updatedAt": "2024-01-01T09:00:00"
}
```

**status 매핑**

| ENUM 값 | statusLabel |
|---|---|
| `SHIPPING` | 배송중 |
| `IN_STOCK` | 재고있음 |
| `OUT_OF_STOCK` | 재고없음 |

---

## 1. 인증 / 인가

### 1-1. 회원가입
```
POST /api/auth/register
```
> **PUBLIC**

**Request Body**
```json
{
  "loginId": "string",   // 5자 이상, 영문+숫자
  "password": "string",  // 영문+숫자 각 1자 이상 포함
  "nickname": "string"   // 1자 이상
}
```

**Response `201 Created`**
```json
{ "success": true, "data": null, "message": "회원가입이 완료되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 400 | `loginId는 5자 이상이어야 합니다.` |
| 400 | `비밀번호는 영문과 숫자를 포함해야 합니다.` |
| 409 | `이미 사용 중인 아이디입니다.` |

---

### 1-2. 로그인
```
POST /api/auth/login
```
> **PUBLIC**

**Request Body**
```json
{ "loginId": "string", "password": "string" }
```

**Response `200 OK`**
> Refresh Token은 `Set-Cookie` (`HttpOnly`, `SameSite=Strict`)로 전달.

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "userId": 1,
    "loginId": "john123",
    "nickname": "존",
    "role": "USER"
  },
  "message": "로그인되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 401 | `아이디 또는 비밀번호가 일치하지 않습니다.` |
| 403 | `비활성화된 계정입니다.` |
| 403 | `탈퇴한 계정입니다.` |

---

### 1-3. 로그아웃
```
POST /api/auth/logout
```
> **인증 필요**

서버 측 Refresh Token 무효화, 클라이언트 쿠키 삭제 (`Max-Age=0`).

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "로그아웃되었습니다." }
```

---

### 1-4. Access Token 재발급
```
POST /api/auth/refresh
```
> **PUBLIC** (쿠키 자동 전송)

**Response `200 OK`**
```json
{ "success": true, "data": { "accessToken": "eyJhbGci..." }, "message": "토큰이 재발급되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 401 | `Refresh Token이 만료되었습니다. 다시 로그인해주세요.` |
| 401 | `유효하지 않은 Refresh Token입니다.` |

---

## 2. 유저

### 2-1. 내 정보 조회
```
GET /api/users/me
```
> **인증 필요** | **권한: USER, ADMIN**

**Response `200 OK`**
```json
{
  "success": true,
  "data": {
    "userId": 1, "loginId": "john123", "nickname": "존",
    "role": "USER", "status": "ACTIVATED", "createdAt": "2024-01-01T10:00:00"
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

---

### 2-2. 내 정보 수정
```
PUT /api/users/me
```
> **인증 필요** | **권한: USER, ADMIN**

**Request Body** (변경할 필드만 포함)
```json
{
  "nickname": "새닉네임",
  "currentPassword": "pass1",
  "newPassword": "newPass2"
}
```

- `newPassword` 전달 시 `currentPassword` 필수.
- `newPassword`: 영문과 숫자 각 1자 이상 포함.

**Response `200 OK`**
```json
{ "success": true, "data": { "userId": 1, "nickname": "새닉네임" }, "message": "정보가 수정되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 400 | `현재 비밀번호가 일치하지 않습니다.` |
| 400 | `새 비밀번호는 영문과 숫자를 포함해야 합니다.` |

---

### 2-3. 회원 탈퇴
```
DELETE /api/users/me
```
> **인증 필요** | **권한: USER, ADMIN**

**Request Body**
```json
{ "password": "string" }
```

`status = DELETED` 처리 (소프트 삭제), Refresh Token 무효화.

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "회원 탈퇴가 완료되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 400 | `비밀번호가 일치하지 않습니다.` |

---

## 3. 추천 게시글

### 3-1. 추천글 목록 조회
```
GET /api/recommendations?page=0&size=10
```
> **인증 필요** | **권한: USER, ADMIN**

**Response `200 OK`** — `content` 원소: `RecommendationListItemDTO`

---

### 3-2. 추천글 단건 조회
```
GET /api/recommendations/{rcId}
```
> **인증 필요** | **권한: USER, ADMIN**

**Response `200 OK`** — `data`: `RecommendationDetailDTO`

| 상태 코드 | 메시지 |
|---|---|
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

### 3-3. 추천글 생성
```
POST /api/recommendations
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "name": "허니버터칩", "reason": "달콤하고 짠 맛의 조화가 일품입니다." }
```

**Response `201 Created`**
```json
{ "success": true, "data": { "rcId": 1 }, "message": "추천글이 등록되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |

---

### 3-4. 추천글 수정
```
PUT /api/recommendations/{rcId}
```
> **인증 필요** | **권한: USER (작성자 본인만)**

**Request Body**
```json
{ "name": "허니버터칩 (수정)", "reason": "수정된 추천 이유입니다." }
```

**Response `200 OK`**
```json
{ "success": true, "data": { "rcId": 1 }, "message": "추천글이 수정되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 게시글만 수정할 수 있습니다.` |
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

### 3-5. 추천글 삭제
```
DELETE /api/recommendations/{rcId}
```
> **인증 필요** | **권한: USER (작성자 본인만)**

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "추천글이 삭제되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 게시글만 삭제할 수 있습니다.` |
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

## 4. 추천 댓글

### 4-1. 추천 댓글 전체 조회 (페이지네이션)
```
GET /api/recommendations/{rcId}/comments?page=0&size=10
```
> **인증 필요** | **권한: USER, ADMIN**

- 메인 화면에서 댓글 펼치기 시 호출.
- 기본 정렬: 오래된 순 (`createdAt ASC`).

**Response `200 OK`** — `content` 원소: `CommentItemDTO`

```json
{
  "success": true,
  "data": {
    "content": [ /* CommentItemDTO 배열 */ ],
    "page": 0,
    "size": 10,
    "totalElements": 8,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

### 4-2. 추천 댓글 작성
```
POST /api/recommendations/{rcId}/comments
```
> **인증 필요** | **권한: USER** (ADMIN 불가 — 추천글은 일반 사용자 간의 교류)

**Request Body**
```json
{ "content": "저도 먹고 싶어요!" }
```

**Response `201 Created`**
```json
{ "success": true, "data": { "rccId": 10 }, "message": "댓글이 작성되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

### 4-3. 추천 댓글 수정
```
PUT /api/recommendations/{rcId}/comments/{rccId}
```
> **인증 필요** | **권한: USER (작성자 본인만)**

**Request Body**
```json
{ "content": "수정된 댓글입니다." }
```

**Response `200 OK`**
```json
{ "success": true, "data": { "rccId": 10 }, "message": "댓글이 수정되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 댓글만 수정할 수 있습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

### 4-4. 추천 댓글 삭제
```
DELETE /api/recommendations/{rcId}/comments/{rccId}
```
> **인증 필요** | **권한: USER (작성자 본인만)**

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "댓글이 삭제되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 댓글만 삭제할 수 있습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

## 5. 추천 게시글 피드백

> **Toggle 방식**: 동일 상태 재요청 → 취소, 다른 상태 요청 → 변경.

| 기존 상태 | 요청 | 결과 |
|---|---|---|
| 없음 | LIKE | LIKE 생성 |
| LIKE | LIKE | 삭제 (toggle off) |
| LIKE | DISLIKE | DISLIKE로 변경 |
| DISLIKE | DISLIKE | 삭제 (toggle off) |
| DISLIKE | LIKE | LIKE로 변경 |

### 5-1. 추천글 피드백 생성 / 변경 / 취소
```
POST /api/recommendations/{rcId}/feedback
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "status": "LIKE" }   // "LIKE" | "DISLIKE"
```

**Response `200 OK`**
```json
{
  "success": true,
  "data": { "myFeedback": "LIKE", "likeCount": 6, "dislikeCount": 1 },
  "message": "피드백이 반영되었습니다."
}
```

취소 시 `"myFeedback": null`.

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `추천 게시글을 찾을 수 없습니다.` |

---

## 6. 추천 댓글 피드백

> Toggle 방식 동일.

### 6-1. 추천 댓글 피드백 생성 / 변경 / 취소
```
POST /api/recommendations/{rcId}/comments/{rccId}/feedback
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "status": "DISLIKE" }   // "LIKE" | "DISLIKE"
```

**Response `200 OK`**
```json
{
  "success": true,
  "data": { "myFeedback": "DISLIKE", "likeCount": 2, "dislikeCount": 1 },
  "message": "피드백이 반영되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

## 7. 구매한 과자

### 7-1. 구매 과자 목록 조회
```
GET /api/bought-snacks?page=0&size=10
```
> **인증 필요** | **권한: USER, ADMIN**

**Response `200 OK`** — `content` 원소: `BoughtSnackListItemDTO`

---

### 7-2. 구매 과자 단건 조회
```
GET /api/bought-snacks/{bsId}
```
> **인증 필요** | **권한: USER, ADMIN**

**Response `200 OK`** — `data`: `BoughtSnackDetailDTO`

| 상태 코드 | 메시지 |
|---|---|
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

### 7-3. 구매 과자 등록
```
POST /api/bought-snacks
```
> **인증 필요** | **권한: ADMIN만**

**Request Body**
```json
{ "name": "새우깡" }
```

등록 시 `status` 기본값: `SHIPPING`.

**Response `201 Created`**
```json
{ "success": true, "data": { "bsId": 1 }, "message": "과자가 등록되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `관리자만 접근할 수 있습니다.` |

---

### 7-4. 구매 과자 수정
```
PUT /api/bought-snacks/{bsId}
```
> **인증 필요** | **권한: ADMIN만**

**Request Body**
```json
{ "name": "새우깡 (대용량)" }
```

**Response `200 OK`**
```json
{ "success": true, "data": { "bsId": 1 }, "message": "과자 정보가 수정되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `관리자만 접근할 수 있습니다.` |
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

### 7-5. 구매 과자 삭제
```
DELETE /api/bought-snacks/{bsId}
```
> **인증 필요** | **권한: ADMIN만**

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "과자가 삭제되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `관리자만 접근할 수 있습니다.` |
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

### 7-6. 구매 과자 상태 변경
```
PATCH /api/bought-snacks/{bsId}/status
```
> **인증 필요** | **권한: USER, ADMIN 모두 가능** (요구사항: "과자의 상태는 모두가 변경할 수 있다")

**Request Body**
```json
{ "status": "IN_STOCK" }   // "SHIPPING" | "IN_STOCK" | "OUT_OF_STOCK"
```

**Response `200 OK`**
```json
{
  "success": true,
  "data": { "bsId": 1, "status": "IN_STOCK", "statusLabel": "재고있음" },
  "message": "상태가 변경되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 400 | `유효하지 않은 상태 값입니다.` |
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

## 8. 구매 과자 댓글

> "관리자가 아닌 사용자가 댓글을 달 수 있음" — **ADMIN은 댓글 작성/수정/삭제 불가**.
> 단, 조회는 ADMIN도 가능.

### 8-1. 구매 과자 댓글 전체 조회 (페이지네이션)
```
GET /api/bought-snacks/{bsId}/comments?page=0&size=10
```
> **인증 필요** | **권한: USER, ADMIN**

기본 정렬: 오래된 순 (`createdAt ASC`).

**Response `200 OK`** — `content` 원소: `CommentItemDTO`

```json
{
  "success": true,
  "data": {
    "content": [ /* CommentItemDTO 배열 */ ],
    "page": 0, "size": 10, "totalElements": 5,
    "totalPages": 1, "first": true, "last": true
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

### 8-2. 구매 과자 댓글 작성
```
POST /api/bought-snacks/{bsId}/comments
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "content": "빨리 먹고 싶어요!" }
```

**Response `201 Created`**
```json
{ "success": true, "data": { "bscId": 5 }, "message": "댓글이 작성되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

### 8-3. 구매 과자 댓글 수정
```
PUT /api/bought-snacks/{bsId}/comments/{bscId}
```
> **인증 필요** | **권한: USER (작성자 본인만)** (ADMIN 불가)

**Request Body**
```json
{ "content": "수정된 댓글입니다." }
```

**Response `200 OK`**
```json
{ "success": true, "data": { "bscId": 5 }, "message": "댓글이 수정되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 댓글만 수정할 수 있습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

### 8-4. 구매 과자 댓글 삭제
```
DELETE /api/bought-snacks/{bsId}/comments/{bscId}
```
> **인증 필요** | **권한: USER (작성자 본인만)** (ADMIN 불가)

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "댓글이 삭제되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `본인의 댓글만 삭제할 수 있습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

## 9. 구매 과자 피드백

> "관리자가 아닌 사용자가 피드백을 달 수 있다" — **ADMIN은 피드백 불가**.
> Toggle 방식 동일.

### 9-1. 구매 과자 피드백 생성 / 변경 / 취소
```
POST /api/bought-snacks/{bsId}/feedback
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "status": "LIKE" }   // "LIKE" | "DISLIKE"
```

**Response `200 OK`**
```json
{
  "success": true,
  "data": { "myFeedback": "LIKE", "likeCount": 11, "dislikeCount": 2 },
  "message": "피드백이 반영되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `구매 과자를 찾을 수 없습니다.` |

---

## 10. 구매 과자 댓글 피드백

> "관리자가 아닌 사용자가 피드백을 달 수 있다" — **ADMIN은 피드백 불가**.
> Toggle 방식 동일.

### 10-1. 구매 과자 댓글 피드백 생성 / 변경 / 취소
```
POST /api/bought-snacks/{bsId}/comments/{bscId}/feedback
```
> **인증 필요** | **권한: USER** (ADMIN 불가)

**Request Body**
```json
{ "status": "LIKE" }   // "LIKE" | "DISLIKE"
```

**Response `200 OK`**
```json
{
  "success": true,
  "data": { "myFeedback": "LIKE", "likeCount": 3, "dislikeCount": 0 },
  "message": "피드백이 반영되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `접근 권한이 없습니다.` |
| 404 | `댓글을 찾을 수 없습니다.` |

---

## 11. 관리자

### 11-1. 전체 유저 목록 조회
```
GET /api/admin/users?page=0&size=10
```
> **인증 필요** | **권한: ADMIN만**

**Response `200 OK`**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": 1, "loginId": "john123", "nickname": "존",
        "role": "USER", "status": "ACTIVATED", "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "page": 0, "size": 10, "totalElements": 50, "totalPages": 5,
    "first": true, "last": false
  },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `관리자만 접근할 수 있습니다.` |

---

### 11-2. 유저 상태 변경
```
PATCH /api/admin/users/{userId}/status
```
> **인증 필요** | **권한: ADMIN만**

**Request Body**
```json
{ "status": "DEACTIVATED" }   // "ACTIVATED" | "DEACTIVATED"
```

> `DELETED`는 이 엔드포인트에서 직접 설정 불가. 탈퇴는 본인만 가능.

**Response `200 OK`**
```json
{ "success": true, "data": { "userId": 1, "status": "DEACTIVATED" }, "message": "유저 상태가 변경되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 400 | `유효하지 않은 상태 값입니다.` |
| 403 | `관리자만 접근할 수 있습니다.` |
| 404 | `유저를 찾을 수 없습니다.` |

---

### 11-3. 유저 삭제
```
DELETE /api/admin/users/{userId}
```
> **인증 필요** | **권한: ADMIN만**

`status = DELETED` 처리 (소프트 삭제).

**Response `200 OK`**
```json
{ "success": true, "data": null, "message": "유저가 삭제되었습니다." }
```

| 상태 코드 | 메시지 |
|---|---|
| 403 | `관리자만 접근할 수 있습니다.` |
| 404 | `유저를 찾을 수 없습니다.` |

---

## 에러 코드 정의

| HTTP 코드 | 의미 | 사용 예 |
|---|---|---|
| 200 | OK | 조회, 수정, 삭제, 피드백 성공 |
| 201 | Created | 생성 성공 |
| 400 | Bad Request | 유효성 검증 실패, 잘못된 요청 |
| 401 | Unauthorized | 토큰 없음·만료·유효하지 않음 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 중복 데이터 (loginId 중복 등) |
| 500 | Internal Server Error | 서버 내부 오류 |

---

## 엔드포인트 요약표

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/register` | PUBLIC | 회원가입 |
| POST | `/api/auth/login` | PUBLIC | 로그인 |
| POST | `/api/auth/logout` | USER, ADMIN | 로그아웃 |
| POST | `/api/auth/refresh` | PUBLIC (쿠키) | Access Token 재발급 |
| GET | `/api/users/me` | USER, ADMIN | 내 정보 조회 |
| PUT | `/api/users/me` | USER, ADMIN | 내 정보 수정 |
| DELETE | `/api/users/me` | USER, ADMIN | 회원 탈퇴 |
| GET | `/api/recommendations` | USER, ADMIN | 추천글 목록 (ListItemDTO) |
| POST | `/api/recommendations` | **USER only** | 추천글 생성 |
| GET | `/api/recommendations/{rcId}` | USER, ADMIN | 추천글 단건 (DetailDTO) |
| PUT | `/api/recommendations/{rcId}` | **USER (본인)** | 추천글 수정 |
| DELETE | `/api/recommendations/{rcId}` | **USER (본인)** | 추천글 삭제 |
| GET | `/api/recommendations/{rcId}/comments` | USER, ADMIN | 추천 댓글 전체 조회 (페이지네이션) |
| POST | `/api/recommendations/{rcId}/comments` | **USER only** | 추천 댓글 작성 |
| PUT | `/api/recommendations/{rcId}/comments/{rccId}` | **USER (본인)** | 추천 댓글 수정 |
| DELETE | `/api/recommendations/{rcId}/comments/{rccId}` | **USER (본인)** | 추천 댓글 삭제 |
| POST | `/api/recommendations/{rcId}/feedback` | **USER only** | 추천글 피드백 toggle |
| POST | `/api/recommendations/{rcId}/comments/{rccId}/feedback` | **USER only** | 추천 댓글 피드백 toggle |
| GET | `/api/bought-snacks` | USER, ADMIN | 구매 과자 목록 (ListItemDTO) |
| POST | `/api/bought-snacks` | **ADMIN only** | 구매 과자 등록 |
| GET | `/api/bought-snacks/{bsId}` | USER, ADMIN | 구매 과자 단건 (DetailDTO) |
| PUT | `/api/bought-snacks/{bsId}` | **ADMIN only** | 구매 과자 수정 |
| DELETE | `/api/bought-snacks/{bsId}` | **ADMIN only** | 구매 과자 삭제 |
| PATCH | `/api/bought-snacks/{bsId}/status` | USER, ADMIN | 구매 과자 상태 변경 |
| GET | `/api/bought-snacks/{bsId}/comments` | USER, ADMIN | 구매 과자 댓글 전체 조회 (페이지네이션) |
| POST | `/api/bought-snacks/{bsId}/comments` | **USER only** | 구매 과자 댓글 작성 |
| PUT | `/api/bought-snacks/{bsId}/comments/{bscId}` | **USER (본인)** | 구매 과자 댓글 수정 |
| DELETE | `/api/bought-snacks/{bsId}/comments/{bscId}` | **USER (본인)** | 구매 과자 댓글 삭제 |
| POST | `/api/bought-snacks/{bsId}/feedback` | **USER only** | 구매 과자 피드백 toggle |
| POST | `/api/bought-snacks/{bsId}/comments/{bscId}/feedback` | **USER only** | 구매 과자 댓글 피드백 toggle |
| GET | `/api/admin/users` | **ADMIN only** | 전체 유저 목록 |
| PATCH | `/api/admin/users/{userId}/status` | **ADMIN only** | 유저 상태 변경 |
| DELETE | `/api/admin/users/{userId}` | **ADMIN only** | 유저 삭제 |
