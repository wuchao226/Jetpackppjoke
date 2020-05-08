package com.wuc.libnetwork;

/**
 * @author: wuchao
 * @date: 2020-01-28 00:33
 * @desciption: Get 请求
 */
public class GetRequest<T> extends Request<T, GetRequest> {
    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        //get 请求把参数拼接在 url后面
        String url = UrlCreator.createUrlFromParams(mUrl, mParams);
        okhttp3.Request request = builder.get().url(url).build();
        return request;
    }
}
