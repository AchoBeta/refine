package com.achobeta.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author malog
 * @description 操作类型枚举
 * @date 2025/11/25
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ActionType {

    UPLOAD("upload", "上传"),
    REVIEW("review", "复习"),
    QA("qa", "问答"),
    MISTAKE("mistake", "错题"),
    STUDY("study", "学习")
    ;

    private String actionType;
    private String actionName;
    }
