package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    @NotBlank(message = "邮箱不允许为空")
    private String email;
    @NotBlank(message = "密码不允许为空")
    private String password;
    @NotBlank(message = "用户名不允许为空")
    private String name;
}
