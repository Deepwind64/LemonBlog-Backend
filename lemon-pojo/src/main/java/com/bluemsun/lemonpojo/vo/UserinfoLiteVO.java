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
public class UserinfoLiteVO {
    private Long userId;
    private String nickname;
    private String intro;
    private String avatar;
}
