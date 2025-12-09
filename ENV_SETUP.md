# 환경 변수 설정 가이드

## 📋 개요

Haru 프로젝트는 보안을 위해 민감한 정보(비밀번호, API 키 등)를 환경 변수로 관리합니다.

## 🚀 빠른 시작

### 1. 환경 변수 파일 생성

```bash
# 프로젝트 루트에서
cp env.example .env
```

### 2. .env 파일 수정

`.env` 파일을 열어서 실제 값으로 변경하세요:

```bash
# 예시
DB_PASSWORD=your_actual_password
JWT_SECRET=your_actual_jwt_secret
```

### 3. 서비스 실행

환경 변수는 자동으로 로드됩니다:

```bash
./start.sh
```

## 📝 환경 변수 목록

### 필수 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DB_HOST` | MySQL 호스트 | `localhost` |
| `DB_PORT` | MySQL 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `haru_db` |
| `DB_USERNAME` | MySQL 사용자명 | `root` |
| `DB_PASSWORD` | MySQL 비밀번호 | (필수) |
| `JWT_SECRET` | JWT 토큰 시크릿 키 | (권장) |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |

### 선택적 환경 변수

#### AssistService 전용
- `SMS_API_KEY`
- `SMS_API_SECRET`
- `SMS_API_URL`
- `NAVER_CLOUD_CHATBOT_SECRET_KEY`
- `NAVER_CLOUD_CHATBOT_API_URL`
- `NAVER_CLOUD_OCR_SECRET_KEY`
- `NAVER_CLOUD_OCR_API_URL`
- `KAKAO_REST_API_KEY`

#### OAuth2 (소셜 로그인)
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`

## 🔧 사용 방법

### 방법 1: .env 파일 사용 (권장)

프로젝트 루트에 `.env` 파일을 생성하면 `spring-dotenv`가 자동으로 로드합니다.

```bash
# .env 파일 생성
cp env.example .env

# .env 파일 편집
nano .env  # 또는 원하는 에디터 사용
```

### 방법 2: 시스템 환경 변수

시스템 환경 변수로 설정할 수도 있습니다:

```bash
# macOS/Linux
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret

# Windows (PowerShell)
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your_secret"
```

### 방법 3: IDE에서 설정

IntelliJ IDEA나 VS Code에서 실행 시 환경 변수를 설정할 수 있습니다.

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Environment variables에 추가

**VS Code:**
`.vscode/launch.json`에 환경 변수 추가

## ⚠️ 주의사항

1. **`.env` 파일은 절대 Git에 커밋하지 마세요!**
   - `.gitignore`에 이미 추가되어 있습니다.
   - 실제 비밀번호가 포함된 파일은 공유하지 마세요.

2. **기본값 사용 시**
   - 환경 변수가 없으면 `application.properties`의 기본값이 사용됩니다.
   - 하지만 비밀번호는 반드시 환경 변수로 설정하는 것을 권장합니다.

3. **프로덕션 환경**
   - 프로덕션에서는 환경 변수나 시크릿 관리 시스템을 사용하세요.
   - Docker를 사용하는 경우 `docker-compose.yml`에서 환경 변수를 설정할 수 있습니다.

## 🔍 환경 변수 확인

실행 중인 서비스의 환경 변수를 확인하려면:

```bash
# CoreService 로그 확인
tail -f logs/core-service.log | grep -i "datasource\|redis\|jwt"
```

## 📚 참고

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [spring-dotenv Documentation](https://github.com/paulschwarz/spring-dotenv)

