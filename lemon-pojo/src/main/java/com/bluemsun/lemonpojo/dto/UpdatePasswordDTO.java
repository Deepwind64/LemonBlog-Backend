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
public class UpdatePasswordDTO {
    @NotBlank(message = "源密码不能为空")
    private String originPassword;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
