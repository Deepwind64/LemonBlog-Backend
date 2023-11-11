package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.entity.Comment;
import com.bluemsun.lemonpojo.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    // FIXME 管理员审核完善
    IPage<CommentVO> getComments(Page<Comment> commentPage, int blogId, String order, Long userId);

    IPage<CommentVO> getSubComments(Page<Comment> commentPage, long parentCommentId, Long userId);
}
