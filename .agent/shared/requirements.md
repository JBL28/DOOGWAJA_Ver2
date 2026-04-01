# 두과자 (DooGwaJa)

- 이 프로젝트는 방 구성원의 과자 선호를 공유하는 서비스이다.
- 프로젝트는 기본적으로, next.js+tailwind 프론트엔드 + springboot 백엔드, mysql DB로 구성된다.
- 모든 기능 구현은 프론트엔드와 백엔드의 안정적인 연결을 1순위로 한다.
- 유지보수가 용이한, 구조화된 코딩을 지양한다.


## 메인 기능

- **인증/인가**
    - 사용자 인증/인가
    - ID는 5자 이상, PW는 영어와 숫자를 포함해야 한다.
    - ID와 PW는 유효성 검증이 이뤄져야한다.
    - 비밀번호는 해시한 값을 저장
- **과자 추천**
    - 먹고싶은 과자를 추천
    - 과자의 이름과 추천 이유를 포함한다.
    - 다른 사용자가 댓글을 달 수 있음
    - 댓글은 기본 3개를 보여주는 것을 디폴트로 하며, 3개 이상일 시 펼치기/접기가 가능하다. (default: 접기)
    - 다른 사용자가 피드백을 달 수 있다.
- **구매한 과자**
    - 관리자가 공용 간식으로 구매한 간식을 관리
    - 관리자가 아닌 사용자가 댓글을 달 수 있음
    - 댓글은 기본 3개를 보여주는 것을 디폴트로 하며, 3개 이상일 시 펼치기/접기가 가능하다. (default: 접기)
    - 관리자가 아닌 사용자가 피드백을 달 수 있다.
    - 과자의 상태는 모두가 변경할 수 있다. (배송중|재고있음|재고없음)
- **과자 피드백**
    - 과자의 상태를 설정 가능
    - 좋아요/싫어요로 피드백 가능
    - 인당 좋아요/싫어요 중 하나만 가능
- **댓글**
    - 구매한 과자 게시글 혹은 추천 게시글에 댓글을 달 수 있다.
    - 댓글은 로그인한 사용자라면 어느 게시글이든 자유롭게 작성 가능하다.
    - 각각의 댓글에는 피드백을 남길 수 있다. (like/dislike)
    - 대댓글 기능은 MVP에서 제외한다.
    - 댓글은 기본 3개를 보여주는 것을 디폴트로 하며, 3개 이상일 시 펼치기/접기가 가능하다. (default: 접기)
    - 댓글이 접힌 상태라면 3개까지 보여야하며, 펼쳐진 경우 모든 댓글을 볼 수 있어야한다.
- **관리자**
    - 관리자 계정으로 로그인 시 구매한 과자를 관리할 수 있어야한다.
    - 가입한 계정을 관리할 수 있다. (삭제/비활성화)

- **권한 정보**
    - **일반 사용자 (USER)**
        - 구매한 과자 조회 가능
        - 구매한 과자에 댓글 작성 가능
        - 구매한 과자에 좋아요/싫어요 가능
        - 과자 추천글 생성/수정/삭제 가능
        - 다른 사용자의 과자 추천글 조회 가능
        - 자신의 추천글/ 다른 사용자의 추천글 댓글 작성 가능
        - 자신의 추천글/ 다른 사용자의 추천글 좋아요/싫어요 가능
    - **관리자 (ADMIN)**
        - 구매한 과자 CRUD 가능
        - 회원 관리 가능

    **필요 페이지**
    1. 로그인
    2. 회원가입
    3. 유저 정보 수정
        - nickname, password 수정이 가능하다.
        - 회원 탈퇴가 가능하다.
    4. 메인 페이지
        - 아래 요소를 한 화면에 보여준다.
        - 10개씩 pagenation이 적용된다.
        - 구매한 과자 리스트
            - 댓글과 피드백(피드백은 좋아요/싫어요를 의미한다.)이 바로 보여야한다.
            - 댓글이 3개가 넘어갈 시 펼치기/접기가 가능하다. (default: 접기, 접기 시에도 3개는 보여야한다.)
        - 과자 추천 리스트
            - 댓글과 피드백(피드백은 좋아요/싫어요를 의미한다.)이 바로 보여야한다.
            - 댓글이 3개가 넘어갈 시 펼치기/접기가 가능하다. (default: 접기, 접기 시에도 3개는 보여야한다.)
    5. 과자 추천/구매 수정 페이지
        - 작성된 게시글을 수정할 수 있다.

    **프로젝트 구조**
    - 프론트엔드
        - access token과 refresh token의 생애주기가 백엔드와 원활하게 연계되어야한다.
        - axios를 사용해 백엔드와 통신한다.
            - request.ts에서 모든 api 엔드포인트를 관리한다.
            - axios 객체를 별도의 컴포넌트로 만들어두어 다른 컴포넌트에서 활용한다.
            - 예외처리를 단단하게 구축해둔다.
        - zustand를 사용해 상태를 관리한다.
            - 로그인한 유저 정보, 다크모드 여부 등을 관리한다.
        - 디자인은 다음의 규칙을 따른다.    
            - 기본적인 컨셉은 제빵소의 종이 메뉴판이다.
            - 아이보리 색상을 메인 색상으로 사용한다.
            - 종이 질감을 사용해 따뜻한 분위기를 연출한다.
            - @media 쿼리를 활용해 화면 크기에 따른 반응형 UI를 구현한다.
    
    - 백엔드
        - springsecurity를 사용해 인증/인가를 구현한다.
            - access token과 refresh token을 사용한다.
            - access token과 refresh token의 생애주기가 프론트엔드와 원활하게 연계되어야한다.
        - 도메인별로 폴더를 만들어 관리한다.
            - 각 도메인별 폴더는 다음과 같은 구조를 가진다.
                - controller
                - service
                - repository
                - dto
                - entity
                - util
        - 기능 구현 시 1순위는 안정적인 동작이며, 2순위는 유지보수 용이성이다.

