package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogBasicInfoVO {
    private Integer blogId;
    private String title;
    private String auther;
    private Long autherId;
    /**
     * 头像链接
     */
    private String autherAvatar;
    private String blogBrief;
    private String[] blogLabels;
    private Integer status;
}
