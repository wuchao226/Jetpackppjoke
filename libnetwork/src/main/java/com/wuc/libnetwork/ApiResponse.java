package com.wuc.libnetwork;

/**
 * @author: wuchao
 * @date: 2020-01-26 23:43
 * @desciption: 请求实体类  基类
 */
public class ApiResponse<T> {
    public boolean success;
    public int status;
    public String message;
    public T body;
}
