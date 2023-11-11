package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserinfoDTO{
    private String nickname;
    private String sex;
    private String birthday;
    private String intro;
    @Email
    private String email;
    private String qq;
    private String profession;
    private String[] label;

}