# ERD

## 1. user
사용자 정보를 저장하는 테이블이다.

- `user_id` : PK, INT, AUTO_INCREMENT
- `login_id` : VARCHAR, NOT NULL, UNIQUE  
  - 로그인에 사용하는 아이디
  - 5자 이상
- `password_h` : VARCHAR, NOT NULL  
  - 비밀번호 해시값
- `nickname` : VARCHAR, NOT NULL  
  - 화면 표시용 닉네임
- `role` : ENUM('ADMIN', 'USER'), NOT NULL, DEFAULT 'USER'
- `status` : ENUM('ACTIVATED', 'DEACTIVATED', 'DELETED'), NOT NULL, DEFAULT 'ACTIVATED'
- `created_at` : DATETIME, NOT NULL
- `updated_at` : DATETIME, NOT NULL

### 설명
- 로그인은 `login_id + password_h` 기준으로 처리한다.
- `nickname`은 수정 가능하다.
- 회원 탈퇴는 실제 삭제 대신 `status = DELETED` 처리도 가능하다.
- 관리자 계정 여부는 `role`로 구분한다.

---

## 2. recommendation
사용자가 먹고 싶은 과자를 추천하는 게시글 테이블이다.

- `rc_id` : PK, INT, AUTO_INCREMENT
- `user_id` : FK -> user.user_id, NOT NULL
- `name` : VARCHAR, NOT NULL  
  - 추천 과자 이름
- `reason` : VARCHAR or TEXT, NOT NULL  
  - 추천 이유
- `created_at` : DATETIME, NOT NULL
- `updated_at` : DATETIME, NOT NULL

### 설명
- 일반 사용자가 생성/수정/삭제 가능하다.
- 작성자는 `user_id`로 연결한다.

---

## 3. rc_comment
추천 게시글에 달리는 댓글 테이블이다.

- `rcc_id` : PK, INT, AUTO_INCREMENT
- `rc_id` : FK -> recommendation.rc_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `content` : VARCHAR or TEXT, NOT NULL
- `created_at` : DATETIME, NOT NULL
- `updated_at` : DATETIME, NOT NULL

### 설명
- 로그인한 사용자는 추천글에 자유롭게 댓글 작성 가능하다.
- 대댓글 기능은 MVP에서 제외한다.

---

## 4. rc_feedback
추천 게시글 자체에 대한 좋아요/싫어요 피드백 테이블이다.

- `id` : PK, INT, AUTO_INCREMENT
- `rc_id` : FK -> recommendation.rc_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `status` : ENUM('LIKE', 'DISLIKE'), NOT NULL

### 제약
- `UNIQUE (user_id, rc_id)`

### 설명
- 한 사용자는 하나의 추천 게시글에 대해 좋아요/싫어요 중 하나만 남길 수 있다.

---

## 5. rcc_feedback
추천 게시글의 댓글에 대한 좋아요/싫어요 피드백 테이블이다.

- `id` : PK, INT, AUTO_INCREMENT
- `rcc_id` : FK -> rc_comment.rcc_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `status` : ENUM('LIKE', 'DISLIKE'), NOT NULL

### 제약
- `UNIQUE (user_id, rcc_id)`

### 설명
- 한 사용자는 하나의 추천 댓글에 대해 좋아요/싫어요 중 하나만 남길 수 있다.
- `rc_id`는 댓글 테이블을 통해 추적 가능하므로 중복 저장하지 않는다.

---

## 6. bought_snack
관리자가 공용 간식으로 등록한 구매 과자 테이블이다.

- `bs_id` : PK, INT, AUTO_INCREMENT
- `name` : VARCHAR, NOT NULL  
  - 구매한 과자 이름
- `status` : ENUM('SHIPPING', 'IN_STOCK', 'OUT_OF_STOCK'), NOT NULL, DEFAULT 'SHIPPING'
  - 배송중 / 재고있음 / 재고없음
- `created_at` : DATETIME, NOT NULL
- `updated_at` : DATETIME, NOT NULL

### 설명
- 구매한 과자는 관리자만 CRUD 가능하다.
- 상태는 요구사항에 따라 사용자들이 변경 가능하도록 서비스 로직에서 처리할 수 있다.
- 한글 대신 영문 ENUM 값을 쓰고, 화면에서 한글로 매핑하는 방식을 권장한다.

