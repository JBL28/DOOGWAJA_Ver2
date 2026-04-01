# 구매 과자 도메인 기능 구현 계획

본 계획은 `.agent/planner/README.md` 지침에 따라 `구매 과자` 도메인의 CRUD, 상태 변경, 댓글, 피드백 기능 개발을 구현 가능한 단위로 분리하고 책임을 명확히 하기 위해 작성되었습니다. 정상 동작 중인 `추천 게시글` 파트의 코드 스니펫 및 로직 구조를 기준으로 개발 일관성을 유지합니다.

---

## 1. 구매 과자 게시글(Bought Snack) 기초 CRUD 백엔드 API 구현

### 목표
관리자가 공용 간식을 등록/수정/삭제하고 모든 사용자가 이를 조회할 수 있는 백엔드 API를 구축합니다.

### 근거 문서
- `../shared/requirements.md` (관리자 권한, 구매 과자 정보)
- `../shared/api_docs/api_spec.md` (7-1 ~ 7-5 구매 과자 항목)
- `../shared/erd.md` (bought_snack 테이블 명세)

### 선행 조건
- `user` 엔티티 및 기본 인증/인가 보안 환경(Spring Security) 구축 완료 상태여야 함

### 작업 범위
- `dev/ssafy/bought_snack/entity/BoughtSnack.java` 생성
- `BoughtSnackRepository`, `BoughtSnackService`, `BoughtSnackController` 생성
- DTO(`BoughtSnackListItemDTO`, `BoughtSnackDetailDTO` 등) 작성
- 조회(목록/상세 단건) 및 등록/수정/삭제 API 작성

### 제외 범위
- 상태 변경(PATCH) 및 댓글/피드백 기능은 이 작업에서 제외

### 세부 정책
- **권한**
  - 등록(POST)/수정(PUT)/삭제(DELETE): `ADMIN`만 접근 가능. 타 권한 접속시 403 반환.
  - 조회(GET 목록/상세): `USER`, `ADMIN` 모두 접근 가능.
- **입력값/유효성 검증**
  - 등록 시 `name` 필수 (비어있을 시 400 에러)
  - 처음 등록 시 상태는 `SHIPPING`(배송중)으로 디폴트 저장.
- **출력/응답**
  - 목록 조회 시 페이지네이션 객체를 반환하며 `commentPreview`(최대 3개), `commentCount`, `feedbackSummary`를 포함해야 함. (연관관계 없는 초기엔 빈 배열/0 처리)
- **실패 케이스**
  - 존재하지 않는 `bsId` 조회/수정/삭제 시 404 NOT_FOUND.

### 완료 조건
- [ ] ADMIN 계정의 Security Context를 통해 POST/PUT/DELETE 정상 처리 및 DB 반영됨을 검증한다.
- [ ] USER 권한 요청 시 등록/수정/삭제에 대해 403을 반환한다.
- [ ] GET 요청 시 페이지네이션을 포함하여 등록된 과자 목록을 응답 규격대로 반환한다.

### 미정 사항
- 없음 (API Docs에 정확히 정의되어 있음)

### generator 전달 메모
- 상태 매핑 정보(`SHIPPING` -> "배송중")는 프론트엔드보다는 백엔드 DTO 단계에서 `statusLabel` 필드로 변환해 내려주는 방식(API spec 참고)을 준수할 것.

---

## 2. 구매 과자 상태 변경(Status) 백엔드 API 구현

### 목표
구매 과자의 재고 및 배송 상태(`SHIPPING`, `IN_STOCK`, `OUT_OF_STOCK`)를 사용자들이 능동적으로 변경할 수 있도록 합니다.

### 근거 문서
- `../shared/requirements.md` (과자의 상태는 모두가 변경할 수 있다.)
- `../shared/api_docs/api_spec.md` (7-6. 구매 과자 상태 변경)

