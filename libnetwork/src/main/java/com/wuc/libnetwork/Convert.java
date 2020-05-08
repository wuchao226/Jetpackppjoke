package com.wuc.libnetwork;

import java.lang.reflect.Type;

/**
 * @author: wuchao
 * @date: 2020-01-28 16:41
 * @desciption:
 */
public interface Convert<T> {
    T convert(String response, Type type);

    T convert(String response, Class claz);
}
