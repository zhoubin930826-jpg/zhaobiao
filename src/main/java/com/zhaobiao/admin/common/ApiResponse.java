package com.zhaobiao.admin.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "统一响应结果。业务接口通常以 HTTP 200 返回，调用方需要同时判断 code；code=0 表示成功，非 0 表示业务失败或参数失败。")
public class ApiResponse<T> {

    @Schema(description = "业务状态码，0 表示成功；常见失败码包括 400 参数/业务校验失败、403 无权限或业务禁止、404 资源不存在、410 接口停用、500 系统异常", example = "0")
    private int code;

    @Schema(description = "响应消息", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应时间")
    private LocalDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
