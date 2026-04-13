package com.achobeta.api.dto;

import com.achobeta.types.annotation.FieldDesc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Auth : Malog
 * @Desc : 简化错因切换请求DTO
 * @Time : 2025/11/10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MistakeReasonToggleRequestDTO implements Serializable {

    @NotBlank(message = "题目ID不能为空")
    @FieldDesc(name = "题目ID")
    private String questionId;

    @NotBlank(message = "错因参数名不能为空")
    @FieldDesc(name = "错因参数名")
    private String reasonName;
}