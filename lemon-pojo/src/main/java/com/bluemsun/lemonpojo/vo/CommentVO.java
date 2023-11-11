package com.bluemsun.lemonpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentVO {
    private Long id;
    private String content;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Integer likeNum;
    private Long createTime;
    private Long parentCommentId;
    private Long replyUserId;
    private String replyUsername;
    private Boolean likeStatus;
}
