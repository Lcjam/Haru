# CoreService 백엔드

## 프로젝트 소개 및 목적
CoreService는 사용자들이 취미를 공유하고 소통할 수 있는 종합 플랫폼입니다. 본 백엔드 시스템은 회원 관리, 프로필 관리, 취미 등록 및 조회, 실시간 채팅 기능을 제공하며, 사용자 간의 원활한 커뮤니케이션과 취미 기반 네트워킹을 지원합니다.

특히 JWT 기반의 안전한 인증 체계와 WebSocket을 활용한 실시간 통신으로 사용자 경험을 향상시키고, 취미를 중심으로 한 사용자 연결을 촉진합니다.

## 기술 스택
- **언어**: Java
- **프레임워크**: Spring Boot
- **데이터베이스**: MySQL (사용자, 취미, 상품 데이터 저장)
- **캐싱 및 메시징**: Redis (WebSocket 메시지 브로커, 토큰 블랙리스트)
- **인증**: JWT (JSON Web Token)
- **실시간 통신**: WebSocket, STOMP 프로토콜
- **파일 저장**: 로컬 파일 시스템 (프로필/상품 이미지)
- **API 문서화**: Swagger/OpenAPI
- **트랜잭션 관리**: Spring `@Transactional`

## 로컬 환경 설치법

### 사전 요구사항
- JDK 11 이상
- MySQL 8.0 이상
- Redis 6.0 이상
- Maven 또는 Gradle

### 설치 단계
1. 레포지토리 클론

2. MySQL 데이터베이스 설정


3. `application.properties` 또는 `application.yml` 파일 설정

4. 애플리케이션 실행

5. 서버는 기본적으로 `http://localhost:8081`에서 실행됩니다

## 주요 백엔드 기능

### 1. 회원 관리 시스템
- **회원가입**: 이메일 기반 회원가입 및 취미 등록 (`AuthService.registerUser`)
- **로그인/로그아웃**: JWT 토큰 기반 인증 시스템 (`AuthService.login`, `AuthService.logout`)
- **소셜 로그인 지원**: Google, Kakao, Naver 연동 (`SecurityConfig`, `JwtAuthenticationFilter`)
- **비밀번호 관리**: 안전한 비밀번호 해싱 및 변경 기능 (`PasswordUtils`, `ProfileService.changePassword`)
- **회원 탈퇴**: 사용자 정보 익명화 및 계정 비활성화 (`WithdrawalService.withdrawUser`)

### 2. 프로필 관리
- **프로필 조회/수정**: 사용자 정보 및 취미 관리 (`ProfileService.getUserProfile`, `ProfileService.updateProfile`)
- **프로필 이미지**: 이미지 업로드, 조회, 삭제 기능 (`ProfileImageService`)
- **마이페이지**: 사용자 활동 정보 요약 제공 (`ProfileService.getMyPageInfo`)
- **도파민 수치 및 활동 포인트**: 사용자 참여도 측정 지표 관리

### 3. 취미 관리 시스템
- **취미 카테고리**: 계층적 취미 분류 체계 (`HobbyService`)
- **사용자별 취미 등록/조회**: 개인화된 취미 정보 관리 (`HobbyService.registerUserHobbies`, `HobbyService.getUserHobbies`)
- **카테고리-취미 관계**: 체계적인 취미 분류 및 검증 로직

### 4. 실시간 채팅 시스템
- **WebSocket 기반 1:1 채팅**: STOMP 프로토콜을 활용한 실시간 메시징 (`ChatMessageService.sendMessage`)
- **채팅방 관리**: 상품 기반 채팅방 생성 및 관리
- **메시지 상태 관리**: 읽음 상태 추적 및 업데이트 (`ChatMessageService.markMessagesAsRead`)
- **메시지 저장 및 페이징**: 대화 내역 영구 저장 및 효율적 조회 (`ChatMessageService.getChatMessages`)
- **Redis 메시지 브로커**: 실시간 메시지 전달 및 분산 처리

### 5. 취미 기반 마켓 시스템
- **상품 등록/조회**: 구매/판매 상품 관리 (`ProductService.createProduct`, `ProductService.getProducts`)
- **이미지 업로드**: 상품 이미지 멀티 업로드 지원 (`ImageUploadService`)
- **상품 요청 및 승인**: 구매자-판매자 간 요청/승인 프로세스 (`ProductService.createProductRequest`, `ProductService.approveProductRequest`)
- **상품 필터링 및 정렬**: 카테고리별, 최신순/가격순 정렬 기능 (`ProductService.getProducts`)
- **사용자별 상품 관리**: 등록 상품 및 요청 상품 조회 기능

## API 엔드포인트

