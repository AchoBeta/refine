package com.achobeta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Auth : Malog
 * @Desc : AI解题请求DTO
 * @Time : 2025/11/7 11:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSolveRequestDTO implements Serializable {

    /**
     * 问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 10000, message = "问题内容长度不能超过10000字符")
    private String question;

}
