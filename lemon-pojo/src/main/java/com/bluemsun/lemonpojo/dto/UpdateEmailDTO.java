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
public class UpdateEmailDTO {
    @NotBlank(message = "源邮箱不能为空")
    private String originEmail;
    @NotBlank(message = "新邮箱不能为空")
    private String newEmail;
    @NotBlank(message = "密码不能为空")
    private String password;
}
