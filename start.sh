#!/bin/bash

# Haru 프로젝트 전체 서비스 실행 스크립트

echo "🚀 Haru 프로젝트 서비스 시작 중..."

# 프로젝트 루트 디렉토리로 이동
cd "$(dirname "$0")"

# Redis 확인 및 시작
echo "📦 Redis 확인 중..."
if ! redis-cli ping > /dev/null 2>&1; then
    echo "⚠️  Redis가 실행되지 않았습니다. Redis를 시작합니다..."
    brew services start redis 2>/dev/null || redis-server &
    sleep 2
    if redis-cli ping > /dev/null 2>&1; then
        echo "✅ Redis 시작 완료"
    else
        echo "❌ Redis 시작 실패. 수동으로 시작해주세요: brew services start redis"
    fi
else
    echo "✅ Redis 실행 중"
fi

# 기존 프로세스 종료 (선택사항)
echo "🧹 기존 프로세스 확인 중..."
lsof -ti :8080 | xargs kill -9 2>/dev/null
lsof -ti :8081 | xargs kill -9 2>/dev/null
lsof -ti :3000 | xargs kill -9 2>/dev/null
sleep 1

# CoreService 실행
echo "🔧 CoreService 시작 중 (포트 8081)..."
cd CoreService
chmod +x gradlew
./gradlew bootRun > ../logs/core-service.log 2>&1 &
CORE_PID=$!
cd ..
echo "✅ CoreService 시작됨 (PID: $CORE_PID)"

# GateWay 실행
echo "🚪 GateWay 시작 중 (포트 8080)..."
cd GateWay
chmod +x gradlew
./gradlew bootRun > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..
echo "✅ GateWay 시작됨 (PID: $GATEWAY_PID)"

# 프론트엔드 실행
echo "🎨 프론트엔드 시작 중 (포트 3000)..."
cd vite-react-teamsketch
if [ ! -d "node_modules" ]; then
    echo "📦 npm 패키지 설치 중..."
    npm install
fi
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
echo "✅ 프론트엔드 시작됨 (PID: $FRONTEND_PID)"

# PID 파일 저장
mkdir -p logs
echo $CORE_PID > logs/core-service.pid
echo $GATEWAY_PID > logs/gateway.pid
echo $FRONTEND_PID > logs/frontend.pid

echo ""
echo "✨ 모든 서비스가 시작되었습니다!"
echo ""
echo "📊 서비스 상태:"
echo "   - Redis: localhost:6379"
echo "   - CoreService: http://localhost:8081"
echo "   - GateWay: http://localhost:8080"
echo "   - 프론트엔드: http://localhost:3000"
echo ""
echo "📝 로그 확인:"
echo "   - CoreService: tail -f logs/core-service.log"
echo "   - GateWay: tail -f logs/gateway.log"
echo "   - 프론트엔드: tail -f logs/frontend.log"
echo ""
echo "🛑 서비스 중지: ./stop.sh"
echo ""

# 서비스 시작 대기
sleep 5

# 서비스 상태 확인
echo "🔍 서비스 상태 확인 중..."
if curl -s http://localhost:8081 > /dev/null 2>&1; then
    echo "✅ CoreService: 실행 중"
else
    echo "⏳ CoreService: 시작 중..."
fi

if curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo "✅ GateWay: 실행 중"
else
    echo "⏳ GateWay: 시작 중..."
fi

if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ 프론트엔드: 실행 중"
else
    echo "⏳ 프론트엔드: 시작 중..."
fi

echo ""
echo "🌐 브라우저에서 http://localhost:3000 을 열어주세요!"