### 작업 범위
- `BoughtSnackController`에 PATCH `/api/bought-snacks/{bsId}/status` 추가
- 서비스 비즈니스 로직에 갱신 로직 구현

### 세부 정책
- **권한**: `USER`, `ADMIN` 모두 접근 가능.
- **입력값**: `{ "status": "IN_STOCK" | "OUT_OF_STOCK" | "SHIPPING" }`
- **유효성 검증**: 정의된 ENUM 외의 문자열 요청 시 400 반환.

### 완료 조건
- [ ] USER 권한으로 PATCH `/api/bought-snacks/{bsId}/status` 요청 시 권한 에러 없이 상태가 올바르게 업데이트된다.

---

## 3. 구매 과자 댓글(Comment) CRUD 백엔드 API 구현

### 목표
사용자가 등록된 구매 과자에 댓글을 남기고, 본인의 댓글을 수정/삭제할 수 있는 기능을 제공합니다.

### 근거 문서
- `../shared/api_docs/api_spec.md` (8. 구매 과자 댓글)
- `../shared/erd.md` (bs_comment 테이블)

### 작업 범위
- `bs_comment` 도메인 폴더 및 Entity, Repository, Service, Controller 구성
- 목록 조회, 등록, 수정, 삭제 API 구현

### 세부 정책
- **권한**
  - 생성: `USER` 권한만 허용 (`ADMIN`은 평가/댓글 작성이 제한됨, 시도 시 403)
  - 수정/삭제: 작성자 본인만 가능 (게시글의 권한과 무관하게 본인 여부 확인, 타인 시도 시 403)
  - 조회: `USER`, `ADMIN`
- **입력값**: `{ "content": "문자열" }` / 비어있으면 400.
- **출력/응답**: 페이지네이션 처리 (GET 호출 시). 각 댓글 항목(`CommentItemDTO`)에는 해당 댓글의 피드백 집계가 포함되어야 함.

### 완료 조건
- [ ] 본인이 쓴 댓글을 타인이 수정 및 삭제하려 할 경우 403 Forbidden 응답이 떨어진다.
- [ ] 관리자(`ADMIN`)가 댓글 작성을 시도할 시 403 상태 코드를 반환한다.

---

## 4. 구매 과자 관련 피드백(Feedback) 백엔드 API 구현

### 목표
게시물 본문과 댓글에 대해 좋아요/싫어요를 토글 방식으로 남길 수 있는 기능을 구현합니다.

### 근거 문서
- `../shared/api_docs/api_spec.md` (9, 10 구매 과자 / 댓글 피드백)
- `../shared/erd.md` (bs_feedback, bsc_feedback 테이블 `UNIQUE(user_id, target_id)`)

### 작업 범위
- `bs_feedback`, `bsc_feedback` 도메인 분리 및 Entity/Repository/Service/Controller 구현
- 게시글 피드백 POST API 및 댓글 피드백 POST API 추가
- `Toggle` 로직 설계 (기존과 동일하면 삭제, 다르면 수정, 없으면 추가)

### 세부 정책
- **권한**: `USER`만 허용 (`ADMIN` 시도 시 403 방어)
- **출력/응답**: 변경된 `likeCount`, `dislikeCount`, `myFeedback` 최신 상태값(`FeedbackSummaryDTO`) 즉시 반환.

### 완료 조건
- [ ] 연속해서 같은 피드백(`LIKE` -> `LIKE`)을 전송할 경우 DB에서 Feedback 데이터가 정상적으로 삭제(Toggle-off)된다.

---

## 5. 프론트엔드 API 통신 및 타입 명세 업데이트

### 목표
구매 과자 도메인을 위한 프론트엔드 통신 함수(`request.ts`) 및 타입을 작성합니다.

### 근거 문서
- 추천 게시판의 기존 `recommendation.ts` 구조 및 `request.ts` 활용 방식

### 작업 범위
- `frontend/types/bought-snack.ts` 추가
- `frontend/lib/request.ts` 내부 Axios 호출 함수 정의

