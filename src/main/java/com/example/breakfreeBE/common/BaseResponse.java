package com.example.breakfreeBE.common;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {
    private MetaResponse meta;
    private T data;

    public BaseResponse(MetaResponse meta, T data) {
        this.meta = meta;
        this.data = data;
    }

    public MetaResponse getMeta() {
        return meta;
    }

    public void setMeta(MetaResponse meta) {
        this.meta = meta;
    }

    public T getData() {
        return data;
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(new MetaResponse(true, message), data);
    }

    public void setData(T data) {
        this.data = data;
    }
}
