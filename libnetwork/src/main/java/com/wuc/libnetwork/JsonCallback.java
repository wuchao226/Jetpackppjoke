package com.wuc.libnetwork;

/**
 * @author: wuchao
 * @date: 2020-01-26 23:43
 * @desciption:
 */
public abstract class JsonCallback<T> {
    public void onSuccess(ApiResponse<T> response) {
    }

    public void onError(ApiResponse<T> response) {
    }

    public void onCacheSuccess(ApiResponse<T> response) {
    }

}
