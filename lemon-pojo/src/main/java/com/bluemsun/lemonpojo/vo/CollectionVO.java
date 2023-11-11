package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionVO {
    private Long id;
    private String name;
    private Integer size;
    /**
     * 表示收藏夹中是否包含当前博客
     */
    private Boolean include;
}
