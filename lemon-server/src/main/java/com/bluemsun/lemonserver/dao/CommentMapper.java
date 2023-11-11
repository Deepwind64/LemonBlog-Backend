package com.bluemsun.lemonserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluemsun.lemonpojo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
