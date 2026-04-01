# 권한 및 인증 상태 관리 버그 픽스 플랜 (Bug Fix Plan)

## 0. 대상 기능 및 발생 이슈
1. **관리자(ADMIN) 권한 거부:** admin 계정으로 추천 게시글 및 댓글 작성, 그리고 피드백(좋아요/싫어요) 시도 시 권한 없음(403) 에러 발생.
2. **새로고침 시 로그인 해제:** F5 등 페이지 새로고침 시 Zustand 런타임 메모리가 초기화되며 로그아웃 처리되는 현상.
3. **마이페이지 이동 시 400 Bad Request 발생:** 네비게이션을 통해 `/mypage` 접근 시 내부적으로 `GET /api/users/me` 호출 과정에서 400 Bad Request 에러가 발생하며 페이지 정상 진입 실패.

---

## 1. 구현 범위 (Scope)

### 1-1. 백엔드 (Spring Boot) 변경
- **대상 파일:** `RecommendationController`, `RcCommentController`, `RcFeedbackController`, `UserController` (또는 연관된 `GlobalExceptionHandler`, 인증 필터 등)
- **적용 사항:** 
  1. 추천 관련 API(추천글 생성, 댓글 생성, 피드백 토글)의 엔드포인트에서 ADMIN 권한의 로직 접근 제한을 해제 (`@PreAuthorize("hasRole('USER')")` ➡️ `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")` 등 변경).
  2. `GET /api/users/me` 호출 시 400 (Bad Request)가 발생하는 원인 분석 및 해결. (SecurityContext 파싱 문제나 어노테이션 바인딩 문제일 확률 높음 점검 필수)

### 1-2. 프론트엔드 (Next.js) 변경
- **대상 파일:** `app/page.tsx`, `app/mypage/page.tsx`, 클라이언트 네비게이션 메뉴 컴포넌트(Header/Navbar) 등 페이지 진입점
- **적용 사항:** 
  1. 인증이 필요한 페이지 렌더링 시 최우선적으로 `initAuth.ts` 내 `initAuthState`를 호출(await)하여 Zustand 인증 상태를 새로고침(또는 쿠키에서 토큰 복원)하는 로직 철저 보장.
  2. `/mypage` 등 내부 링크 이동 시 전체 페이지 리로드(Full Reload)를 유발하는 `<a href="...">` 태그를 찾아내 Next.js App Router의 `<Link href="...">` 컴포넌트로 전면 교체.

---

## 2. 세부 작업 정책 및 방침

### [작업 1] 관리자 권한 허용
- **목표:** ADMIN 역할도 USER와 동일하게 추천 도메인 내 작성 관련 및 피드백 기능 사용 허용.
- **근거:** API 문서(`api_spec.md`) 상 `USER (ADMIN 불가)`로 명시되어 있으나 요구사항에 따라 ADMIN 기능 추가. (작성 권한, 댓글 작성 권한, 피드백 권한 전부 허용으로 통일 확정)
- **주의:** 변경 후 테스트 시, 본인 게시글 수정/삭제가 아닐 때 발생하는 일반적인 403 상황과 생성 시의 권한을 헷갈리지 않게 유의하여 검증.

### [작업 2] 새로고침 시 로그인 해제 현상
- **목표:** 환경 새로고침(F5) 등으로 애플리케이션 리셋 시 쿠키의 Refresh Token을 사용해 자동으로 인증 정보 보강.
- **수정 정책:**
  - 미인증 상태에서 컴포넌트 `useEffect` 마운트 시 무조건 `/login`으로 튕겨내는 단순한 로직이 있다면 수정합니다.
  - 마운트 시점에 반드시 `await initAuthState(authStore)`를 대기하여 토큰 재발급 여부를 알아낸 뒤, 최종 불가 판정(`false`)일 때만 `/login`으로 리디렉트 처리되도록 보완합니다.

### [작업 3] 마이페이지 400 에러 및 권한 문제 해결
- **목표 분석 및 해결 정책:**
  1. 프론트엔드 상태 유지 보장: 네비게이션의 마이페이지 링크를 클릭했을 때 클라이언트 렌더링 라우팅(`<Link>`)이 적용되게 수정하여 불필요한 새로고침 및 토큰 유실을 방지합니다.
  2. `GET /api/users/me` 400 현상: 보통 파라미터가 없는 GET API이므로 400 에러는 `MethodArgumentTypeMismatch`, `@AuthenticationPrincipal`의 캐스팅 실패, 헤더 값이 잘못 파싱되는 경우 자주 발생합니다. 에러 발생 근원지를 찾아 권한이 있는 사용자에게 200 OK와 올바른 유저 데이터를 반환하도록 백엔드 로직 수정이 필요합니다.

---

## 3. 완료 조건 (tester 검수 기준)

- **백엔드:**
  - [ ] ADMIN 계정으로 추천 게시글 작성(`POST /api/recommendations`) 시 201 응답 수신
  - [ ] ADMIN 계정으로 추천 댓글 작성(`POST /api/recommendations/{rcId}/comments`) 시 201 응답 수신
  - [ ] ADMIN 계정으로 피드백(`POST .../feedback`) 토글 요청 시 200 정상 응답 및 동작
  - [ ] `GET /api/users/me` 호출 시 400 에러 없이 현재 인증된 회원의 정상 데이터를 응답받음
- **프론트엔드:**
  - [ ] 로그인 후 추천 메인화면에서 빈 공간에 F5(새로고침) 입력 시 로그아웃 화면으로 돌아가지 않고 즉시 상태가 복원됨 (헤더에 닉네임 표시 정상 동작)
  - [ ] 네비게이션 바 메뉴를 통해 마이페이지 이동 시 화면 깜빡임이 없고, API 에러 없이 내 정보 편집 화면이 온전하게 렌더링됨
