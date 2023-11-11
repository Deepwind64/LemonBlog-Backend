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
@ApiModel(description = "博客实体类")
@TableName("blog")
public class Blog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "blog_id", type = IdType.AUTO)
    private Integer id;
    @TableField("blog_title")
    private String title;
    @TableField("blog_brief")
    private String brief;
    @TableField("blog_content")
    private String content;
    /**
     * 收藏次数
     */
    @TableField("blog_collect_num")
    private Integer collectNum;
    @TableField("blog_comment_num")
    private Integer commentNum;
    @TableField("blog_like_num")
    private Integer likeNum;
    @TableField("blog_read_num")
    private Integer readNum;
    @TableField("blog_label")
    private String label;
    /**
     * 创建者id
     */
    @TableField("user_id")
    private Long autherId;
    @TableField("blog_status")
    private Integer status;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
