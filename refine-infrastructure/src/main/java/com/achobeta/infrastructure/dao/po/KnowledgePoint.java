package com.achobeta.infrastructure.dao.po;

import lombok.Data;

/**
 * @Auth : Malog
 * @Desc :
 * @Time : 2025/10/30 17:36
 */
@Data
public class KnowledgePoint {

    /** 主键ID */
    private Long id;

    /** 知识点唯一标识ID */
    private String knowledgePointId;

    /** 父知识点ID（顶级知识点为NULL） */
    private String parentKnowledgePointId;

    /** 子知识点ID（无下级为NULL） */
    private String sonKnowledgePointId;

    /** 知识点详细描述 */
    private String knowledgeDesc;

}
