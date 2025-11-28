package com.achobeta.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyPointsDTO {
    /**
     * 知识点id
     */
    private int id;
    /**
     * 知识点内容
     */
    private String keyPoints;
}
