# 인증 / 유저 기능 구현 플랜

> **플래너 작성일:** 2026-04-01  
> **대상 기능:** 인증/인가 (Auth) + 유저 (User)  
> **근거 문서:**
> - `.agent/shared/requirements.md`
> - `.agent/shared/api_docs/api_spec.md`
> - `.agent/generator/README.md`
> - `.agent/tester/README.md`

---

## 1. 구현 범위 (Scope)

이번 플랜은 아래 API 명세 범위만 다룬다.  
다른 도메인(추천, 구매 과자 등)은 이번 플랜에 포함하지 않는다.

### 1-1. 백엔드 (Spring Boot)

| 번호 | 메서드 | URL | 권한 | 설명 |
|---|---|---|---|---|
| A-1 | POST | `/api/auth/register` | PUBLIC | 회원가입 |
| A-2 | POST | `/api/auth/login` | PUBLIC | 로그인 |
| A-3 | POST | `/api/auth/logout` | USER, ADMIN | 로그아웃 |
| A-4 | POST | `/api/auth/refresh` | PUBLIC (쿠키) | Access Token 재발급 |
| U-1 | GET | `/api/users/me` | USER, ADMIN | 내 정보 조회 |
| U-2 | PUT | `/api/users/me` | USER, ADMIN | 내 정보 수정 |
| U-3 | DELETE | `/api/users/me` | USER, ADMIN | 회원 탈퇴 |

### 1-2. 프론트엔드 (Next.js)

| 번호 | 경로 | 설명 |
|---|---|---|
| F-1 | `/login` | 로그인 페이지 |
| F-2 | `/register` | 회원가입 페이지 |
| F-3 | `/mypage` | 내 정보 수정 / 회원 탈퇴 페이지 |

---

## 2. 전제 조건 및 의존성

- **DB:** `user` 테이블이 ERD 명세 그대로 존재해야 한다.
- **의존성 추가 (백엔드 `build.gradle`):**
  - `spring-boot-starter-data-jpa` — Entity 기반 ORM
  - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` — JWT 처리
  - `spring-boot-validation` — 입력 유효성 검증
- **의존성 추가 (프론트엔드 `package.json`):**
  - `axios` — HTTP 통신
  - `zustand` — 전역 상태 관리
- **환경 변수 (`application.yml`):**
  - `jwt.secret`, `jwt.access-expiration`, `jwt.refresh-expiration`
  - DB 접속 정보

---

## 3. 백엔드 구현 세부 계획

### 3-1. 패키지 구조

`dev.ssafy` 하위에 도메인별로 패키지를 생성한다.

```
dev.ssafy
├── auth
│   ├── controller   AuthController.java
│   ├── service      AuthService.java
│   ├── dto          RegisterRequest, LoginRequest, LoginResponse, TokenResponse
│   └── util         JwtProvider.java, CookieUtil.java
├── user
│   ├── controller   UserController.java
│   ├── service      UserService.java
│   ├── repository   UserRepository.java
│   ├── entity       User.java
│   └── dto          UserMeResponse, UpdateUserRequest, UpdateUserResponse, DeleteUserRequest
├── security
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── CustomUserDetails.java
└── common
    ├── ApiResponse.java      (공통 응답 래퍼)
    └── exception
        ├── GlobalExceptionHandler.java
        └── (Custom Exception 클래스들)
