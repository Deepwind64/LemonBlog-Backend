package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.entity.Like;

public interface LikeService extends IService<Like> {
    /**
     * 获取用户对某评论的点赞状态
     * @return 1为点赞，0为未点赞
     */
    void transCommentLikedCount();

    /**
     * 检查用户是否已经点赞，先查redis，再查数据库
     * @param userId 点赞用户id
     * @param objectId 点赞对象id
     * @param type 点赞对象类型
     * @param redisLikeMap 可选，不查redis则填null
     */
    Boolean getLikeStatus(Long userId, long objectId, String type, String redisLikeMap);

    void transCommentLiked();

    void transBlogLiked();

    void transBlogLikedCount();
}
