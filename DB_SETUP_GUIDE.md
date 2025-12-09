# Haru 프로젝트 DB 설정 가이드

## 📋 목차
1. [필수 사전 준비](#1-필수-사전-준비)
2. [MySQL 설치 확인](#2-mysql-설치-확인)
3. [데이터베이스 생성](#3-데이터베이스-생성)
4. [Redis 설정](#4-redis-설정)
5. [application.properties 확인](#5-applicationproperties-확인)
6. [테스트 실행](#6-테스트-실행)

---

## 1. 필수 사전 준비

### 필요한 소프트웨어
- **MySQL 8.0 이상**
- **Redis** (채팅 기능용)
- **Java 17 이상** (Spring Boot 실행용)
- **Node.js 18 이상** (프론트엔드 실행용)

---

## 2. MySQL 설치 확인

### macOS (Homebrew 사용)
```bash
# MySQL 설치 확인
mysql --version

# MySQL이 없으면 설치
brew install mysql

# MySQL 서비스 시작
brew services start mysql
```

### MySQL 접속 테스트
```bash
# root 계정으로 접속
mysql -u root -p
# 비밀번호 입력: (본인의 MySQL root 비밀번호)
```

---

## 3. 데이터베이스 생성

### 방법 1: 통합 SQL 스크립트 사용 (권장)

```bash
# MySQL 접속
mysql -u root -p

# 스크립트 실행
source /Users/jamie/Desktop/Project/Haru/MySQL/setup_database.sql
```

또는

```bash
# 터미널에서 직접 실행
mysql -u root -p < /Users/jamie/Desktop/Project/Haru/MySQL/setup_database.sql
```

### 방법 2: 수동 생성

```sql
-- MySQL 접속 후 실행
CREATE DATABASE haru_db;
USE haru_db;

-- 사용자 생성
CREATE USER 'user'@'localhost' IDENTIFIED BY 'user';
CREATE USER 'user'@'%' IDENTIFIED BY 'user';
GRANT ALL PRIVILEGES ON haru_db.* TO 'user'@'localhost';
GRANT ALL PRIVILEGES ON haru_db.* TO 'user'@'%';
FLUSH PRIVILEGES;
```

### 테이블 생성 확인
```sql
USE haru_db;
SHOW TABLES;
```

예상 결과:
- Users
- categories
- hobbies
- Products
- chatrooms
- messages
- boards
- Posts
- Comments
- Transactions
- 등등...

---

## 4. Redis 설정

### Redis 설치 (macOS)
```bash
# Redis 설치
brew install redis

# Redis 서비스 시작
brew services start redis

# Redis 접속 테스트
redis-cli ping
# 응답: PONG
```

### Redis 설정 확인
- **Host**: localhost
- **Port**: 6379
- **Password**: (없음)

---

## 5. application.properties 확인

### CoreService 설정
**파일 위치**: `CoreService/src/main/resources/application.properties`

**주요 설정 확인**:
```properties
# MySQL 설정
spring.datasource.url=jdbc:mysql://localhost:3306/haru_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD  # 본인의 MySQL 비밀번호로 변경

# Redis 설정
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
```

### AssistService 설정
**파일 위치**: `AssistService/src/main/resources/application.properties`

**주요 설정 확인**:
```properties
# MySQL 설정 (원격 서버 사용 시)
spring.datasource.url=jdbc:mysql://YOUR_DB_HOST:3306/haru_db  # 원격 서버 주소로 변경
spring.datasource.username=YOUR_DB_USERNAME  # DB 사용자명으로 변경
spring.datasource.password=YOUR_DB_PASSWORD  # DB 비밀번호로 변경
```

**⚠️ 주의**: AssistService는 원격 서버(`sunbee.world`)를 사용하고 있습니다. 
로컬 개발 시에는 `localhost`로 변경해야 할 수 있습니다.

### GateWay 설정
**파일 위치**: `GateWay/src/main/resources/application.properties`

GateWay는 DB 연결이 없으므로 확인 불필요.

---

## 6. 테스트 실행

### 1단계: MySQL 연결 테스트
```bash
# MySQL 접속 테스트
mysql -u root -p -e "USE haru_db; SHOW TABLES;"
```

### 2단계: Redis 연결 테스트
```bash
# Redis 접속 테스트
redis-cli ping
```

### 3단계: 백엔드 서비스 실행 순서

**중요**: 서비스 실행 순서를 반드시 지켜야 합니다!

1. **Redis 시작** (먼저!)
   ```bash
   brew services start redis
   # 또는
   redis-server
   ```

2. **CoreService 실행**
   ```bash
   cd CoreService
   ./gradlew bootRun
   # 또는
   ./gradlew build && java -jar build/libs/*.jar
   ```
   - 포트: `8081`
   - 확인: http://localhost:8081/actuator/health

3. **AssistService 실행** (선택사항)
   ```bash
   cd AssistService
   ./gradlew bootRun
   ```
   - 포트: `8082`

4. **GateWay 실행**
   ```bash
   cd GateWay
   ./gradlew bootRun
   ```
   - 포트: `8080`
   - 확인: http://localhost:8080

### 4단계: 프론트엔드 실행
```bash
cd vite-react-teamsketch
npm install
npm run dev
```
- 포트: `3000` (기본값)
- 확인: http://localhost:3000

---

## 🔧 문제 해결

### MySQL 연결 오류
```
Error: Access denied for user 'root'@'localhost'
```
**해결**:
1. MySQL 비밀번호 확인
2. `application.properties`의 비밀번호 확인
3. 사용자 권한 확인:
   ```sql
   SELECT host, user FROM mysql.user;
   ```

### Redis 연결 오류
```
Error: Connection refused
```
**해결**:
```bash
# Redis 서비스 상태 확인
brew services list | grep redis

# Redis 재시작
brew services restart redis
```

### 포트 충돌
```
Error: Port 8080 already in use
```
**해결**:
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 [PID]
```

### 테이블이 생성되지 않음
**해결**:
1. SQL 스크립트 오류 확인
2. 외래키 제약조건 확인
3. 테이블 생성 순서 확인 (외래키 의존성)

---

## 📝 추가 설정 (선택사항)

### 초기 데이터 삽입
```sql
USE haru_db;

-- 샘플 카테고리
INSERT INTO categories (category_name) VALUES 
('운동'), ('문화'), ('취미'), ('기타');

-- 샘플 취미
INSERT INTO hobbies (hobby_name) VALUES 
('축구'), ('독서'), ('요리'), ('사진');
```

### 환경 변수 설정 (AssistService)
AssistService는 다음 환경 변수가 필요합니다:
- `SMS_API_KEY`
- `SMS_API_SECRET`
- `SMS_API_URL`
- `NAVER_CLOUD_CHATBOT_SECRET_KEY`
- `NAVER_CLOUD_CHATBOT_API_URL`
- `NAVER_CLOUD_OCR_SECRET_KEY`
- `NAVER_CLOUD_OCR_API_URL`
- `KAKAO_REST_API_KEY`

---

## ✅ 체크리스트

실행 전 확인사항:
- [ ] MySQL 설치 및 실행 중
- [ ] `haru_db` 데이터베이스 생성 완료
- [ ] 모든 테이블 생성 완료
- [ ] Redis 설치 및 실행 중
- [ ] `application.properties` 설정 확인
- [ ] 포트 충돌 없음 (8080, 8081, 8082, 3000)

---

## 🚀 다음 단계

DB 설정이 완료되면:
1. 백엔드 서비스 실행
2. 프론트엔드 실행
3. API 테스트
4. 오류 로그 확인

문제가 발생하면 로그를 확인하세요:
- CoreService: `CoreService/logs/` 또는 콘솔 출력
- GateWay: `GateWay/logs/` 또는 콘솔 출력

