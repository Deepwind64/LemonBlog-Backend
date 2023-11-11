package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginTicketVO {
    private Long userId;
    private String token;
    private Integer userAccountStatus;
    private Long expireTime;
}
