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
@ApiModel(description = "博客收藏信息实体类")
@TableName("blog_mto_collection")
public class CollectionContent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(value = "blog_mto_collection_id",type = IdType.AUTO)
    private Long id;
    @TableField(value = "collection_id")
    private Integer collectionId;
    @TableField(value = "blog_id")
    private Integer blogId;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
