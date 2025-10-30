package com.achobeta.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @Auth : Malog
 * @Desc :
 * @Time : 2025/10/30 17:41
 */
@Data
public class MistakeQuestion {

    /** 主键ID */
    private Long id;

    /** 逻辑外键：关联UserInformation表的user_id */
    private String userId;

    /** 题目内容 */
    private String questionContent;

    /** 所属学科 */
    private String subject;

    /** 是否粗心（0-否，1-是） */
    private int isCareless;

    /** 是否知识点不熟悉 */
    private int isUnfamiliar;

    /** 是否计算错误 */
    private int isCalculateErr;

    /** 是否时间不足 */
    private int isTimeShortage;

    /** 其他原因 */
    private String otherReason;

    /** 逻辑外键：关联knowledge_point表的knowledge_point_id */
    private String knowledgePointId;

    /** 学习笔记 */
    private String studyNote;

    /** 题目状态（0-未理解，1-已理解） */
    private int questionStatus;

    /** 错题添加日期（yyyy-mm-dd） */
    private Date createTime;

    /** 最后更新时间（含时分秒） */
    private Date updateTime;
}