---

## 7. bs_comment
구매한 과자 게시글에 달리는 댓글 테이블이다.

- `bsc_id` : PK, INT, AUTO_INCREMENT
- `bs_id` : FK -> bought_snack.bs_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `content` : VARCHAR or TEXT, NOT NULL
- `created_at` : DATETIME, NOT NULL
- `updated_at` : DATETIME, NOT NULL

### 설명
- 로그인한 일반 사용자는 구매한 과자 게시글에 댓글 작성 가능하다.
- 관리자도 정책상 허용 여부를 서비스 단에서 제어할 수 있다.
- 댓글 내용은 문자열 타입이어야 한다.

---

## 8. bs_feedback
구매한 과자 게시글 자체에 대한 좋아요/싫어요 피드백 테이블이다.

- `id` : PK, INT, AUTO_INCREMENT
- `bs_id` : FK -> bought_snack.bs_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `status` : ENUM('LIKE', 'DISLIKE'), NOT NULL

### 제약
- `UNIQUE (user_id, bs_id)`

### 설명
- 한 사용자는 하나의 구매 과자 게시글에 대해 좋아요/싫어요 중 하나만 남길 수 있다.

---

## 9. bsc_feedback
구매한 과자 게시글의 댓글에 대한 좋아요/싫어요 피드백 테이블이다.

- `id` : PK, INT, AUTO_INCREMENT
- `bsc_id` : FK -> bs_comment.bsc_id, NOT NULL
- `user_id` : FK -> user.user_id, NOT NULL
- `status` : ENUM('LIKE', 'DISLIKE'), NOT NULL

### 제약
- `UNIQUE (user_id, bsc_id)`

### 설명
- 한 사용자는 하나의 구매 댓글에 대해 좋아요/싫어요 중 하나만 남길 수 있다.

---

# 테이블 관계 요약

## user 기준
- user 1 : N recommendation
- user 1 : N rc_comment
- user 1 : N rc_feedback
- user 1 : N rcc_feedback
- user 1 : N bs_comment
- user 1 : N bs_feedback
- user 1 : N bsc_feedback

## recommendation 기준
- recommendation 1 : N rc_comment
- recommendation 1 : N rc_feedback

## rc_comment 기준
- rc_comment 1 : N rcc_feedback

## bought_snack 기준
- bought_snack 1 : N bs_comment
- bought_snack 1 : N bs_feedback

## bs_comment 기준
- bs_comment 1 : N bsc_feedback

---

# 구현 시 중요한 제약 요약

## 유저
- `login_id`는 반드시 UNIQUE
- `password_h`에는 평문 비밀번호 저장 금지
- `status`는 NULL 허용하지 않음

## 게시글/댓글
- 게시글과 댓글은 모두 `created_at`, `updated_at` 필요
- 댓글 `content`는 문자열 타입이어야 함

## 피드백
- 게시글 피드백:
  - 추천글: `UNIQUE(user_id, rc_id)`
  - 구매글: `UNIQUE(user_id, bs_id)`
- 댓글 피드백:
  - 추천댓글: `UNIQUE(user_id, rcc_id)`
  - 구매댓글: `UNIQUE(user_id, bsc_id)`

---

# 에이전트용 추가 해설

이 프로젝트의 DB 구조는 크게 4개 축으로 나뉜다.

1. 사용자
2. 추천 게시글
3. 구매한 과자 게시글
4. 각 게시글 및 댓글에 대한 피드백

추천 게시글과 구매한 과자 게시글은 성격이 다르므로 테이블을 분리한다.  
댓글도 각 게시글 종류별로 분리한다.  
피드백은 게시글 피드백과 댓글 피드백을 분리한다.  
모든 피드백은 한 사용자가 하나의 대상에 대해 좋아요/싫어요 중 하나만 가능하도록 UNIQUE 제약을 둔다.

구현 시 권한 처리는 DB가 아니라 백엔드 서비스 및 Spring Security에서 담당한다.

- USER:
  - 추천글 CRUD 가능
  - 추천글/구매글 조회 가능
  - 댓글 작성 가능
  - 피드백 가능
- ADMIN:
  - 구매한 과자 CRUD 가능
  - 회원 관리 가능

# 비기능 요구사항
- 프론트엔드는 Vitest, 백엔드는 JUnit으로 테스트를 진행한다.
    - 테스트 코드 작성 원칙은 다음과 같다.
        1. 한 테스트는 한 가지 사실만 검증한다
        2. 구현이 아니라 동작을 검증한다
        3. 테스트 이름만 봐도 의도가 드러나야 한다
        4. 외부 의존성은 끊고, 테스트 대상을 고립시킨다
            - mock
            - stub
            - fake
            - spy
        5. 입력과 기대 결과를 명확하게 드러낸다
        6. 성공 케이스만 쓰지 말고 경계값과 실패 케이스를 꼭 포함한다
        7. 테스트끼리 서로 의존하지 않게 만든다
        8. 불안정한 테스트를 만들지 않는다
        9. 너무 많은 것을 검증하지 않는다
        10. 테스트도 코드이므로 읽기 좋게 관리한다

    