package com.achobeta.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author chensongmin
 * @description 全局服务响应状态码枚举
 * @date 2024/11/11
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum GlobalServiceStatusCode {
    /* 成功, 默认200 */
    SYSTEM_SUCCESS(200, "操作成功"),

    //401, access-token 过期
    UNAUTHORIZED(401, "access-token 过期，请用 refresh-token 刷新"),

    /* 系统错误 负数 */
    SYSTEM_SERVICE_FAIL(-4396, "操作失败"),
    SYSTEM_SERVICE_ERROR(-500, "系统异常"),
    SYSTEM_TIME_OUT(-1, "请求超时"),

    /* 参数错误：1001～2000 */
    PARAM_NOT_VALID(1001, "参数无效"),
    PARAM_IS_BLANK(1002, "参数为空"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_NOT_COMPLETE(1004, "参数缺失"),
    PARAM_FAILED_VALIDATE(1005, "参数未通过验证"),

    REQUEST_NOT_VALID(1101, "请求无效"),

    /* 用户错误 2001-3000 */
    USER_NOT_LOGIN(2001, "用户未登录"),
    USER_ACCOUNT_EXPIRED(2002, "账号已过期"),
    USER_CREDENTIALS_ERROR(2003, "账号或密码错误"),
    USER_CREDENTIALS_EXPIRED(2004, "密码过期"),
    USER_ACCOUNT_DISABLE(2005, "账号不可用"),
    USER_ACCOUNT_LOCKED(2006, "账号被锁定"),
    USER_ACCOUNT_NOT_EXIST(2007, "账号不存在"),
    USER_ACCOUNT_ALREADY_EXIST(2008, "账号已存在"),
    USER_ACCOUNT_USE_BY_OTHERS(2009, "账号下线"),
    USER_ACCOUNT_REGISTER_ERROR(2010, "账号注册错误"),
    USER_EMAIL_FORMAT_ERROR(2011, "邮箱格式不正确"),
    USER_EMAIL_ALREADY_EXIST(2012, "邮箱已存在"),
    USER_EMAIL_NOT_EXIST(2013, "邮箱不存在,请查看邮箱是否有误"),
    USER_ID_IS_NULL(2014, "用户id为空"),
    USER_EMAIL_VERIFY_CODE_ERROR(2100, "验证码已过期，请重新获取"),

    USER_TYPE_EXCEPTION(2101, "用户类别异常"),

    USER_NO_PERMISSION(2403, "用户无权限"),
    USER_CAPTCHA_CODE_ERROR(2500, "验证码错误"),

    QUESTION_GENERATION_FAIL(10001, "题目生成失败,请稍后再试"),
    QUESTION_IS_EXPIRED(10002, "题目已过期或不存在" ),
    OCR_ERROR(10003, "OCR图片识别错误"),

    /**
     * 主页错误返回码
     */
    GET_STUDY_DYNAMIC_FAIL(10004, "获取学习动态失败,请稍后再试"),
    GET_OVERDUE_REVIEW_COUNT_FAIL(10005, "获取待复习题目数量失败,请稍后再试"),
    GET_TRICKY_KNOWLEDGE_POINT_FAIL(10006, "获取易错知识点失败,请稍后再试"),
    AI_RESPONSE_TIMEOUT(10007, "AI响应超时"),

    /**
     * 待复习题目错误返回码
     */
    GET_REVIEW_QUESTION_LIST_FAIL(10008, "获取待复习题目列表失败,请稍后再试"),
    DELETE_REVIEW_QUESTION_FAIL(10009, "删除待复习题目失败,请稍后再试"),
    GET_STATISTICS_FAIL(10010, "获取待复习题目统计信息失败,请稍后再试"),

    /**
     * 知识点脑图错误返回码
     */
    GET_KEY_POINTS_FAIL(10011, "获取中心知识点失败,请稍后再试"),
    GET_SON_KEY_POINTS_FAIL(10012, "获取子知识点失败,请稍后再试"),
    GET_KNOWLEDGE_POINT_DESC_FAIL(10013, "获取知识点描述失败,请稍后再试"),
    GET_RELATED_MESSAGES_FAIL(10014, "获取知识点相关信息失败,请稍后再试"),
    MARK_AS_MASTERED_FAIL(10015, "标记为已掌握失败,请稍后再试"),
    GET_RELATED_POINTS_FAIL(10016, "获取相关知识点失败,请稍后再试"),
    SAVE_OR_UPDATE_NOTE_FAIL(10017, "保存或更新笔记失败,请稍后再试"),
    RENAME_NODE_FAIL(10018, "重命名节点失败,请稍后再试"),
    SHOW_TOOLTIP_FAIL(10019, "显示提示失败,请稍后再试"),
    ADD_SON_POINT_FAIL(10020, "添加子知识点失败,请稍后再试"),
    /* 错因管理相关状态码 11001-11100 */
    MISTAKE_REASON_SUCCESS(11001, "错因操作成功"),
    MISTAKE_REASON_TOGGLE_SUCCESS(11002, "错因状态切换成功"),
    MISTAKE_REASON_UPDATE_SUCCESS(11003, "错因信息更新成功"),
    MISTAKE_REASON_GET_SUCCESS(11004, "错因信息获取成功"),
    STUDY_NOTE_SUBMIT_SUCCESS(11005, "错题笔记提交成功"),
    STUDY_NOTE_GET_SUCCESS(11006, "错题笔记获取成功"),


    MISTAKE_REASON_NOT_FOUND(11101, "未找到对应的错题记录"),
    MISTAKE_REASON_INVALID_PARAM(11102, "错因参数无效"),
    MISTAKE_REASON_UPDATE_FAILED(11103, "错因状态更新失败"),
    MISTAKE_REASON_TOGGLE_FAILED(11104, "错因状态切换失败"),
    STUDY_NOTE_UPDATE_FAILED(11105, "错题笔记更新失败"),
    MISTAKE_REASON_SYSTEM_ERROR(11106, "错因管理系统异常"),
    ;

    private Integer code;
    private String message;

}