```

### 3-2. Entity

#### User.java
- 필드: `userId`, `loginId`, `passwordH`, `nickname`, `role`(ENUM), `status`(ENUM), `createdAt`, `updatedAt`
- `@PrePersist` / `@PreUpdate`로 `createdAt`, `updatedAt` 자동 처리
- `role`: ADMIN / USER
- `status`: ACTIVATED / DEACTIVATED / DELETED

### 3-3. 인증 흐름

#### Access Token / Refresh Token 정책
| 항목 | 값 |
|---|---|
| Access Token 유효기간 | 30분 |
| Refresh Token 유효기간 | 7일 |
| Access Token 전달 방식 | `Authorization: Bearer {token}` (Response Body 포함) |
| Refresh Token 전달 방식 | `Set-Cookie` (HttpOnly, SameSite=Strict) |

#### Refresh Token 저장 위치
- **DB 저장** (`user` 테이블에 `refresh_token VARCHAR` 컬럼 추가, nullable)
- 로그아웃 시 DB의 `refresh_token`을 null로 업데이트
- 재발급 시 DB의 값과 쿠키 값을 비교하여 유효성 검증

> **주의:** `user` ERD에 `refresh_token` 컬럼이 명시되어 있지 않다.  
> DB 저장 외 대안(Redis, In-Memory)은 현재 인프라에 Redis가 없으므로, DB 컬럼 추가 방식으로 진행한다.  
> 이 결정은 tester가 검수할 수 있도록 명시적으로 드러낸다.

### 3-4. 엔드포인트별 상세

#### A-1. POST `/api/auth/register` — 회원가입
- **권한:** PUBLIC
- **요청 유효성 검증:**
  - `loginId`: 5자 이상, 영문 + 숫자 (공백 불가)
  - `password`: 영문과 숫자 각 1자 이상 포함
  - `nickname`: 1자 이상
- **처리:**
  1. `loginId` 중복 확인 → 중복 시 `409 Conflict`
  2. `BCryptPasswordEncoder`로 비밀번호 해시
  3. User 저장 (`status=ACTIVATED`, `role=USER`)
- **성공 응답:** `201 Created`, `data: null`, `"회원가입이 완료되었습니다."`
- **실패 응답:**
  - `400` — `loginId는 5자 이상이어야 합니다.`
  - `400` — `비밀번호는 영문과 숫자를 포함해야 합니다.`
  - `409` — `이미 사용 중인 아이디입니다.`

#### A-2. POST `/api/auth/login` — 로그인
- **권한:** PUBLIC
- **처리:**
  1. `loginId`로 유저 조회 → 없으면 `401`
  2. `status=DEACTIVATED` → `403 "비활성화된 계정입니다."`
  3. `status=DELETED` → `403 "탈퇴한 계정입니다."`
  4. 비밀번호 불일치 → `401 "아이디 또는 비밀번호가 일치하지 않습니다."`
  5. Access Token, Refresh Token 발급
  6. Refresh Token을 DB에 저장, `Set-Cookie`로 전달
- **성공 응답:** `200`, `data: { accessToken, userId, loginId, nickname, role }`

#### A-3. POST `/api/auth/logout` — 로그아웃
- **권한:** USER, ADMIN (인증 필요)
- **처리:**
  1. DB의 `refresh_token`을 null로 업데이트
  2. `Set-Cookie`로 `refreshToken`의 `Max-Age=0` 설정
- **성공 응답:** `200`, `data: null`, `"로그아웃되었습니다."`

#### A-4. POST `/api/auth/refresh` — Access Token 재발급
- **권한:** PUBLIC (쿠키 자동 전송)
- **처리:**
  1. 쿠키 `refreshToken` 추출
  2. JWT 서명 및 만료 검증
  3. DB의 값과 일치 여부 확인
  4. 새 Access Token 발급
- **실패 응답:**
  - `401` — `Refresh Token이 만료되었습니다. 다시 로그인해주세요.`
  - `401` — `유효하지 않은 Refresh Token입니다.`

#### U-1. GET `/api/users/me` — 내 정보 조회
- **권한:** USER, ADMIN
- **처리:** SecurityContext에서 인증된 유저 ID로 조회
- **성공 응답:** `200`, `data: { userId, loginId, nickname, role, status, createdAt }`

#### U-2. PUT `/api/users/me` — 내 정보 수정
- **권한:** USER, ADMIN
- **처리:**
  1. `nickname`만 있으면 닉네임 변경
  2. `newPassword` 있으면 `currentPassword` 필수 → 현재 비밀번호 검증 → 새 비밀번호 유효성 검증 → 해시 후 저장
- **실패 응답:**
  - `400` — `현재 비밀번호가 일치하지 않습니다.`
  - `400` — `새 비밀번호는 영문과 숫자를 포함해야 합니다.`
- **성공 응답:** `200`, `data: { userId, nickname }`

#### U-3. DELETE `/api/users/me` — 회원 탈퇴
- **권한:** USER, ADMIN
- **처리:**
  1. 비밀번호 검증
  2. `status = DELETED` 처리 (소프트 삭제)
  3. DB의 `refresh_token` null로 업데이트
- **실패 응답:**
  - `400` — `비밀번호가 일치하지 않습니다.`
- **성공 응답:** `200`, `data: null`, `"회원 탈퇴가 완료되었습니다."`

### 3-5. Spring Security 설정

- `SecurityConfig`에서 permitAll 경로:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
- 나머지는 인증 필요
- `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 추가
- CORS: `http://localhost:3000` 허용, `allowCredentials = true`
- CSRF: 비활성화 (JWT 방식 사용)
- 세션: `STATELESS`

### 3-6. 공통 응답 래퍼

