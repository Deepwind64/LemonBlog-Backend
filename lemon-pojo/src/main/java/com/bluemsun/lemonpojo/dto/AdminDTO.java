package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    @NotBlank(message = "用户名不允许为空")
    private String name;
    @NotBlank(message = "密码不允许为空")
    private String password;

}