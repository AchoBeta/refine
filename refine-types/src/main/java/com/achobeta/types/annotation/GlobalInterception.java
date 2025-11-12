package com.achobeta.types.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterception {

    /**
     * 是否检查登录
     *
     * @return true 检查登录，false 不检查登录
     */
    boolean checkLogin() default true;

}
