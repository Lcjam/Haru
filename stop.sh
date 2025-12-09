#!/bin/bash

# Haru 프로젝트 전체 서비스 중지 스크립트

echo "🛑 Haru 프로젝트 서비스 중지 중..."

cd "$(dirname "$0")"

# PID 파일에서 프로세스 종료
if [ -f logs/core-service.pid ]; then
    CORE_PID=$(cat logs/core-service.pid)
    if ps -p $CORE_PID > /dev/null 2>&1; then
        kill -9 $CORE_PID 2>/dev/null
        echo "✅ CoreService 종료됨 (PID: $CORE_PID)"
    fi
    rm logs/core-service.pid
fi

if [ -f logs/gateway.pid ]; then
    GATEWAY_PID=$(cat logs/gateway.pid)
    if ps -p $GATEWAY_PID > /dev/null 2>&1; then
        kill -9 $GATEWAY_PID 2>/dev/null
        echo "✅ GateWay 종료됨 (PID: $GATEWAY_PID)"
    fi
    rm logs/gateway.pid
fi

if [ -f logs/frontend.pid ]; then
    FRONTEND_PID=$(cat logs/frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        kill -9 $FRONTEND_PID 2>/dev/null
        echo "✅ 프론트엔드 종료됨 (PID: $FRONTEND_PID)"
    fi
    rm logs/frontend.pid
fi

# 포트로 프로세스 확인 및 종료 (안전장치)
lsof -ti :8080 | xargs kill -9 2>/dev/null
lsof -ti :8081 | xargs kill -9 2>/dev/null
lsof -ti :3000 | xargs kill -9 2>/dev/null

echo "✨ 모든 서비스가 중지되었습니다."

