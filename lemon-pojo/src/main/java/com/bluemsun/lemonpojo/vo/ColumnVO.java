package com.bluemsun.lemonpojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnVO {
    private Integer id;
    private String name;
    private Integer blogNum;
    private String pictureUrl;
    private Long userId;
    private Date createTime;
    private Date updateTime;
    private Boolean isBlogContained;
}
