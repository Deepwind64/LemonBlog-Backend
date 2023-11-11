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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户信息实体类")
@TableName("userinfo")
public class Userinfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "userinfo_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "userinfo_nickname")
    private String nickname;
    @TableField(value = "userinfo_sex")
    private String sex;
    @TableField(value = "userinfo_birthday")
    private String birthday;
    @TableField(value = "userinfo_intro")
    private String intro;
    @TableField(value = "userinfo_email")
    private String email;
    @TableField(value = "userinfo_qq")
    private String qq;
    @TableField("userinfo_profession")
    private String profession;
    @TableField("userinfo_label")
    private String label;
    @TableField("user_id")
    private Long userId;
    @TableField("userinfo_avatar_pic_id")
    private Long avatarId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}