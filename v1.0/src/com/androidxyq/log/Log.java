package com.androidxyq.log;

import java.text.MessageFormat;

/**
 * 日志接口封装
 * <p/>
 * User: gongdewei
 * Date: 12-6-4
 * Time: 下午10:30
 */
public class Log {
    public static void debug(Object clazz, String message){
        android.util.Log.d(getTagName(clazz), message);
    }
    
    public static void debug(Object clazz, String message, Object... params){
        android.util.Log.d(getTagName(clazz), MessageFormat.format(message, params));
    }

    public static void debug(Object clazz, String message, Throwable throwable){
        android.util.Log.d(getTagName(clazz), message, throwable);
    }

    public static void info(Object clazz, String message){
        android.util.Log.i(getTagName(clazz), message);
    }

    public static void info(Object clazz, String message, Object... params){
        android.util.Log.i(getTagName(clazz), MessageFormat.format(message, params));
    }

    public static void info(Object clazz, String message, Throwable throwable){
        android.util.Log.i(getTagName(clazz), message, throwable);
    }

    public static void warn(Object clazz, String message){
        android.util.Log.w(getTagName(clazz), message);
    }
    public static void warn(Object clazz, String message, Object... params){
        android.util.Log.w(getTagName(clazz), MessageFormat.format(message, params));
    }

    public static void warn(Object clazz, String message, Throwable throwable){
        android.util.Log.w(getTagName(clazz), message, throwable);
    }

    public static void error(Object clazz, String message){
        android.util.Log.e(getTagName(clazz), message);
    }
    public static void error(Object clazz, String message, Object... params){
        android.util.Log.e(getTagName(clazz), MessageFormat.format(message, params));
    }

    public static void error(Object clazz, String message, Throwable throwable){
        android.util.Log.e(getTagName(clazz), message, throwable);
    }

    private static String getTagName(Object target){
        Class clazz = null;
        if(target instanceof Class){
            clazz = (Class) target;
        }else {
            clazz = target.getClass();
        }
        return clazz.getSimpleName();
    }
}
