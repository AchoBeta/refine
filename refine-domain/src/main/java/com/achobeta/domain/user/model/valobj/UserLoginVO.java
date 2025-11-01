package com.achobeta.domain.user.model.valobj;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVO {

    private String userId;
    private String userName;
    private String token;


}
