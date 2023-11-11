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
@ApiModel(description = "举报实体类")
@TableName("accuse")
public class Accuse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "accuse_id",type = IdType.AUTO)
    private Integer id;
    /**
     * blog为0/comment为1
     */
    @TableField(value = "type")
    private Integer type;
    @TableField(value = "content")
    private String content;
    @TableField(value = "brief")
    private String brief;
    @TableField(value = "object_id")
    private Long objectId;
    @TableField(value = "user_id")
    private Long userId;
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private Date createTime;
}
