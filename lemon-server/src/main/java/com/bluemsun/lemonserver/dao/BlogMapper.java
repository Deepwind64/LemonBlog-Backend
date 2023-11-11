package com.bluemsun.lemonserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemonpojo.entity.Blog;
import com.bluemsun.lemonpojo.entity.Userinfo;
import com.bluemsun.lemonpojo.vo.CollectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author deepwind
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    Page<Blog> getCollectionContent(Page<Blog> page, @Param("collectionId") int collectionId);
    Boolean isCollected(@Param("blogId") int blogId,@Param("userId") long userId);

    /**
     * 仅返回已审核通过的博客数据
     * @param page
     * @param content
     * @return
     */
    Page<Blog> searchBlog(Page<Blog> page, @Param("content") String content);

    Page<Userinfo> searchUser(Page<Userinfo> page, @Param("content") String content);
}