```java
// ApiResponse<T>
{
  boolean success;
  T data;
  String message;
}
```

- 성공: `success=true`
- 실패: `success=false`, `data=null`

### 3-7. 예외 처리

`GlobalExceptionHandler`에서 아래 예외를 처리한다:
- `MethodArgumentNotValidException` → `400`
- `DuplicateLoginIdException` → `409`
- `InvalidCredentialsException` → `401`
- `AccountStatusException` (비활성/탈퇴) → `403`
- `InvalidTokenException` → `401`
- `PasswordMismatchException` → `400`
- `EntityNotFoundException` → `404`

---

## 4. 프론트엔드 구현 세부 계획

### 4-1. 디렉토리 구조

```
frontend/app
├── login                page.tsx       로그인 페이지
├── register             page.tsx       회원가입 페이지
├── mypage               page.tsx       내 정보 수정 / 탈퇴 페이지
├── layout.tsx                          루트 레이아웃 (유지)
├── globals.css                         전역 스타일
└── ...
frontend
├── lib
│   ├── axios.ts                        Axios 인스턴스 (interceptor 포함)
│   └── request.ts                      모든 API 호출 함수 모음
├── store
│   └── authStore.ts                    Zustand 유저 인증 상태
└── types
    └── auth.ts                         인증 관련 타입 정의
```

### 4-2. Axios 설정 (`lib/axios.ts`)

- 기본 `baseURL`: `http://localhost:8080/api`
- `withCredentials: true` (Refresh Token 쿠키 자동 전송)
- **Request Interceptor:** `localStorage`(또는 메모리)에서 Access Token을 꺼내 `Authorization: Bearer {token}` 헤더에 추가
- **Response Interceptor:**
  - `401` 오류 발생 시 `/api/auth/refresh` 호출하여 Access Token 재발급
  - 재발급 성공 시 원래 요청 재시도
  - 재발급 실패 시 `/login` 리다이렉트

### 4-3. API 함수 (`lib/request.ts`)

```typescript
// 인증
postRegister(data: RegisterRequest): Promise<ApiResponse<null>>
postLogin(data: LoginRequest): Promise<ApiResponse<LoginData>>
postLogout(): Promise<ApiResponse<null>>
postRefresh(): Promise<ApiResponse<{ accessToken: string }>>

// 유저
getMe(): Promise<ApiResponse<UserData>>
putMe(data: UpdateUserRequest): Promise<ApiResponse<{ userId: number; nickname: string }>>
deleteMe(data: { password: string }): Promise<ApiResponse<null>>
```

### 4-4. Zustand 스토어 (`store/authStore.ts`)

```typescript
interface AuthState {
  accessToken: string | null;
  user: { userId: number; loginId: string; nickname: string; role: string } | null;
  setAuth: (token: string, user: ...) => void;
  clearAuth: () => void;
}
```

- Access Token은 Zustand 메모리에 보관 (XSS 방어를 위해 localStorage 사용 최소화)
- Refresh Token은 HttpOnly 쿠키로만 관리 (프론트에서 직접 접근 불가)

### 4-5. 페이지별 UI 요구사항

#### F-1. `/login` — 로그인 페이지
- 디자인 컨셉: 제빵소 종이 메뉴판 / 아이보리 메인 색상
- 입력: `loginId`, `password`
- 유효성 메시지 표시 (클라이언트 사이드)
- 성공 시: accessToken 저장, Zustand에 유저 정보 저장 → 메인 페이지(`/`) 이동
- 실패 시: 서버 오류 메시지 표시
- 회원가입 링크 포함

#### F-2. `/register` — 회원가입 페이지
- 입력: `loginId` (5자 이상, 영문+숫자), `password` (영문+숫자 각 1자 이상), `nickname` (1자 이상)
- 클라이언트 유효성 검증 후 서버 요청
- 성공 시: 로그인 페이지로 이동, 성공 메시지 표시
- 실패 시: 서버 오류 메시지 표시 (loginId 중복 등)

#### F-3. `/mypage` — 내 정보 수정 / 탈퇴
- 인증되지 않은 사용자는 `/login`으로 리다이렉트
- **닉네임 수정:** 현재 닉네임 표시, 변경 후 저장
- **비밀번호 변경:** 현재 비밀번호 + 새 비밀번호 입력
- **회원 탈퇴:** 비밀번호 확인 후 탈퇴 처리 → 로그아웃 → 메인 이동

---

## 5. 구현 순서 (generator 참고용)

아래 순서대로 구현하면 의존성 문제를 최소화할 수 있다.

