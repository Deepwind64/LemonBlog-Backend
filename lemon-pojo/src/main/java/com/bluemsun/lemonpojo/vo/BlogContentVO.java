package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author deepwind
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogContentVO {
    private String title;
    private String auther;
    private Long autherId;
    /**
     * 头像链接
     */
    private String autherAvatar;
    private String brief;
    private String[] labels;
    private String content;
    private FileVO[] files;
    private Integer collectNum;
    private Integer commentNum;
    private Integer likeNum;
    private Integer readNum;
    private Boolean likeStatus;
    private Boolean collectedStatus;
    private Date createTime;
    private Date updateTime;

}
