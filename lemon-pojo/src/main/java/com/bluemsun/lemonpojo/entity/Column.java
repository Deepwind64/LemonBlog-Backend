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
@ApiModel(description = "专栏实体类")
@TableName("columns")
public class Column implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "column_id", type = IdType.AUTO)
    private Integer id;
    @TableField("name")
    private String name;
    /**
     * 专栏中的博客数量
     */
    @TableField("blog_num")
    private Integer blogNum;
    @TableField("picture_id")
    private Long pictureId;
    @TableField("user_id")
    private Long userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}