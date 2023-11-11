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
@ApiModel(description = "点赞关系类")
@TableName("`like`")
public class Like implements Serializable {
    /**
     * 注意，like是标识符，不建议作为数据库名
     */
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "like_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "like_object_id")
    private Long objectId;
    @TableField(value = "like_type")
    private String type;
    @TableField(value = "like_user_id")
    private Long userId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
