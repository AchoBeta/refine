package com.achobeta.infrastructure.dao.po;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Auth : Malog
 * @Desc :
 * @Time : 2025/10/30 17:42
 */
@Data
public class UserData {

    /** 主键ID */
    private Long id;

    /** 逻辑外键：关联UserInformation表的user_id（所属用户） */
    private String userId;

    /** 累计错题数量 */
    private int questionsNum;

    /** 复习巩固率（百分比） */
    private BigDecimal reviewRate;

    /** 易错知识点 */
    private Integer hardQuestions;

    /** 累计学习时长（小时） */
    private int studyTime;

}
