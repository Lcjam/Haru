# 🌸 Haru (하루)

> "하고싶은 취미를 루틴처럼 하루에 하나씩" - 취미 기반 지역 거래 플랫폼

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.0.0-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7.2-blue.svg)](https://www.typescriptlang.org/)

## 📖 프로젝트 소개

**Haru(하루)**는 취미를 가진 사람들이 자신의 재능과 취미를 공유하고 거래할 수 있는 지역 기반 플랫폼입니다. 

### 🎯 만든 이유

- **취미 공유의 어려움**: 혼자 즐기기엔 아쉬운 취미를 함께 즐길 사람을 찾기 어려움
- **지역 기반 연결**: 가까운 거리에 있는 사람들과 쉽게 만나고 소통할 수 있는 공간 필요
- **취미의 상업화**: 취미를 재능으로 바꾸고, 그것을 통해 소득을 얻을 수 있는 기회 제공
- **커뮤니티 형성**: 같은 취미를 가진 사람들끼리 지속적인 관계를 형성할 수 있는 환경 조성

### ✨ 주요 기능

- 🛍️ **취미 마켓플레이스**: 취미 상품 등록, 구매/판매, 함께하기 요청
- 💬 **실시간 채팅**: WebSocket 기반 1:1 채팅 및 알림 시스템
- 📍 **위치 기반 서비스**: 사용자 위치 기반 상품 검색 및 거래
- 👥 **취미 커뮤니티**: 취미별 전용 게시판 및 댓글 시스템
- 🤖 **AI 고객센터**: LLaMA 기반 챗봇 "Luffy"를 통한 고객 지원
- 🔐 **소셜 로그인**: Google, Kakao, Naver OAuth2 연동
- 📱 **PWA 지원**: 모바일 앱처럼 사용 가능한 Progressive Web App

## 🛠️ 기술 스택

### Backend

| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | 21 | 백엔드 개발 언어 |
| **Spring Boot** | 3.3.7 | 백엔드 프레임워크 |
| **Spring Cloud Gateway** | 2023.0.0 | API Gateway |
| **Spring Security** | - | 인증/인가 |
| **Spring WebSocket/STOMP** | - | 실시간 통신 |
| **JWT** | 0.11.5 | 토큰 기반 인증 |
| **MyBatis** | 3.0.4 | ORM |
| **MySQL** | 8.0+ | 관계형 데이터베이스 |
| **Redis** | - | 세션 관리 및 Pub/Sub |
| **OAuth2** | - | 소셜 로그인 |

### Frontend

| 기술 | 버전 | 용도 |
|------|------|------|
| **React** | 19.0.0 | UI 프레임워크 |
| **TypeScript** | 5.7.2 | 타입 안정성 |
| **Vite** | 6.1.0 | 빌드 도구 |
| **Redux Toolkit** | 2.5.1 | 상태 관리 |
| **React Query** | 5.66.0 | 서버 상태 관리 |
| **React Router** | 7.1.5 | 라우팅 |
| **STOMP.js** | 7.0.1 | WebSocket 클라이언트 |
| **Tailwind CSS** | 3.4.1 | 스타일링 |
| **Leaflet** | 1.9.4 | 지도 기능 |

### AI & 기타

| 기술 | 용도 |
|------|------|
| **FastAPI** | AI 서비스 (Python) |
| **LLaMA/TinyLlama** | LLM 챗봇 |
| **Naver Cloud OCR** | 이미지 텍스트 인식 |
| **OpenCV.js** | 카드 인식 기능 |

## 🏗️ 아키텍처

```
┌─────────────┐
│   Client    │ (React + TypeScript)
│  (Port 3000)│
└──────┬──────┘
       │
┌──────▼──────┐
│   GateWay   │ (Spring Cloud Gateway)
│  (Port 8080)│
└──────┬──────┘
       │
   ┌───┴───┬──────────────┬─────────────┐
   │       │              │             │
┌──▼──┐ ┌──▼──┐      ┌───▼───┐    ┌───▼────┐
│Core │ │Assist│      │FastAPI│    │ Redis  │
│Service││Service│      │(8001)│    │(6379)  │
│(8081)│ │(8082)│      └──────┘    └────────┘
└──┬──┘ └──┬──┘
   │       │
┌──▼───────▼──┐
│   MySQL     │
│  (haru_db)  │
└─────────────┘
```

### 서비스 구성

- **GateWay**: API 라우팅, JWT 인증 필터링
- **CoreService**: 핵심 비즈니스 로직 (인증, 채팅, 게시판, 마켓플레이스)
- **AssistService**: 보조 서비스 (AI 챗봇, OCR, SMS)
- **FastAPI**: LLM 기반 AI 서비스

## 🚀 시작하기

### 필수 요구사항

- **Java** 21 이상
- **Node.js** 18 이상
- **MySQL** 8.0 이상
- **Redis** (채팅 기능용)
- **npm** 또는 **yarn**

### 1. 저장소 클론

```bash
git clone https://github.com/Lcjam/Haru.git
cd Haru
```

### 2. 데이터베이스 설정

#### MySQL 설치 및 실행

```bash
# macOS (Homebrew)
brew install mysql
brew services start mysql

# MySQL 접속
mysql -u root -p
```

#### 데이터베이스 생성

```bash
# 통합 스크립트 사용 (권장)
mysql -u root -p < MySQL/setup_database.sql

# 또는 수동 생성
mysql -u root -p
```

```sql
CREATE DATABASE haru_db;
USE haru_db;
-- setup_database.sql 파일의 내용 실행
```

#### Redis 설치 및 실행

```bash
# macOS (Homebrew)
brew install redis
brew services start redis

# Redis 확인
redis-cli ping  # PONG 응답 확인
```

### 3. 환경 변수 설정

#### CoreService 설정

`CoreService/src/main/resources/application.properties`:

```properties
# MySQL 설정
spring.datasource.url=jdbc:mysql://localhost:3306/haru_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# Redis 설정
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

#### AssistService 설정 (선택사항)

`AssistService/src/main/resources/application.properties`에서 환경 변수 설정:

- `SMS_API_KEY`
- `NAVER_CLOUD_CHATBOT_SECRET_KEY`
- `NAVER_CLOUD_OCR_SECRET_KEY`
- `KAKAO_REST_API_KEY`

### 4. 프로젝트 실행

#### 방법 1: 자동 실행 스크립트 (권장) ⭐

```bash
# 모든 서비스 시작
./start.sh

# 모든 서비스 중지
./stop.sh
```

#### 방법 2: 수동 실행

**1. Redis 시작**
```bash
brew services start redis
```

**2. CoreService 실행**
```bash
cd CoreService
chmod +x gradlew
./gradlew bootRun
# 포트: 8081
```

**3. GateWay 실행** (새 터미널)
```bash
cd GateWay
chmod +x gradlew
./gradlew bootRun
# 포트: 8080
```

**4. 프론트엔드 실행** (새 터미널)
```bash
cd vite-react-teamsketch
npm install
npm run dev
# 포트: 3000
```

### 5. 접속 확인

- **프론트엔드**: http://localhost:3000
- **GateWay**: http://localhost:8080
- **CoreService**: http://localhost:8081
- **API 문서 (Swagger)**: http://localhost:8081/swagger-ui.html

## 📁 프로젝트 구조

```
Haru/
├── GateWay/              # API Gateway 서비스
│   ├── src/main/java/   # 라우팅 및 JWT 필터 설정
│   └── build.gradle
│
├── CoreService/          # 핵심 비즈니스 로직
│   ├── src/main/java/
│   │   ├── controller/  # REST API 컨트롤러
│   │   ├── service/     # 비즈니스 로직
│   │   ├── mapper/      # MyBatis 매퍼
│   │   └── model/       # 엔티티 모델
│   └── build.gradle
│
├── AssistService/        # 보조 서비스 (AI, OCR, SMS)
│   ├── src/main/java/
│   └── build.gradle
│
├── FastAPI/              # Python AI 서비스
│   ├── llamaServer.py   # LLaMA 챗봇 서버
│   └── requirements.txt
│
├── vite-react-teamsketch/ # 프론트엔드
│   ├── src/
│   │   ├── components/  # React 컴포넌트
│   │   ├── pages/       # 페이지 컴포넌트
│   │   ├── services/    # API 서비스
│   │   └── store/       # Redux 스토어
│   └── package.json
│
├── MySQL/                # DB 스키마 및 초기 데이터
│   ├── setup_database.sql
│   └── [테이블별 SQL 파일]
│
├── start.sh              # 서비스 실행 스크립트
├── stop.sh               # 서비스 중지 스크립트
└── README.md
```

## 📚 API 문서

Swagger UI를 통해 API 문서를 확인할 수 있습니다:

- **CoreService**: http://localhost:8081/swagger-ui.html
- **AssistService**: http://localhost:8082/swagger-ui.html

## 🔧 주요 기능 상세

### 인증 시스템
- 이메일/비밀번호 로그인
- JWT 토큰 기반 인증
- 소셜 로그인 (Google, Kakao, Naver)
- 비밀번호 변경, 계정 탈퇴

### 마켓플레이스
- 상품 등록 (구매/판매/요청)
- 카테고리 및 취미별 필터링
- 위치 기반 상품 검색
- 상품 이미지 업로드
- 거래 승인 시스템

### 실시간 채팅
- WebSocket/STOMP 기반 1:1 채팅
- Redis Pub/Sub를 통한 메시지 브로드캐스팅
- 이미지 전송 지원
- 읽음 확인 기능

### 게시판
- 취미별 전용 게시판
- 게시글 작성/수정/삭제
- 댓글 및 대댓글 시스템
- 신고 기능

### AI 챗봇
- LLaMA 기반 고객 지원 챗봇 "Luffy"
- 앱 사용법 안내
- FAQ 답변
- 피드백 수집

## 🐛 문제 해결

### 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인 및 종료
lsof -ti :8080 | xargs kill -9
lsof -ti :8081 | xargs kill -9
lsof -ti :3000 | xargs kill -9
```

### MySQL 연결 오류
- `application.properties`의 DB 설정 확인
- MySQL 서비스 실행 상태 확인
- 사용자 권한 확인

### Redis 연결 오류
```bash
# Redis 서비스 상태 확인
brew services list | grep redis

# Redis 재시작
brew services restart redis
```

## 📝 로그 확인

```bash
# CoreService 로그
tail -f logs/core-service.log

# GateWay 로그
tail -f logs/gateway.log

# 프론트엔드 로그
tail -f logs/frontend.log
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 개인 학습 및 포트폴리오 목적으로 제작되었습니다.

## 👥 팀

- **개발 기간**: 2025-02-06 ~ 2025-04-03 (팀 프로젝트)
- **개인 개발**: 2025-12-08 ~ (진행 중)

## 🔗 관련 링크

- [GitHub Repository](https://github.com/Lcjam/Haru)
- [API Documentation](http://localhost:8081/swagger-ui.html)

---

**Haru** - 취미를 루틴처럼, 하루에 하나씩 🌸

