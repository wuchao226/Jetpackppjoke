package com.wuc.libcommon.global;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: wuchao
 * @date: 2020-01-22 19:41
 * @desciption: 这种方式获取全局的Application 是一种拓展思路。
 * <p>
 * 对于组件化项目,不可能把项目实际的Application下沉到Base,而且各个module也不需要知道Application真实名字
 * <p>
 * 这种一次反射就能获取全局Application对象的方式相比于在Application#OnCreate保存一份的方式显示更加通用了
 */
public class AppGlobals {

    private static Application sApplication;

    public static Application getApplication() {
        if (sApplication == null) {
            try {
                //通过反射获取Application
                @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
                Method method = Class.forName("android.app.ActivityThread").
                        getDeclaredMethod("currentApplication");
                sApplication = (Application) method.invoke(null, new Object[]{});
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }
}
