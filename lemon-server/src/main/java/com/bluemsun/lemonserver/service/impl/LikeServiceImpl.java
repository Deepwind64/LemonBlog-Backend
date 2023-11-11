package com.bluemsun.lemonserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.OperationType;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemonpojo.entity.Blog;
import com.bluemsun.lemonpojo.entity.Comment;
import com.bluemsun.lemonpojo.entity.Like;
import com.bluemsun.lemonserver.dao.CommentMapper;
import com.bluemsun.lemonserver.dao.LikeMapper;
import com.bluemsun.lemonserver.service.BlogService;
import com.bluemsun.lemonserver.service.LikeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import java.util.Map;
@Slf4j
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper,Like> implements LikeService {
    @Resource
    LikeMapper likeMapper;
    @Resource
    CommentMapper commentMapper;
    @Resource
    BlogService blogService;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Override
    public Boolean getLikeStatus(Long userId, long objectId, String type, String redisLikeMap){
        // 如果提供redis map，就先查redis
        if(userId==null){
            return false;
        }
        if(redisLikeMap!=null){
            Integer value = (Integer) redisTemplate.opsForHash().get(redisLikeMap, RedisConstant.getLikeKey(userId,objectId));
            if(value!=null) {
                if (value == OperationType.ADD_LIKE){
                    return true;
                } else if (value==OperationType.CANCEL_LIKE) {
                    return false;
                }
            }
        }
        // 再查数据库
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("like_user_id",userId);
        queryWrapper.eq("like_object_id",objectId);
        return getOne(queryWrapper) != null;
    }
    @Override
    public void transCommentLiked() {
        // 将存入map中的点赞记录持久化，发送点赞通知
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConstant.MAP_COMMENT_LIKED, ScanOptions.NONE);
        int counter=0;
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();

            String key = (String) entry.getKey();
            // 分离出 likedUserId，likedObjectId,likeOwnerId
            String[] split = key.split("::");
            Long likedUserId = Long.parseLong(split[0]);
            Long likedCommentId = Long.parseLong(split[1]);

            Integer value = (Integer) entry.getValue();
            if(value==OperationType.ADD_LIKE){
                // 添加点赞记录
                save(Like.builder()
                        .type(OperationType.COMMENT)
                        .userId(likedUserId)
                        .objectId(likedCommentId).build());
            }else{
                // 删除点赞记录
                QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("like_object_id", likedCommentId);
                queryWrapper.eq("like_user_id", likedUserId);
                remove(queryWrapper);
                // 不发取消点赞通知
            }
            //存到 list 后从 Redis 中删除
            redisTemplate.opsForHash().delete(RedisConstant.MAP_COMMENT_LIKED, key);
            counter++;
        }
        if(counter!=0) {
            log.info("已更新评论点赞关系，共更新{}条数据", counter);
        }
        cursor.close();
    }

    @Override
    public void transCommentLikedCount() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConstant.MAP_COMMENT_LIKED_COUNT, ScanOptions.NONE);
        int counter=0;
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            Long commentId = Long.parseLong((String)map.getKey());
            // 更新数据库
            commentMapper.updateById(Comment.builder().id(commentId).likeNum(commentMapper.selectById(commentId).getLikeNum()+(Integer) map.getValue()).build());
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(RedisConstant.MAP_COMMENT_LIKED_COUNT, commentId.toString());
            counter++;
        }
        if(counter!=0) {
            log.info("已更新评论点赞数，共有{}个评论点赞数改变", counter);
        }
        cursor.close();
    }

    @Override
    public void transBlogLiked() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConstant.MAP_BLOG_LIKED, ScanOptions.NONE);
        int counter=0;
        while (cursor.hasNext()){
            Map.Entry<Object, Object> entry = cursor.next();

            String key = (String) entry.getKey();

            String[] split = key.split("::");
            Long likedUserId = Long.parseLong(split[0]);
            Long likedBlogId = Long.parseLong(split[1]);

            Integer value = (Integer) entry.getValue();
            if(value==OperationType.ADD_LIKE){
                // 删除取消点赞/未点赞记录
                likeMapper.insert(Like.builder()
                        .type(OperationType.BLOG)
                        .userId(likedUserId)
                        .objectId(likedBlogId).build());
            }else{
                // 删除点赞记录
                QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("like_object_id", likedBlogId);
                queryWrapper.eq("like_user_id", likedUserId);
                remove(queryWrapper);
                // 不发取消点赞通知
            }
            // TODO 发出被点赞通知
            //存到 list 后从 Redis 中删除
            redisTemplate.opsForHash().delete(RedisConstant.MAP_BLOG_LIKED, key);
            counter++;
        }
        if(counter!=0) {
            log.info("已更新博客点赞关系，共更新{}条数据", counter);
        }
        cursor.close();
    }

    @Override
    public void transBlogLikedCount() {
        // 优化点赞计数更新函数设计
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConstant.MAP_BLOG_LIKED_COUNT, ScanOptions.NONE);
        int counter=0;
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            Integer blogId = Integer.parseInt((String)map.getKey());
            // 更新数据库
            blogService.updateById(Blog.builder().id(blogId).likeNum(blogService.getById(blogId).getLikeNum() + (Integer) map.getValue()).build());
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(RedisConstant.MAP_BLOG_LIKED_COUNT, blogId.toString());
            counter++;
        }
        if(counter!=0){
            log.info("已更新博客点赞数，共有{}个博客点赞数改变",counter);
        }
        cursor.close();
    }
}
