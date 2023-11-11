package com.bluemsun.lemonpojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
/**
 * @author deepwind
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户账户实体类")
@TableName("user")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "user_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "user_email")
    private String email;
    @TableField(value = "user_password")
    private String password;
    @TableField(value = "user_name")
    private String name;
    @TableField(value = "user_login_status")
    private int loginStatus;
    @TableField(value = "user_account_status")
    private int accountStatus;
    @TableField("user_last_login_time")
    private Date lastLoginTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
