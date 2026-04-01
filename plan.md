# Implementation Plan - 좋아요 표시한 사람 목록 조회 기능

본 계획은 사용자가 게시글(추천글, 구매 과자)의 좋아요 수를 클릭하거나 마우스를 올렸을 때, 해당 글에 좋아요를 표시한 사용자 목록을 최대 20명까지 보여주는 기능을 추가하는 것을 목표로 합니다.

## User Review Required

> [!IMPORTANT]
> - **최대 노출 수**: 20명으로 제한하며, 초과 시 `...` 또는 `외 N명` 등으로 표시합니다.
> - **노출 시점**: 현재 메인 페이지에서 '👍' 아이콘이나 숫자 근처에 마우스 오버 시 정보를 가져와 보여주는 방식을 제안합니다.
> - **개인정보**: 현재 시스템의 `User.nickname` 필드를 노출합니다.

## Proposed Changes

### [Backend]

#### [NEW] [LikedUsersResponseDTO.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/common/dto/LikedUsersResponseDTO.java)
- 좋아요 누른 유저 목록과 초과 여부를 담는 공통 DTO
- `List<UserNicknameDTO> users`
- `boolean hasMore`

#### [NEW] [UserNicknameDTO.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/user/dto/UserNicknameDTO.java)
- 최소한의 유저 정보(ID, 닉네임)만 담는 DTO

#### [MODIFY] [RcFeedbackRepository.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/rc_feedback/repository/RcFeedbackRepository.java)
- `findTop21ByRecommendationAndStatusOrderByCreatedAtDesc` 추가

#### [MODIFY] [BsFeedbackRepository.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/bs_feedback/repository/BsFeedbackRepository.java)
- `findTop21ByBoughtSnackAndStatusOrderByCreatedAtDesc` 추가

#### [MODIFY] [RecommendationController.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/recommendation/controller/RecommendationController.java)
- `GET /api/recommendations/{rcId}/likes` 엔드포인트 추가

#### [MODIFY] [BoughtSnackController.java](file:///c:/Users/SSAFY/lab/DOGWAJA/backend/src/main/java/dev/ssafy/bought_snack/controller/BoughtSnackController.java)
- `GET /api/bought-snacks/{bsId}/likes` 엔드포인트 추가

### [Frontend]

#### [MODIFY] [types/recommendation.ts](file:///c:/Users/SSAFY/lab/DOGWAJA/frontend/types/recommendation.ts)
- `LikedUsersResponseDTO` 인터페이스 정의

#### [MODIFY] [lib/request.ts](file:///c:/Users/SSAFY/lab/DOGWAJA/frontend/lib/request.ts)
- `getRecommendationLikes(rcId)` API 함수 추가
- `getBoughtSnackLikes(bsId)` API 함수 추가

#### [MODIFY] [app/page.tsx](file:///c:/Users/SSAFY/lab/DOGWAJA/frontend/app/page.tsx)
- 각 카드 컴포넌트에 좋아요 목록 상태(`likedUsersMap`) 추가
- 👍 버튼 근처에 마우스 오버 시 목록을 비동기로 로드
- 20명 초과 시 `...` 표시 로직 구현

## Open Questions

- **UI 형태**: 단순 툴팁(Tooltip) 형태가 좋을까요, 아니면 클릭 시 작은 팝업(Pop-over) 형태가 좋을까요? (현재는 툴팁 방식을 제안합니다.)

## Verification Plan

### Automated Tests
- Backend: JUnit을 사용하여 20명 이하/초과 시 `hasMore` 값이 정확히 반환되는지 테스트.
- Frontend: Vitest를 사용하여 데이터 수신에 따른 `...` 표시 여부 로직 검증.

### Manual Verification
- 21명 이상의 더미 데이터를 넣고 메인 페이지에서 정상적으로 `...`이 출력되는지 확인.
