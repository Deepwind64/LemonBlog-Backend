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
@ApiModel(description = "图片文件类")
@TableName("picture")
public class Picture implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "picture_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "picture_name")
    private String name;
    @TableField(value = "picture_type")
    private String type;
    @TableField(value = "picture_hash")
    private String hash;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
