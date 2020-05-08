package com.wuc.libnavannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author: wuchao
 * @date: 2020-01-18 22:57
 * @desciption:
 */
@Target(ElementType.TYPE)
public @interface ActivityDestination {
    String pageUrl();

    /*
     登录标志，有些页面需要登录后才能进入
     */
    boolean needLogin() default false;

    /*
        startDestination 是否把当前页面作为开始页面
     */
    boolean asStarter() default false;
}
