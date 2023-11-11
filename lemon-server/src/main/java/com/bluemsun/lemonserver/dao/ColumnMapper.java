package com.bluemsun.lemonserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemonpojo.entity.Blog;
import com.bluemsun.lemonpojo.entity.Column;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ColumnMapper extends BaseMapper<Column> {
    Page<Blog> getColumnBlogs(Page<Blog> page, @Param("columnId") int columnId);
    List<Integer> getColumnBlogName(int columnId);
}