### 인증 API
- `POST /api/core/auth/signup`: 회원가입
- `POST /api/core/auth/login`: 로그인
- `POST /api/core/auth/logout`: 로그아웃
- `POST /api/core/auth/me/withdrawal`: 회원 탈퇴
- `PUT /api/core/auth/me/password`: 비밀번호 변경 (로그인 상태)
- `PUT /api/core/auth/me/password/notoken`: 비밀번호 변경 (비로그인 상태)

### 프로필 API
- `GET /api/core/profiles/me`: 자신의 프로필 조회
- `GET /api/core/profiles/user/{nickname}`: 닉네임으로 프로필 조회
- `PUT /api/core/profiles/me`: 프로필 정보 수정
- `POST /api/core/profiles/me/image`: 프로필 이미지 업로드
- `DELETE /api/core/profiles/me/image`: 프로필 이미지 삭제
- `GET /api/core/profiles/mypage`: 마이페이지 정보 조회

### 취미 관련 API
- `GET /api/core/hobbies`: 전체 취미 목록 조회
- `GET /api/core/hobbies/categories`: 취미 카테고리 조회
- `GET /api/core/hobbies/categories/{id}`: 카테고리별 취미 목록 조회

### 채팅 API
- WebSocket 연결: `/ws`
- 메시지 전송: `/app/chat.sendMessage`
- 메시지 구독: `/topic/chatroom.{chatroomId}`
- `GET /api/core/chat/messages/{chatroomId}`: 채팅 메시지 조회
- `POST /api/core/chat/messages/read/{chatroomId}`: 메시지 읽음 상태 업데이트

### 마켓 API
- `POST /api/core/market/products/registers`: 상품 등록
- `POST /api/core/market/products/requests`: 상품 요청 등록
- `POST /api/core/market/products/requests/approve`: 상품 요청 승인
- `GET /api/core/market/products/all`: 전체 상품 목록 조회
- `POST /api/core/market/products/all/filter`: 필터링된 상품 목록 조회
- `GET /api/core/market/products/{id}`: 개별 상품 조회
- `GET /api/core/market/products/users/registers/buy`: 내가 등록한 구매 상품 조회
- `GET /api/core/market/products/users/registers/sell`: 내가 등록한 판매 상품 조회

## 개발 과정에서의 이슈와 해결법

### 1. JWT 토큰 관리와 로그아웃 구현
**이슈**: JWT는 stateless 특성 때문에 서버에서 토큰 무효화가 어려워 로그아웃 구현이 복잡했습니다.

**해결법**: Redis를 활용한 토큰 블랙리스트를 구현하여 로그아웃된 토큰을 관리했습니다. 만료 시간을 Redis TTL과 연동하여 자동 정리되도록 설계했습니다.
```java
jwtTokenBlacklistService.addToBlacklist(token, expiryMillis);
```

### 2. 실시간 채팅과 메시지 상태 관리
**이슈**: WebSocket을 통한 실시간 채팅에서 메시지 전송, 저장, 상태 관리를 일관되게 처리하기 어려웠습니다.

**해결법**: 
- Redis의 pub/sub 기능을 활용해 실시간 메시지 전달
- 메시지 영구 저장은 MySQL에 구현
- 읽음 상태는 별도 테이블로 관리하여 성능 최적화
```java
// 메시지 발행
redisTemplate.convertAndSend(channelTopic.getTopic(), message);
// 메시지 저장
chatMessageMapper.saveChatMessage(message);
```

### 3. 트랜잭션 관리와 데이터 일관성
**이슈**: 회원가입, 프로필 수정, 취미 등록 등 여러 테이블이 연관된 작업에서 부분 실패 시 데이터 일관성 문제가 발생했습니다.

**해결법**: Spring의 `@Transactional` 어노테이션을 활용하여 원자적 트랜잭션 처리를 구현했습니다. 특히 회원가입 과정에서 사용자 기본 정보, 계정 정보, 취미 정보의 일관성을 보장했습니다.
```java
@Transactional
public SignupResponse registerUser(SignupRequest request) {
    // 사용자 등록, 계정 정보 추가, 취미 등록이 하나의 트랜잭션으로 처리
}
```

### 4. 다중 이미지 업로드 및 관리
**이슈**: 상품 등록 시 여러 이미지를 업로드하고 관리하는 과정에서 파일 시스템과 데이터베이스 간의 동기화 문제가 있었습니다.

**해결법**: 트랜잭션 내에서 이미지 업로드와 메타데이터 저장을 함께 처리하고, 실패 시 업로드된 파일을 자동으로 삭제하는 롤백 메커니즘을 구현했습니다.
```java
// 이미지 업로드 및 DB 저장을 하나의 작업으로 처리
ResponseEntity<Object> response = imageUploadService.uploadProductImages(email, productId, images);
if (response.getStatusCode().is2xxSuccessful()) {
    // 메타데이터 저장
}
```

## 기여자
- **Backend Developer**: 본인 - 회원 관리, 인증 시스템, 취미 기능, 프로필 관리, 채팅 시스템
