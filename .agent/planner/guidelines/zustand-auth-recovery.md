# [지침] Next.js + Zustand 인증 상태 복구 설계 주의사항

> **작성 배경:** 인증/유저 기능 구현 후, 로그인 성공 → 메인 페이지 → 마이페이지 이동 시
> Zustand accessToken이 소실되어 불필요하게 로그인 화면으로 튕기는 문제가 발생.
> planner가 이 구조적 위험을 미리 인지하고 계획에 반영하기 위한 지침.

---

## 핵심 원칙

**Zustand는 JavaScript 런타임 메모리에만 존재한다.**
페이지 새로고침, 직접 URL 입력, 탭 복원 등 어떤 상황에서든 Zustand 상태는 초기화된다.
Refresh Token(HttpOnly 쿠키)은 살아있어도, accessToken이 없으면 인증 실패로 처리된다.

---

## planner가 인증이 필요한 페이지를 계획할 때 반드시 명시해야 하는 것

### 1. 인증 체크 로직의 순서를 명시한다

**금지 패턴:**
```
인증 필요 페이지 진입 시:
- accessToken이 없으면 /login으로 이동
```

**올바른 패턴:**
```
인증 필요 페이지 진입 시:
1. accessToken(Zustand)이 있으면 → 그대로 진행
2. accessToken이 없으면 → /api/auth/refresh 호출 (쿠키 자동 전송)
   - refresh 성공 → 새 accessToken을 Zustand에 저장 → 진행
   - refresh 실패 → /login으로 이동
```

이 순서를 명시하지 않으면 generator는 단순한 `!accessToken → redirect` 패턴으로 구현한다.

---

### 2. Next.js Server Component / Client Component 구분을 명시한다

Next.js App Router에서 **Server Component로 이동하면 JS 번들이 재실행**되어 Zustand가 초기화된다.

planner는 각 페이지에 대해 아래를 명시해야 한다:

| 페이지 | Component 타입 | 이유 |
|---|---|---|
| 인증이 필요한 페이지 (`/mypage` 등) | `'use client'` 필수 | Zustand 접근, useEffect 사용 |
| 공개 페이지 (`/`, `/about` 등) | Server Component 가능 | 단, 다른 페이지로의 **링크는 `<Link>` 사용 필수** |

---

### 3. 페이지 간 이동 방식을 명시한다

**`<a href="...">` 사용 금지** — 브라우저 풀 리로드 발생 → Zustand 초기화

**반드시 Next.js `<Link href="...">` 사용** — 클라이언트 사이드 라우팅 → Zustand 유지

planner 산출물에 아래 완료 조건을 추가한다:
```
- 페이지 간 모든 이동은 Next.js <Link> 컴포넌트를 사용한다
- <a href>로 내부 페이지 이동을 구현하지 않는다
```

---

## planner 산출물에 추가해야 하는 항목

인증이 필요한 페이지가 포함된 플랜에는 아래 항목을 **명시적으로** 포함한다.

### 세부 정책 섹션에 추가
```
#### 인증 상태 복구 정책
- 페이지 진입 시 accessToken(Zustand)이 없어도 즉시 /login으로 보내지 않는다.
- /api/auth/refresh를 먼저 호출하여 Refresh Token 쿠키로 복구를 시도한다.
- refresh 성공 시: 새 accessToken을 Zustand에 저장하고 정상 진행한다.
- refresh 실패 시: /login으로 리다이렉트한다.
```

### 완료 조건 섹션에 추가
```
- [ ] 로그인 후 새로고침해도 인증이 필요한 페이지에 정상 진입된다 (쿠키가 유효한 경우)
- [ ] Refresh Token이 만료된 경우에만 /login으로 리다이렉트된다
- [ ] 내부 페이지 이동은 모두 <Link> 컴포넌트를 사용한다
```

---

## generator 전달 메모 예시

```
- 인증 필요 페이지는 반드시 'use client' 선언
- accessToken이 없을 때 즉시 redirect하지 말고 postRefresh()를 먼저 시도할 것
- 내부 페이지 링크는 <a href> 대신 Next.js <Link> 사용
- Zustand는 새로고침 시 초기화되므로, 이를 전제로 설계할 것
```

---

## tester 체크 항목 예시

```
- 로그인 → 해당 페이지 이동 → 새로고침 → 페이지가 정상 표시되는가
- 로그인 → 해당 페이지 URL 직접 입력 → 페이지가 정상 표시되는가
- Refresh Token 만료 상태에서 해당 페이지 접근 시 /login으로 이동하는가
- 페이지 내 링크가 <a href>가 아닌 <Link>로 구현되어 있는가
```
