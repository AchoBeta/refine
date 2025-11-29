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
 * @Desc : 更新其他原因文本请求DTO
 * @Time : 2025/11/10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOtherReasonRequestDTO implements Serializable {

    @NotBlank(message = "题目ID不能为空")
    @FieldDesc(name = "题目ID")
    private String questionId;

    @NotBlank(message = "其他原因描述不能为空")
    @FieldDesc(name = "其他原因描述")
    private String otherReasonText;
}