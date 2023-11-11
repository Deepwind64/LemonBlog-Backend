package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {
    private int id;
    private String email;
    private String username;
    private String nickname;
    private String avatar;
    private Long createTime;
}
