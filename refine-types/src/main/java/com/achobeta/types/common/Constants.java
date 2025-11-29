package com.achobeta.types.common;

public class Constants {

    public final static String SPLIT = ",";

    public final static String TRACE_ID = "traceId";

    // 验证码缓存键
    public static final String REDIS_EMAIL_KEY = "user:email:code:";
    public static final String REDIS_EMAIL_RECORD_KEY = "user:email:record:";


    public static final String QUESTION_GENERATION_ID_KEY = "question:generationId:";
    // 题目缓存最大时间: 1天
    public static final Long QUESTION_GENERATION_EXPIRED_SECONDS = 2 * 60 * 60 * 24 * 1000L;


    // 验证码发送间隔 1分钟
    public static final int SEND_INTERVAL_MILLISECONDS = 60 * 1000;

    public static final String USER_REFRESH_TOKEN_KEY = "user:token:refresh:";
    public static final String USER_TOKEN_ID_KEY = "user:token:id:";

    // 密码正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";



}
