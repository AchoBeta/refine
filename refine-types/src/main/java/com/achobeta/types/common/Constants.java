package com.achobeta.types.common;

public class Constants {

    public final static String SPLIT = ",";

    public final static String TRACE_ID = "traceId";

    // 验证码缓存键
    public static final String REDIS_EMAIL_KEY = "user:verify:code:";
    public static final String REDIS_EMAIL_RECORD_KEY = "user:verify:record:";

    // 验证码发送间隔 1分钟
    public static final int SEND_INTERVAL_MILLISECONDS = 60 * 1000;

    public static final String USER_REFRESH_TOKEN_KEY = "user:token:refresh:";

    // 密码正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

}
