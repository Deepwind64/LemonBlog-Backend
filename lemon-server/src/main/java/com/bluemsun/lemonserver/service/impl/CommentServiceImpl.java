package com.bluemsun.lemonserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.constant.OperationType;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemoncommon.exception.InvalidArgsException;
import com.bluemsun.lemonpojo.entity.Comment;
import com.bluemsun.lemonpojo.entity.Userinfo;
import com.bluemsun.lemonpojo.vo.CommentVO;
import com.bluemsun.lemonserver.dao.CommentMapper;
import com.bluemsun.lemonserver.service.CommentService;
import com.bluemsun.lemonserver.service.LikeService;
import com.bluemsun.lemonserver.service.UserinfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author deepwind
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    CommentMapper commentMapper;
    @Resource
    UserinfoService userinfoService;
    @Resource
    LikeService likeService;
    @Override
    public IPage<CommentVO> getComments(Page<Comment> page, int blogId, String order, Long userId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 查询博客对应评论区
        queryWrapper.eq("blog_id",blogId);
        // 筛选主评论
        queryWrapper.eq("comment_priority", OperationType.MAIN);
        // 按 order 排序，降序
        switch (order){
            case OperationType.HOT -> queryWrapper.orderBy(true,false,"comment_like","create_time");
            case OperationType.TIME -> queryWrapper.orderBy(true,false,"create_time");
            default -> {
                throw new InvalidArgsException(MessageConstant.INVALID_ORDER);
            }
        }
        Page<Comment> commentPage = commentMapper.selectPage(page,queryWrapper);
        return commentPage.convert(result ->{
            CommentVO commentVO = new CommentVO();
            BeanUtil.copyProperties(result,commentVO);
            commentVO.setUserNickname(userinfoService.getByUserId(userId).getNickname());
            commentVO.setUserAvatar(userinfoService.getAvatarUrlByUserId(userId));
            commentVO.setLikeStatus(likeService.getLikeStatus(userId,commentVO.getId(),OperationType.COMMENT,RedisConstant.MAP_COMMENT_LIKED));
            return commentVO;
        });
    }

    @Override
    public IPage<CommentVO> getSubComments(Page<Comment> page, long parentCommentId, Long userId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 查询博客对应评论区
        queryWrapper.eq("comment_parent_comment_id", parentCommentId);
        // 筛选子评论
        queryWrapper.eq("comment_priority", OperationType.SUB);

        Page<Comment> commentPage = commentMapper.selectPage(page,queryWrapper);
        return commentPage.convert(result ->{
            CommentVO commentVO = new CommentVO();
            BeanUtil.copyProperties(result,commentVO);
            long commentCreatorId = result.getUserId();
            Userinfo replyUserinfo = getUserinfo(result.getReplyCommentId());
            commentVO.setUserNickname(userinfoService.getByUserId(commentCreatorId).getNickname());
            commentVO.setUserAvatar(userinfoService.getAvatarUrlByUserId(commentCreatorId));
            commentVO.setLikeStatus(likeService.getLikeStatus(userId,commentVO.getId(),OperationType.COMMENT,RedisConstant.MAP_COMMENT_LIKED));
//            commentVO.setParentCommentId(parentCommentId);
            commentVO.setReplyUserId(replyUserinfo.getUserId());
            commentVO.setReplyUsername(replyUserinfo.getNickname());
            return commentVO;
        });
    }
    public Userinfo getUserinfo(long commentId){
        return userinfoService.getByUserId(commentMapper.selectById(commentId).getUserId());
    }

}
