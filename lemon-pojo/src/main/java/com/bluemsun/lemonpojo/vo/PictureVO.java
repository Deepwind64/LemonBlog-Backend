package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PictureVO {
    private Long id;
    private String url;
}
