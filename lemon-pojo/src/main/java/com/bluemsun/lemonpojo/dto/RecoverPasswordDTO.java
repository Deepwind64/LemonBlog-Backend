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
public class RecoverPasswordDTO {
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @NotBlank(message = "新密码不能为空")
    private String password;
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
