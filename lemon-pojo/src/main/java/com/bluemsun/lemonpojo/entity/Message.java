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
@ApiModel(description = "消息实体类")
@TableName("message")
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer id;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("sender_id")
    private Long senderId;
    @TableField("receiver_id")
    private Long receiverId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
