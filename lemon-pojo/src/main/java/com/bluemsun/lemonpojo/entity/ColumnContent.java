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
@ApiModel(description = "博客专栏信息实体类")
@TableName("blog_mto_column")
public class ColumnContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "blog_mto_column_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "blog_mto_column_column_id")
    private Integer columnId;
    @TableField(value = "blog_mto_column_blog_id")
    private Integer blogId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}

