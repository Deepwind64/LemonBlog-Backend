package com.bluemsun.lemonpojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccuseVO {
    private Integer id;
    /**
     * blog为0/comment为1
     */
    private Integer type;
    private String content;
    private String blogTitle;
    private String brief;
    private Long objectId;
    private Long userId;
}
