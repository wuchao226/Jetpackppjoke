package com.wuc.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.wuc.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author: wuchao
 * @date: 2020-01-26 23:49
 * @desciption: param <T> 实体类型
 * param <R> Request 的子类
 */
public abstract class Request<T, R extends Request> implements Cloneable {

    /**
     * 仅仅只访问本地缓存，即便本地缓存不存在，也不会发起网络请求
     */
    public static final int CACHE_ONLY = 1;
    /**
     * 先访问缓存，同时发起网络的请求，成功后缓存到本地
     */
    public static final int CACHE_FIRST = 2;
    /**
     * 仅仅只访问服务器，不存任何存储
     */
    public static final int NET_ONLY = 3;
    /**
     * 先访问网络，成功后缓存到本地
     */
    public static final int NET_CACHE = 4;
    /**
     * 请求Url
     */
    protected String mUrl;
    /**
     * 存储添加的请求参数param
     */
    protected HashMap<String, Object> mParams = new HashMap<>();
    /**
     * 存储请求头header
     */
    private HashMap<String, String> mHeaders = new HashMap<>();
    private String cacheKey;
    private Type mType;
    private Class mClaz;
    private int mCacheStrategy = NET_ONLY;

    public Request(String url) {
        //url类型 user/list
        mUrl = url;
    }

    /**
     * 添加请求头参数到mHeaders中
     */
    public R addHeader(String key, String value) {
        mHeaders.put(key, value);
        return (R) this;
    }

    /**
     * 添加请求参数
     */
    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }
        //int byte char short long double float boolean 和他们的包装类型，但是除了 String.class 所以要额外判断
        try {
            if (value.getClass() == String.class) {
                mParams.put(key, value);
            } else {
                //反射获取Object的基本类型 TYPE:代表原始类型的实例
                Field field = value.getClass().getField("TYPE");
                Class claz = (Class) field.get(null);
                //如果claz是基本类型
                if (claz != null && claz.isPrimitive()) {
                    mParams.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    /**
     * 缓存策略
     */
    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    /**
     * 缓存key
     */
    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

    public R responseType(Class claz) {
        mType = claz;
        return (R) this;
    }

    /**
     * 同步execute
     *
     * @return ApiResponse
     */
    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法,response 返回值 类型必须设置");
        }
        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }
        if (mCacheStrategy != CACHE_ONLY) {
            ApiResponse<T> result = null;
            try {
                Response response = getCall().execute();
                result = parseResponse(response, null);
            } catch (IOException e) {
                e.printStackTrace();
                if (result == null) {
                    result = new ApiResponse<>();
                    result.message = e.getMessage();
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 读取缓存
     *
     * @return
     */
    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    /**
     * 获取请求Call
     */
    public Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.OK_HTTP_CLIENT.newCall(request);
        return call;
    }

    /**
     * 解析Java bean 对象
     *
     * @param response response
     * @param callback callback
     * @return ApiResponse
     */
    private ApiResponse<T> parseResponse(Response response, JsonCallback callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    //https://blog.csdn.net/harvic880925/article/details/50085595
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = null;
                    if (type != null) {
                        argument = type.getActualTypeArguments()[0];
                    }
                    result.body = (T) convert.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) convert.convert(content, mType);
                } else if (mClaz != null) {
                    result.body = (T) convert.convert(content, mClaz);
                } else {
                    Log.e("request", "parseResponse: 无法解析 ");
                }
            } else {
                message = content;
            }
        } catch (IOException e) {
            e.printStackTrace();
            message = e.getMessage();
            success = false;
            status = 0;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        if (mCacheStrategy != NET_ONLY && result.success && result.body != null
                && result.body instanceof Serializable) {
            saveCache(result.body);
        }
        return result;
    }

    /**
     * 生成缓存 cacheKey
     *
     * @return cacheKey
     */
    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, mParams);
        return cacheKey;
    }

    /**
     * 添加请求头参数到okhttp3中
     */
    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 生成Request 请求
     *
     * @param builder builder
     * @return Request
     */
    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    /**
     * 缓存 数据
     *
     * @param body body
     */
    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        CacheManager.save(key, body);
    }

    /**
     * 异步execute
     *
     * @param callback callback
     */
    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback callback) {
        if (mCacheStrategy != NET_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null && response.body != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> result = new ApiResponse<>();
                    result.message = e.getMessage();
                    callback.onError(result);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> result = parseResponse(response, callback);
                    if (!result.success) {
                        callback.onError(result);
                    } else {
                        callback.onSuccess(result);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request) super.clone();
    }

    /**
     * cache 类型
     */
    @IntDef({CACHE_ONLY, CACHE_FIRST, NET_CACHE, NET_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CacheStrategy {

    }

}