### Phase 1: 백엔드 기반 구성
1. `build.gradle` 의존성 추가 (JPA, JWT, Validation)
2. `application.yml` 설정 (DB, JWT 시크릿, 만료 시간)
3. `User` Entity 작성
4. `UserRepository` 작성
5. `ApiResponse` 공통 응답 래퍼 작성

### Phase 2: 인증 인프라
6. `JwtProvider` 작성 (토큰 생성, 검증, 파싱)
7. `CustomUserDetailsService`, `CustomUserDetails` 작성
8. `JwtAuthenticationFilter` 작성
9. `SecurityConfig` 작성 (CORS, CSRF, 세션, 필터 등록)

### Phase 3: Auth 엔드포인트
10. `AuthService` — register, login, logout, refresh 로직
11. `AuthController` — POST /api/auth/*
12. `GlobalExceptionHandler` 작성

### Phase 4: User 엔드포인트
13. `UserService` — getMe, updateMe, deleteMe 로직
14. `UserController` — GET/PUT/DELETE /api/users/me

### Phase 5: 프론트엔드
15. 의존성 설치 (axios, zustand)
16. `lib/axios.ts` — Axios 인스턴스 + interceptor
17. `lib/request.ts` — API 함수
18. `store/authStore.ts` — Zustand 스토어
19. `/login` 페이지 구현
20. `/register` 페이지 구현
21. `/mypage` 페이지 구현

---

## 6. 완료 조건 (tester 검수 기준)

generator는 아래 조건이 모두 충족되어야 구현을 완료로 본다.

### 백엔드
- [ ] `POST /api/auth/register` — loginId 중복 시 409, 유효성 실패 시 400, 성공 시 201
- [ ] `POST /api/auth/login` — 비밀번호 불일치 401, 비활성 403, 탈퇴 403, 성공 시 200 + Refresh Token 쿠키
- [ ] `POST /api/auth/logout` — Refresh Token DB null 처리, 쿠키 Max-Age=0
- [ ] `POST /api/auth/refresh` — 쿠키에서 Refresh Token 추출, DB 비교, 만료/유효하지 않음 구분 401
- [ ] `GET /api/users/me` — 인증 없이 접근 시 401
- [ ] `PUT /api/users/me` — 현재 비밀번호 불일치 400, 새 비밀번호 유효성 400
- [ ] `DELETE /api/users/me` — 비밀번호 불일치 400, status=DELETED 처리
- [ ] 모든 응답이 `{ success, data, message }` 형식을 준수
- [ ] 비밀번호는 BCrypt 해시값으로 저장 (평문 저장 금지)
- [ ] Access Token 유효기간 30분, Refresh Token 유효기간 7일

### 프론트엔드
- [ ] Axios 인터셉터가 Access Token을 요청 헤더에 자동 추가
- [ ] 401 응답 시 자동으로 refresh 시도, 실패 시 `/login` 이동
- [ ] Zustand 스토어에 유저 정보와 Access Token이 저장됨
- [ ] `/login` 페이지: 로그인 성공 시 메인 이동, 실패 시 에러 메시지 표시
- [ ] `/register` 페이지: 유효성 검증 후 요청, 성공 시 `/login` 이동
- [ ] `/mypage` 페이지: 닉네임 수정, 비밀번호 변경, 회원 탈퇴 기능 동작
- [ ] 미인증 상태에서 `/mypage` 접근 시 `/login`으로 리다이렉트
- [ ] 모든 API 호출은 `lib/request.ts`에서 관리

---

## 7. 미정 사항 (planner 판단 불가 — 구현자 결정 필요)

| 항목 | 내용 |
|---|---|
| Refresh Token 저장 위치 | ERD에 명시 없음. 현재 플랜은 DB(`user.refresh_token` 컬럼 추가)로 결정. Redis 미도입. |
| Access Token 프론트 저장 방식 | Zustand 메모리 보관을 기본으로 설정. 새로고침 시 소실 → `/api/auth/refresh`로 복구. |
| 회원가입 후 자동 로그인 여부 | 명세에 없으므로 **자동 로그인 없이 `/login`으로 이동**으로 결정. |

---

## 8. 명시적 제외 범위

이번 플랜에서 **포함하지 않는** 기능:
- 추천글, 구매 과자, 댓글, 피드백 관련 API
- 관리자 유저 관리 API (`/api/admin/*`)
- JUnit 테스트 코드 (별도 플랜으로 분리)
- Vitest 테스트 코드 (별도 플랜으로 분리)
