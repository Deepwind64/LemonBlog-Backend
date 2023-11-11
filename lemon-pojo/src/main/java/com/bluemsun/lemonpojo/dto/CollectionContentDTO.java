package com.bluemsun.lemonpojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于向收藏夹添加博客时
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContentDTO {
    @Min(message = "非法ID值", value = 1L)
    private Integer collectionId;
    @Min(message = "非法ID值", value = 1L)
    private Integer blogId;
}
