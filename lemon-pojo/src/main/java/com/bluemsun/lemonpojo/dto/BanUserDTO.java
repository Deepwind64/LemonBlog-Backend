package com.bluemsun.lemonpojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanUserDTO {
    private Long userId;
    private Integer time;
    private String reason;
}
