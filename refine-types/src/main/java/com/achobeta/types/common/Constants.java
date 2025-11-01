package com.achobeta.types.common;

public class Constants {

    public final static String SPLIT = ",";

    public final static String TRACE_ID = "traceId";

    // 验证码缓存键前缀
    public static final String CACHE_KEY_PREFIX = "user:verify:code:";

    public static final String SEND_RECORD_PREFIX = "user:verify:record:";

    // 验证码发送间隔
    public static final int SEND_INTERVAL_SECONDS = 60;

    // 用户token有效期：2天
    public static final int USER_TOKEN_EXPIRED_SECONDS = 60 * 60 * 24 * 2;

    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    public static final String USER_ID_KEY_PREFIX = "user:id:";
}