### 완료 조건
- [ ] 프론트엔드 타입 검사 도구가 명세를 정상적으로 인식 및 컴파일 통과.
- [ ] API 호출 함수들이 `accessToken` 등을 올바르게 포함하도록 구현된다.

---

## 6. 메인 페이지 - 구매 과자 프론트엔드 리스트 구현

### 목표
기존 `page.tsx`(메인 페이지)에서 추천 글과 함께 등록된 과자 내역을 출력합니다.

### 근거 문서
- `../shared/requirements.md` (한 화면에 보여준다, 10개씩 페이지네이션 적용)

### 작업 범위
- `frontend/app/page.tsx` 내 구매 과자 리스트 조회 및 렌더링 영역 추가
- 리스트 상에서 직접 상태(배송중/재고있음 등) 변경 가능한 UI 구현
- 기존의 댓글 및 피드백 컴포넌트(`CommentSection` 등)를 재활용하여 부착.

### 세부 정책
- **권한**: 목록 조회 및 댓글 작성/피드백 버튼은 기존 추천 게시물 컴포넌트의 정책과 동일하게 적용. 단, 구매 과자의 상태(Status) 변경은 **권한 등급과 관계 없이 모든 사용자가 조작하여 즉각 반영**될 수 있어야 함.

### 완료 조건
- [ ] 화면 상에서 과자의 현재 상태(`status`) 변경 요청 시 서버에 업데이트되고 UI에 즉각 반영되며 상태 레이블이 갱신된다.
- [ ] 댓글 확장/접기 기능이 정상 사용 가능하다.

---

## 7. 관리자 전용 "구매 과자" 등록/수정 페이지 구현

### 목표
관리자가 공용 간식을 등록/수정하는 별도 폼 페이지를 연동하며, Zustand 복구 지침을 엄수합니다.

### 근거 문서
- `../shared/requirements.md` (관리자 계정으로 로그인 시 구매한 과자를 관리)
- `../planner/guidelines/zustand-auth-recovery.md` (인증 상태 복구 지침)

### 작업 범위
- `frontend/app/bought-snacks/new/page.tsx` 추가
- `frontend/app/bought-snacks/[bsId]/edit/page.tsx` 추가

### 세부 정책 (인증 상태 복구 정책)
- **권한 확인 방식**
  - 컴포넌트 최상단 빈칸 없이 `'use client'` 선언 필수.
  - 진입 시 Zustand의 `accessToken`이 존재하지 않아도 즉시 `/login`으로 보내지 않는다.
  - `/api/auth/refresh` API(혹은 이에 상응하는 로직)를 호출하여 HttpOnly 쿠키로 복구를 먼저 시도한다.
  - refresh 성공 시 새 accessToken을 Zustand에 저장하고 정상 진행한다.
  - refresh 실패 시 `/login`으로 리다이렉트한다.
- 일반 `USER`가 진입했을 경우 경고 모달/메시지를 띄우고 메인으로 돌려보낸다.
- **페이지 간 이동은 절대 `<a href>`를 통한 브라우저 풀 리로드를 발생시키지 않으며, 반드시 Next.js `<Link href="...">`를 사용한다.**

### 완료 조건
- [ ] 로그인 후 새로고침해도 인증이 필요한 페이지에 정상 진입된다 (쿠키가 유효할 경우).
- [ ] Refresh Token이 만료된 경우에만 `/login`으로 리다이렉트된다.
- [ ] 내부 컴포넌트 이동 링크에는 모두 Next.js `<Link>` 컴포넌트가 적용되어 있다.

### tester 전달 메모
- 새 과자 등록 및 수정 페이지에서 직접 `F5`를 눌렀을 때 튕겨나가는 버그가 재현되는지 확인해주세요. 권한이 일반 USER인 상태로 해당 URL로 강제 접근할 때 제대로 차단되는지 점검 바랍니다.
