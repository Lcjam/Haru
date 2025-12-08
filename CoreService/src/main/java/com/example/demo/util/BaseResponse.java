// API 응답 구조 통일을 위한 공통 클래스
package com.example.demo.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private String status;  // 상태 필드 (success / error)
    private String message; // 메시지 필드
    private T data;
    private String code;    // HTTP 상태 코드 (선택적)

    // 기본 생성자 (성공 응답)
    public BaseResponse(T data) {
        this.status = "success";
        this.message = "요청이 성공적으로 처리되었습니다.";
        this.data = data;
        this.code = "200";
    }

    // 커스텀 메시지를 포함한 성공 응답
    public BaseResponse(T data, String message) {
        this.status = "success";
        this.message = message;
        this.data = data;
        this.code = "200";
    }

    // 커스텀 메시지와 코드를 포함한 성공 응답
    public BaseResponse(T data, String message, String code) {
        this.status = "success";
        this.message = message;
        this.data = data;
        this.code = code;
    }

    // 정적 성공 응답 메서드 추가 (data만)
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data, "요청이 성공적으로 처리되었습니다.", "200");
    }

    // 정적 성공 응답 메서드 추가 (data + custom message)
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(data, message, "200");
    }

    // 정적 성공 응답 메서드 추가 (data + custom message + code)
    public static <T> BaseResponse<T> success(T data, String message, String code) {
        return new BaseResponse<>(data, message, code);
    }

    // 에러 응답 (message만)
    public static <T> BaseResponse<T> error(String message) {
        BaseResponse<T> response = new BaseResponse<>(null);
        response.status = "error";
        response.message = message;
        response.code = "400";
        return response;
    }

    // 에러 응답 (message + code)
    public static <T> BaseResponse<T> error(String message, String code) {
        BaseResponse<T> response = new BaseResponse<>(null);
        response.status = "error";
        response.message = message;
        response.code = code;
        return response;
    }

    // 에러 응답 (data + code) - ApiResponse 호환성을 위해
    public static <T> BaseResponse<T> error(T data, String code) {
        BaseResponse<T> response = new BaseResponse<>(data);
        response.status = "error";
        response.message = data != null && data instanceof String ? (String) data : "오류가 발생했습니다.";
        response.code = code;
        return response;
    }

    // Getter 추가
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getCode() {
        return code;
    }
}
