package com.whu.onlinechat.common;

public record ApiResult<T>(int code, String message, T data) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(0, "success", data);
    }

    public static ApiResult<Void> success() {
        return new ApiResult<>(0, "success", null);
    }

    public static ApiResult<Void> error(String message) {
        return new ApiResult<>(1, message, null);
    }
}

