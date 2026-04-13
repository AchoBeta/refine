package com.achobeta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auth : Malog
 * @Desc : 错题笔记请求DTO
 * @Time : 2025/11/10
 */
@Data
public class StudyNoteRequestDTO implements Serializable {

    /**
     * 题目ID
     */
    @NotBlank(message = "题目ID不能为空")
    private String questionId;

    /**
     * 学习笔记内容
     */
    @NotBlank(message = "笔记内容不能为空")
    @Size(max = 2000, message = "笔记内容不能超过2000字符")
    private String studyNote;
}