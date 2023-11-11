package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserinfoVO {
    private Long id;
    private String nickname;
    private String sex;
    private String birthday;
    private String intro;
    private String email;
    private String qq;
    private String profession;
    private String[] label;
}
