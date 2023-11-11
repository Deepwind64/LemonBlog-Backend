package com.bluemsun.lemonserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.*;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.enumeration.BlogStatus;
import com.bluemsun.lemoncommon.exception.PermissionDeniedException;
import com.bluemsun.lemoncommon.exception.ResourceNotFoundException;
import com.bluemsun.lemonpojo.dto.BlogContentDTO;
import com.bluemsun.lemonpojo.entity.*;
import com.bluemsun.lemonpojo.vo.*;
import com.bluemsun.lemonserver.dao.AccuseMapper;
import com.bluemsun.lemonserver.dao.BlogMapper;
import com.bluemsun.lemonserver.dao.ColumnContentMapper;
import com.bluemsun.lemonserver.dao.ColumnMapper;
import com.bluemsun.lemonserver.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author deepwind
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Resource
    BlogMapper blogMapper;
    @Resource
    FileService fileService;
    @Resource
    UserinfoService userinfoService;
    @Resource
    LikeService likeService;
    @Resource
    CommentService commentService;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Resource
    ColumnMapper columnMapper;
    @Resource
    ColumnContentMapper columnContentMapper;
    @Resource
    PictureService pictureService;
    @Resource
    MessageService messageService;
    @Resource
    AccuseMapper accuseMapper;
    @Override
    public int createBlog(BlogContentDTO blogContentDTO) {
        Long creatorId = blogContentDTO.getUserId();
        // 构建博客对象并存入数据库
        Blog blog = Blog.builder()
                .title(blogContentDTO.getTitle())
                .brief(blogContentDTO.getBrief())
                .content(blogContentDTO.getContent())
                .label(StrUtil.join(",", (Object) blogContentDTO.getLabel()))
                .autherId(creatorId)
                .status(BlogStatus.UNCHECK.getCode()).build();
        blogMapper.insert(blog);
        fileService.updateBlogId(creatorId, blogContentDTO.getFile(), blog.getId());
        return blog.getId();
    }

    @Override
    public Author getAuthor(long authorId) {
        return Author.builder()
                .authorId(authorId)
                .authorName(userinfoService.getByUserId(authorId).getNickname())
                .authorAvatar(userinfoService.getAvatarUrlByUserId(authorId))
                .build();
    }
    @Override
    public boolean checkBlogExist(long blogId){
        if (blogMapper.exists(new QueryWrapper<>(Blog.builder().id((int) blogId).build()))){
            return true;
        }else{
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
    }

    @Override
    public void addCommentLike(long userId, long commentId) {
        // 仅在未点赞状态下进行操作
        if(!likeService.getLikeStatus(userId,commentId,OperationType.COMMENT,RedisConstant.MAP_COMMENT_LIKED)){
            redisTemplate.opsForHash().increment(RedisConstant.MAP_COMMENT_LIKED_COUNT, String.valueOf(commentId),1L);
            Integer redisRecord = (Integer) redisTemplate.opsForHash().get(RedisConstant.MAP_BLOG_LIKED, RedisConstant.getLikeKey(userId,commentId));
            if (redisRecord==null || redisRecord == 0) {
                redisTemplate.opsForHash().put(RedisConstant.MAP_COMMENT_LIKED, RedisConstant.getLikeKey(userId, commentId), OperationType.ADD_LIKE);
            }
            // 避免自己给自己发点赞消息
            long commentOwnerId = commentService.getById(commentId).getUserId();
            if(commentOwnerId!=userId){
                // 发送点赞消息至被点赞者的消息系统
                String content = StrUtil.format(MessageConstant.ADD_LIKE_UNCOMPLETED,
                        getAuthor(UserContext.getUserId()).getAuthorName());
                messageService.send(Message.builder()
                                .title(MessageConstant.ADD_LIKE)
                                .content(content)
                                .senderId(DefaultConstant.SYSTEM_USER_ID)
                                .receiverId(commentOwnerId)
                                .createTime(new Date()).build());
            }
        }
    }

    @Override
    public void cancelCommentLike(long userId, long commentId) {
        // 仅在已点赞状态下进行操作
        if(likeService.getLikeStatus(userId,commentId,OperationType.COMMENT,RedisConstant.MAP_COMMENT_LIKED)) {
            redisTemplate.opsForHash().increment(RedisConstant.MAP_COMMENT_LIKED_COUNT, String.valueOf(commentId), -1L);
            // 如果点赞后马上取消点赞，则删除记录
            Integer redisRecord = (Integer) redisTemplate.opsForHash().get(RedisConstant.MAP_COMMENT_LIKED, RedisConstant.getLikeKey(userId,commentId));
            if (redisRecord==null || redisRecord == 1) {
                // 否则增加一条取消点赞的记录
                redisTemplate.opsForHash().put(RedisConstant.MAP_COMMENT_LIKED, RedisConstant.getLikeKey(userId, commentId), OperationType.CANCEL_LIKE);
            }
        }
    }
    @Override
    public void addBlogLike(Long userId, int blogId) {
        // 仅在未点赞状态下进行操作
        if(!likeService.getLikeStatus(userId,blogId,OperationType.BLOG,RedisConstant.MAP_BLOG_LIKED)) {
            // 点赞数++
            redisTemplate.opsForHash().increment(RedisConstant.MAP_BLOG_LIKED_COUNT, String.valueOf(blogId),1L);
            // 热榜加点赞分
            redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,String.valueOf(blogId), MarkConstant.LIKE);
            // 存点赞记录，如果有存在取消点赞的记录就删除那条记录
            Integer redisRecord = (Integer) redisTemplate.opsForHash().get(RedisConstant.MAP_BLOG_LIKED, RedisConstant.getLikeKey(userId,(long) blogId));
            if(redisRecord==null || redisRecord == 0) {
                redisTemplate.opsForHash().put(RedisConstant.MAP_BLOG_LIKED, RedisConstant.getLikeKey(userId, (long) blogId), OperationType.ADD_LIKE);
            }
            // 避免自己给自己发点赞消息
            long commentOwnerId = blogMapper.selectById(blogId).getAutherId();
            if(commentOwnerId!=userId){
                // 发送点赞消息至被点赞者的消息系统
                String content = StrUtil.format(MessageConstant.ADD_LIKE_UNCOMPLETED,
                        getAuthor(UserContext.getUserId()).getAuthorName());
                messageService.send(Message.builder()
                        .title(MessageConstant.ADD_LIKE)
                        .content(content)
                        .senderId(DefaultConstant.SYSTEM_USER_ID)
                        .receiverId(commentOwnerId)
                        .createTime(new Date()).build());
            }
        }
    }

    @Override
    public void cancelBlogLike(Long userId, int blogId) {
        // 仅在已点赞状态下进行操作
        log.info(likeService.getLikeStatus(userId,blogId,OperationType.BLOG,RedisConstant.MAP_BLOG_LIKED).toString());
        if(likeService.getLikeStatus(userId,blogId,OperationType.BLOG,RedisConstant.MAP_BLOG_LIKED)) {
            redisTemplate.opsForHash().increment(RedisConstant.MAP_BLOG_LIKED_COUNT, String.valueOf(blogId),-1L);
            // 热榜加点赞分
            redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,String.valueOf(blogId), -1 * MarkConstant.LIKE);
            // 如果点赞后马上取消点赞，则删除记录
            Integer redisRecord = (Integer) redisTemplate.opsForHash().get(RedisConstant.MAP_BLOG_LIKED, RedisConstant.getLikeKey(userId,(long) blogId));
            if(redisRecord==null || redisRecord == 1){
                // 否则增加一条取消点赞的记录
                redisTemplate.opsForHash().put(RedisConstant.MAP_BLOG_LIKED,RedisConstant.getLikeKey(userId, (long) blogId), OperationType.CANCEL_LIKE);
            }
        }
    }

    @Override
    public IPage<BlogBasicInfoVO> getUserBlogs(Page<Blog> page, Long userId, boolean isOwner) {
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        // 所有者有权查看所有博客，其他人仅可查看已过审博客
        if(!isOwner){
            queryWrapper.eq("blog_status",BlogStatus.PASS.getCode());
        }
        return blogMapper.selectPage(page,queryWrapper).convert(this::getBlogBasicVO);

    }

    @Override
    public void updateView() {
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(RedisConstant.MAP_BLOG_VIEW_COUNT, ScanOptions.NONE);
        int counter=0;
        while (cursor.hasNext()){
            Map.Entry<Object, Object> map = cursor.next();
            Integer blogId = Integer.parseInt((String)map.getKey());
            // 更新数据库
            blogMapper.updateById(Blog.builder().id(blogId).readNum(getById(blogId).getReadNum()+(Integer) map.getValue()).build());
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(RedisConstant.MAP_BLOG_VIEW_COUNT, blogId.toString());
            counter++;
        }
        if(counter!=0){
            log.info("已更新博客浏览量，在过去3秒内共有{}个博客被访问", counter);
        }
        cursor.close();
    }

    @Override
    public boolean checkAccuse(int type, long objectId) {
        return  (type==0 && checkBlogExist(objectId) && blogPassAuthenticate((int)objectId)) ||
                (type==1 && commentService.getById(objectId)!=null);
    }

    @Override
    public BlogContentVO getBlogContent(int blogId) {
        // 从数据库获取需要的属性
        Blog blog = getById(blogId);
        if(blog==null){
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
        BlogContentVO blogContentVO = new BlogContentVO();
        // 复制属性
        BeanUtil.copyProperties(blog, blogContentVO);
        // 补充属性
        Author author = getAuthor(blog.getAutherId());
        blogContentVO.setAuther(author.getAuthorName());
        blogContentVO.setAutherAvatar(author.getAuthorAvatar());
        blogContentVO.setLabels(blog.getLabel().split(","));
        blogContentVO.setFiles(fileService.getBlogFile(blogId));
        // 未登录状态不增加访问量
        return blogContentVO;
    }
    @Override
    public BlogContentVO getBlogContent(int blogId, long userId) {
        // 从数据库获取需要的属性
        Blog blog = getById(blogId);
        if(blog==null){
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
        BlogContentVO blogContentVO = new BlogContentVO();
        // 复制属性
        BeanUtil.copyProperties(blog, blogContentVO);
        // 补充属性
        Author author = getAuthor(blog.getAutherId());
        blogContentVO.setAuther(author.getAuthorName());
        blogContentVO.setAutherAvatar(author.getAuthorAvatar());
        blogContentVO.setLabels(blog.getLabel().split(","));
        blogContentVO.setFiles(fileService.getBlogFile(blogId));
        blogContentVO.setLikeStatus(likeService.getLikeStatus(userId,blogId,OperationType.BLOG,RedisConstant.MAP_BLOG_LIKED));
        blogContentVO.setCollectedStatus(isCollected(blogId,userId));
        // 访问量++
        redisTemplate.opsForHash().increment(RedisConstant.MAP_BLOG_VIEW_COUNT, String.valueOf(blogId),1L);
        // 热榜评分+1
        redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,String.valueOf(blogId), MarkConstant.READ);
        // TODO 增加记录访问历史功能
        return blogContentVO;
    }
    boolean isCollected(int blogId, long userId){
        return blogMapper.isCollected(blogId,userId);
    }

    @Override
    public IPage<BlogBasicInfoVO> searchBlog(Page<Blog> page, String content) {
        return blogMapper.searchBlog(page, content).convert(this::getBlogBasicVO);
    }

    @Override
    public IPage<UserinfoLiteVO> searchUser(Page<Userinfo> page, String content) {
        return blogMapper.searchUser(page, content).convert(result-> UserinfoLiteVO.builder()
                .userId(result.getUserId())
                .nickname(result.getNickname())
                .intro(result.getIntro())
                .avatar(userinfoService.getAvatarUrlByUserId(result.getUserId())).build());
    }

    @Override
    public void addColumn(String name, Long userId) {
        columnMapper.insert(Column.builder()
                        .name(name)
                        .blogNum(0)
                        .userId(userId)
                        .pictureId(DefaultConstant.DEFAULT_COLUMN_PIC_ID)
                        .build());
        log.info(MessageConstant.LOG_USER_OPERATION,userId,"添加专栏 "+name);
    }

    @Override
    public void addColumnBlog(int columnId, int blogId, Long userId) {
        // 确保未存过
        if(!isBlogInColumn(columnId,blogId)){
            // 保存记录
            columnContentMapper.insert(ColumnContent.builder().blogId(blogId).columnId(columnId).build());
            // 收藏夹大小+1
            int size = columnMapper.selectById(columnId).getBlogNum();
            columnMapper.updateById(Column.builder().id(columnId).blogNum(size+1).build());
        }
        if(columnMapper.selectById(columnId)==null){
            throw new ResourceNotFoundException("专栏不存在");
        }
        // 已保存情况不报错
    }

    private boolean isBlogInColumn(int columnId, int blogId) {
        return columnMapper.getColumnBlogName(columnId).contains(blogId);
    }

    @Override
    public void removeColumnBlog(int columnId, int blogId) {
        // 确保存过
        if(isBlogInColumn(columnId,blogId)){
            // 保存记录
            QueryWrapper<ColumnContent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("blog_mto_column_column_id",columnId);
            queryWrapper.eq("blog_mto_column_blog_id",blogId);
            columnContentMapper.delete(queryWrapper);
            // 收藏夹大小-1
            int size = columnMapper.selectById(columnId).getBlogNum();
            columnMapper.updateById(Column.builder().id(columnId).blogNum(size-1).build());
        }
        else{
            throw new ResourceNotFoundException("博客未保存在指定专栏中");
        }
    }

    @Override
    public void removeColumn(int columnId) {
        columnMapper.deleteById(columnId);
    }

    @Override
    public IPage<ColumnVO> getUserColumn(Page<Column> page, int userId) {
        QueryWrapper<Column> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return columnMapper.selectPage(page,queryWrapper).convert(result->{
            ColumnVO columnVO = new ColumnVO();
            BeanUtil.copyProperties(result, columnVO);
            columnVO.setIsBlogContained(false);
            columnVO.setPictureUrl(pictureService.getPicUrl(result.getPictureId()));
            return columnVO;
        });
    }
    @Override
    public IPage<ColumnVO> getUserColumnForAdd(Page<Column> page, int userId, int blogId) {
        QueryWrapper<Column> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return columnMapper.selectPage(page,queryWrapper).convert(result->{
            ColumnVO columnVO = new ColumnVO();
            BeanUtil.copyProperties(result, columnVO);
            columnVO.setIsBlogContained(isBlogInColumn(result.getId(),blogId));
            columnVO.setPictureUrl(pictureService.getPicUrl(result.getPictureId()));
            return columnVO;
        });
    }

    @Override
    public IPage<BlogBasicInfoVO> getColumns(Page<Blog> page, int columnId) {
        return columnMapper.getColumnBlogs(page, columnId).convert(this::getBlogBasicVO);
    }
    private BlogBasicInfoVO getBlogBasicVO(Blog blog){
        Author author = getAuthor(blog.getAutherId());
        return BlogBasicInfoVO.builder()
                .blogId(blog.getId())
                .title(blog.getTitle())
                .blogBrief(blog.getBrief())
                .blogLabels(blog.getLabel().split(","))
                .auther(author.getAuthorName())
                .autherId(author.getAuthorId())
                .status(blog.getStatus())
                .autherAvatar(author.getAuthorAvatar())
                .build();
    }

    @Override
    public long getColumnOwnerId(int columnId) {
        return columnMapper.selectById(columnId).getUserId();
    }

    @Override
    public List<BlogBasicInfoVO> getUnexaminedBlog() {
        List<BlogBasicInfoVO> blogList = new ArrayList<>();
        redisTemplate.opsForHash().entries(RedisConstant.MAP_BLOG_EXAMINE).forEach((key, value) -> {
            blogList.add(getBlogBasicVO(getById(Integer.valueOf((String) key))));
        });
        return blogList;
    }

    @Override
    public boolean blogPassAuthenticate(int blogId) {
        if(getById(blogId).getStatus()==BlogStatus.UNCHECK.getCode()){
            throw new PermissionDeniedException(MessageConstant.PERMISSION_DENIED);
        }
        return true;
    }

    @Override
    public IPage<AccuseVO> getAccuseIntro(Page<Accuse> page, int type) {
        QueryWrapper<Accuse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type",type);
        return accuseMapper.selectPage(page,queryWrapper).convert(accuse->{
            AccuseVO accuseVO = new AccuseVO();
            BeanUtil.copyProperties(accuse,accuseVO);
            if(type==0){accuseVO.setBlogTitle(getById(accuseVO.getObjectId()).getTitle());}
            return accuseVO;
        });
    }

    @Override
    public boolean isAccuseExist(int accuseId) {
        return accuseMapper.selectById(accuseId)!=null;
    }

    @Override
    public void rejectAccuse(int accuseId) {
        Accuse accuse = accuseMapper.selectById(accuseId);
        accuseMapper.deleteById(accuseId);
        // 发送驳回消息
        String objectName = accuse.getType()==0 ? getById(accuse.getObjectId()).getTitle() : commentService.getById(accuse.getObjectId()).getContent().substring(0,5)+"...";
        String title = StrUtil.format(MessageConstant.ACCUSE_REFUSE_UNCOMPLETED,objectName);
        messageService.send(Message.builder()
                .title(title)
                .content("")
                .senderId(DefaultConstant.SYSTEM_USER_ID)
                .receiverId(accuse.getUserId())
                .createTime(new Date()).build());
    }

    @Override
    public void addLabel(String label) {
        redisTemplate.opsForList().rightPush(RedisConstant.LIST_LABEL,label);
    }

    @Override
    public List<String> getLabels() {
        return redisTemplate.opsForList().range(RedisConstant.LIST_LABEL,0,-1);
    }

    @Override
    public void removeLabel(String label) {
        redisTemplate.opsForList().remove(RedisConstant.LIST_LABEL,0,label);
    }
}
