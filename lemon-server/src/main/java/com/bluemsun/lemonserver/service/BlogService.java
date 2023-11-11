package com.bluemsun.lemonserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bluemsun.lemonpojo.dto.BlogContentDTO;
import com.bluemsun.lemonpojo.entity.*;
import com.bluemsun.lemonpojo.vo.*;

import java.util.List;

/**
 * @author deepwind
 */
public interface BlogService extends IService<Blog> {
    int createBlog(BlogContentDTO blogContentDTO);
    Author getAuthor(long authorId);

    boolean checkBlogExist(long blogId);

    void addCommentLike(long userId, long commentId);

    void cancelCommentLike(long userId, long commentId);

    void addBlogLike(Long userId, int blogId);

    void cancelBlogLike(Long userId, int blogId);

    IPage<BlogBasicInfoVO> getUserBlogs(Page<Blog> page, Long userId, boolean isOwner);

    void updateView();

    boolean checkAccuse(int type, long objectId);

    BlogContentVO getBlogContent(int blogId);

    BlogContentVO getBlogContent(int blogId, long userId);

    IPage<BlogBasicInfoVO> searchBlog(Page<Blog> page, String content);

    IPage<UserinfoLiteVO> searchUser(Page<Userinfo> page, String content);

    void addColumn(String name, Long userId);

    void addColumnBlog(int columnId, int blogId, Long userId);

    void removeColumnBlog(int columnId, int blogId);

    void removeColumn(int columnId);


    IPage<ColumnVO> getUserColumn(Page<Column> page, int userId);

    IPage<BlogBasicInfoVO> getColumns(Page<Blog> page, int columnId);

    long getColumnOwnerId(int columnId);

    List<BlogBasicInfoVO> getUnexaminedBlog();

    boolean blogPassAuthenticate(int blogId);

    IPage<AccuseVO> getAccuseIntro(Page<Accuse> page, int type);

    boolean isAccuseExist(int accuseId);

    void rejectAccuse(int accuseId);

    IPage<ColumnVO> getUserColumnForAdd(Page<Column> objectPage, int userId, int blogId);

    void addLabel(String label);

    List<String> getLabels();

    void removeLabel(String trim);

}
