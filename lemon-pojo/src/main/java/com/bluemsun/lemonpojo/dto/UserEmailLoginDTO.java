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
public class UserEmailLoginDTO{
    @NotBlank(message = "邮箱不允许为空")
    private String email;
    @NotBlank(message = "密码不允许为空")
    private String password;
    @NotBlank(message = "验证码不允许为空")
    private String verifyCode;

}
