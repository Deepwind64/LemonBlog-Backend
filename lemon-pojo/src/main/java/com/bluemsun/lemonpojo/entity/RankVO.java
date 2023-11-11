package com.bluemsun.lemonpojo.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "排名实体类")
public class RankVO {
    /**
     *  用来表明具体的对象
     */
    Long objectId;
    /**
     * 对象在排行榜上的排名
     */
    Long rank;
    /**
     * 用户的历史最高积分，也就是排行榜上的积分
     */
    String name;

}
