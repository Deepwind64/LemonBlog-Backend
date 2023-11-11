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
@ApiModel(description = "文件类")
@TableName("file")
public class MyFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "file_id",type = IdType.AUTO)
    private Integer id;
    @TableField(value = "file_origin_name")
    private String name;
    @TableField(value = "file_uid")
    private String uid;
    @TableField(value = "file_type")
    private String type;
    @TableField(value = "file_hash")
    private String hash;
    @TableField(value = "blog_id")
    private Integer blogId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}