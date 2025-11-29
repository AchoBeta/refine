// com.achobeta.infrastructure.dao.po.MistakePO.java
package com.achobeta.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 错题数据库映射对象（与数据库表1:1对应）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MistakePO {

    private String userId;
    private String questionId;
    private String questionContent;
    private String subject;
    private String otherReason;
    private Integer knowledgePointId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime; // 领域层Entity无需关注的字段
}