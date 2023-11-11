package com.bluemsun.lemoncommon.context.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author deepwind
 */
@Data
@Builder
public class BaseUserDTO {
    private Long userId;
    private Boolean isAdmin;
    private String authorities;
}
