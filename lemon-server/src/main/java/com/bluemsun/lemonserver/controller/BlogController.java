package com.bluemsun.lemonserver.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.*;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.enumeration.BlogStatus;
import com.bluemsun.lemoncommon.exception.*;
import com.bluemsun.lemonpojo.dto.*;
import com.bluemsun.lemonpojo.entity.*;
import com.bluemsun.lemonpojo.vo.*;
import com.bluemsun.lemonserver.dao.AccuseMapper;
import com.bluemsun.lemonserver.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author deepwind
 */
@Slf4j
@RestController
@RequestMapping("/blog")
@Api("博客访问与管理相关接口")
public class BlogController {
    @Resource
    BlogService blogService;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Resource
    FileService fileService;
    @Resource
    CommentService commentService;
    @Resource
    AccuseMapper accuseMapper;
    @Resource
    UserinfoService userinfoService;

    @ApiOperation("提交博客")
    @PostMapping("/submit")
    public int submitBlog(@RequestBody @Validated BlogContentDTO blogContentDTO){
        // 鉴权
        UserService.userAuthenticate(blogContentDTO.getUserId());
        // 接收基本信息,存入blog数据库,返回博客id
        int blogId = blogService.createBlog(blogContentDTO);
        // 加入审核列表
        redisTemplate.opsForHash().put(RedisConstant.MAP_BLOG_EXAMINE,String.valueOf(blogId),BlogStatus.UNCHECK.getCode());
        log.info("用户id：{}创建博客：{}，blogId={}",blogContentDTO.getUserId(),blogContentDTO.getTitle(),blogId);
        return blogId;
    }
    @ApiOperation("查看博客基本信息")
    @GetMapping("/basicInfo/{blogId}")
    public BlogBasicInfoVO getBlogBasicInfo(@PathVariable int blogId){
        Blog blog = blogService.getById(blogId);
        Author author = blogService.getAuthor(blog.getAutherId());
        return BlogBasicInfoVO.builder()
                .blogId(blogId)
                .autherId(author.getAuthorId())
                .title(blog.getTitle())
                .auther(author.getAuthorName())
                .autherAvatar(author.getAuthorAvatar())
                .blogBrief(blog.getBrief())
                .blogLabels(blog.getLabel().split(",")).build();
    }
    @ApiOperation(value = "展示博客详情——用户未登录",notes = "公开api")
    @GetMapping("/view/{blogId}")
    public BlogContentVO getBlogContent(@PathVariable int blogId){
        // 阻止查看未过审博客
        blogService.blogPassAuthenticate(blogId);
        return blogService.getBlogContent(blogId);
    }
    @ApiOperation(value = "展示博客详情——用户已登录")
    @GetMapping("/user/view/{blogId}")
    public BlogContentVO getBlogContentWithToken(@PathVariable int blogId){
        Blog blog = blogService.getById(blogId);
        if(blog.getStatus()==BlogStatus.UNCHECK.getCode()){
            // 用户试图查看未过审博客时进行鉴权，放行作者
            UserService.userAuthenticate(blog.getAutherId());
        }
        return blogService.getBlogContent(blogId, UserContext.getUserId());
    }
    @ApiOperation(value = "更新博客详情")
    @PutMapping("/{blogId}/update")
    public void updateBlogContent(@PathVariable int blogId, @RequestBody @Validated BlogUpdateDTO blogUpdateDTO){
        if (blogService.checkBlogExist(blogId)){
            Blog blog = blogService.getById(blogId);
            // authentication
            UserService.userAuthenticate(blog.getAutherId());

            BeanUtil.copyProperties(blogUpdateDTO,blog);
            blog.setStatus(BlogStatus.UNCHECK.getCode());
            // 重新加入审核列表
            redisTemplate.opsForHash().put(RedisConstant.MAP_BLOG_EXAMINE,String.valueOf(blogId),BlogStatus.UNCHECK.getCode());
            // 更新file文件blogId状态
            fileService.updateBlogId(UserContext.getUserId(), blogUpdateDTO.getFile(), blog.getId());
            blogService.updateById(blog);
        }
    }
    @ApiOperation("上传图片")
    @PostMapping("/pic")
    public String uploadPicture(@RequestParam("pic") MultipartFile pic) throws IOException {
        // FIXME 实现冗余图片删除

        if(DefaultConstant.MAX_PIC_SIZE < pic.getSize()){
            throw new InvalidFileException(MessageConstant.TOO_LARGE_PICTURE_ERROR);
        }

        return fileService.savePic(pic, UserContext.getUserId()).getUrl();
    }
    @ApiOperation("上传文件")
    @PutMapping("/file")
    public FileVO uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        // 文件最大大小在 application.yaml 中统一管理
        return fileService.saveFile(file, UserContext.getUserId());
    }
    @ApiOperation("删除文件")
    @DeleteMapping("/file")
    public void delFile(int fileId){
        // 文件最大大小在 application.yaml 中统一管理
        UserService.userAuthenticate(fileService.getOwner(fileId));
        fileService.removeFile(fileId);
    }
    @ApiOperation(value = "展示一/二级评论",notes = "公开api")
    @GetMapping("/{blogId}/comments")
    public IPage<CommentVO> showComments(@PathVariable int blogId, @RequestParam("parentCommentId") long parentCommentId, @RequestParam("page") Long page, @RequestParam("order") String order){
        blogService.blogPassAuthenticate(blogId);
        // 判断是不是二级评论
        if (parentCommentId==0){
            return commentService.getComments(new Page<>(page, DefaultConstant.PAGE_CAPACITY), blogId, order,null);
        }else{
            return commentService.getSubComments(new Page<>(page,DefaultConstant.PAGE_CAPACITY), parentCommentId,null);
        }
    }
    @ApiOperation(value = "修改评论赞状态")
    @PostMapping("/comment/{commentId}/like")
    public void updateCommentLike(@PathVariable int commentId, @RequestBody @Validated LikeDTO likeDTO){
        if(commentService.getById(commentId)!=null){
            switch (likeDTO.getType()){
                case OperationType.ADD_LIKE -> blogService.addCommentLike(UserContext.getUserId(), commentId);
                case OperationType.CANCEL_LIKE -> blogService.cancelCommentLike(UserContext.getUserId(), commentId);
                default -> {}
            }
        }else{
            throw new ResourceNotFoundException("评论"+MessageConstant.RES_NOT_FOUND);
        }
    }
    @ApiOperation(value = "添加评论")
    @PostMapping("/{blogId}/comment")
    public void addComment(@PathVariable Integer blogId, @RequestBody @Validated CommentDTO commentDTO){
        // 博客状态校验
        blogService.blogPassAuthenticate(blogId);
        Comment newComment = Comment.builder()
                .blogId(blogId)
                .content(commentDTO.getContent())
                .priority(OperationType.MAIN)
                .userId(UserContext.getUserId()).build();
        // 添加子评论属性
        Long parentCommentId = commentDTO.getParentCommentId();
        Long replyCommentId = commentDTO.getReplyCommentId();
        if (parentCommentId!=null && replyCommentId!=null){
            newComment.setParentCommentId(parentCommentId);
            newComment.setPriority(OperationType.SUB);
            newComment.setReplyCommentId(replyCommentId);
            newComment.setReplyUserId(commentService.getById(replyCommentId).getUserId());
        }

        commentService.save(newComment);

        // 改变博客的评论数
        int commendNum = blogService.getById(blogId).getCommentNum();
        blogService.updateById(Blog.builder().id(blogId).commentNum(commendNum+1).build());
        // 热榜加评论分+6
        redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,blogId.toString(), MarkConstant.COMMENT);
        log.info("用户{}发布了一条评论",UserContext.getUserId());
    }
    @ApiOperation(value = "修改博客赞状态")
    @PostMapping("/{blogId}/like")
    public void updateBlogLike(@PathVariable int blogId, @RequestBody @Validated LikeDTO likeDTO){
        blogService.blogPassAuthenticate(blogId);
        if(blogService.checkBlogExist(blogId)){
            switch (likeDTO.getType()){
                case OperationType.ADD_LIKE -> blogService.addBlogLike(UserContext.getUserId(), blogId);
                case OperationType.CANCEL_LIKE -> blogService.cancelBlogLike(UserContext.getUserId(), blogId);
                default -> {}
            }
        }else{
            throw new ResourceNotFoundException("博客"+MessageConstant.RES_NOT_FOUND);
        }
    }

    @ApiOperation(value = "举报评论/博客")
    @PostMapping("/accuse")
    public void accuseObject(@RequestBody @Validated AccuseDTO accuseDTO){
        // 检查是否为合法博客/评论
        if(blogService.checkAccuse(accuseDTO.getType(),accuseDTO.getObjectId())){
            Accuse accuse = new Accuse();
            BeanUtil.copyProperties(accuseDTO,accuse);
            accuse.setUserId(UserContext.getUserId());
            accuseMapper.insert(accuse);
            log.info("用户id={}，提交了简介为{}的举报",UserContext.getUserId(),accuseDTO.getBrief());
        }else {
            throw new ResourceNotFoundException(accuseDTO.getObjectId()+MessageConstant.RES_NOT_FOUND);
        }
    }
    @ApiOperation("博客页面-展示收藏夹")
    @GetMapping("/{blogId}/collections")
    public List<CollectionVO> showCollections(@PathVariable int blogId){
        return userinfoService.getCollections(UserContext.getUserId(),blogId);
    }
}
