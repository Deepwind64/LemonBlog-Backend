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
@ApiModel(description = "评论对象类")
@TableName("comment")
public class Comment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "comment_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "blog_id")
    private Integer blogId;
    @TableField(value = "comment_content")
    private String content;
    @TableField(value = "comment_like")
    private Integer likeNum;
    @TableField(value = "comment_priority")
    private Integer priority;
    @TableField(value = "comment_parent_comment_id")
    private Long parentCommentId;
    @TableField(value = "comment_reply_user_id")
    private Long replyUserId;
    @TableField(value = "comment_reply_comment_id")
    private Long replyCommentId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

}
