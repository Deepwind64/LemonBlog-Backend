package com.bluemsun.lemonserver.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.*;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.enumeration.BlogStatus;
import com.bluemsun.lemoncommon.exception.InvalidArgsException;
import com.bluemsun.lemoncommon.exception.ModifyInvalidResourceException;
import com.bluemsun.lemoncommon.exception.OccupiedException;
import com.bluemsun.lemoncommon.exception.ResourceNotFoundException;
import com.bluemsun.lemoncommon.utils.JWTGenerator;
import com.bluemsun.lemonpojo.dto.AdminDTO;
import com.bluemsun.lemonpojo.dto.BanUserDTO;
import com.bluemsun.lemonpojo.dto.LabelDTO;
import com.bluemsun.lemonpojo.entity.*;
import com.bluemsun.lemonpojo.vo.AccuseVO;
import com.bluemsun.lemonpojo.vo.AdminLoginVO;
import com.bluemsun.lemonpojo.vo.BlogBasicInfoVO;
import com.bluemsun.lemonpojo.vo.UserVO;
import com.bluemsun.lemonserver.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author deepwind
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@Api("管理员相关接口")
public class AdminController {
    @Resource
    UserService userService;
    @Resource
    AdminService adminService;
    @Resource
    BlogService blogService;
    @Resource
    MessageService messageService;
    @Resource
    CommentService commentService;
    @Resource
    RedisTemplate<String,Object> redisTemplate;
    @ApiOperation("管理员登录")
    @GetMapping("/login")
    public AdminLoginVO login(@Validated AdminDTO adminDTO){
        Integer id = adminService.login(adminDTO);
        // 生成 token
        Long expireTime = System.currentTimeMillis() + EncryptionConstant.JWT_EXPIRE_TIME;
        String token = JWTGenerator.getJWT(expireTime,id.longValue(),true);
        // token 存入 redis
        redisTemplate.opsForValue().set(RedisConstant.ADMIN_TOKEN_PREFIX+id,token,1, TimeUnit.DAYS);
        return AdminLoginVO.builder()
                .userId(id)
                .token(token)
                .expireTime(expireTime)
                .build();
    }
    @ApiOperation("添加管理员")
    @PostMapping("/add")
    public void addAdmin(@RequestBody AdminDTO adminDTO){
        // name 查重
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_name",adminDTO.getName());
        if (adminService.getOne(queryWrapper)==null){
            throw new OccupiedException(MessageConstant.NAME);
        }
        // 不检查管理员密码
        adminService.save(Admin.builder()
                .name(adminDTO.getName())
                .password(SecureUtil.sha1(adminDTO.getPassword())).build());
    }
    @ApiOperation("管理员登出")
    @PostMapping("/logout")
    public void logout(){
        // 删除token
        redisTemplate.delete(UserContext.getUserId().toString());
    }
    @ApiOperation("查看所有用户")
    @GetMapping("/users")
    public IPage<UserVO> showUsers(@RequestParam int curPage){
        return adminService.getUsers(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/user/{userId}")
    public void removeAccount(@PathVariable Long userId){
        if (userService.isUserExistedById(userId)){
            throw new ModifyInvalidResourceException(MessageConstant.INVALID_RES_OPERATION);
        }
        User user = User.builder().id(userId).build();
        userService.removeById(user);
        //消除 token
        redisTemplate.delete(userId.toString());
        log.info("id: {} 已被移除", userId);
    }
    @ApiOperation("获取所有待审核博客")
    @GetMapping("/examine/blog")
    public List<BlogBasicInfoVO> getUnexaminedBlog(){
        return blogService.getUnexaminedBlog();
    }

    @ApiOperation("博客审核")
    @PutMapping("/examine/blog/{blogId}")
    public void examineBlog(@PathVariable("blogId") int blogId, @RequestParam("result") int result){
        // 检查是否存在记录
        Object record = redisTemplate.opsForHash().get(RedisConstant.MAP_BLOG_EXAMINE, String.valueOf(blogId));
        if(record!=null && (Integer)record!=0){
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
        Blog blog = blogService.getById(blogId);
        // 0为未通过/1为通过
        switch (result){
            case OperationType.PASS -> {
                // 修改博客状态
                blogService.updateById(Blog.builder()
                        .id(blogId)
                        .status(BlogStatus.PASS.getCode())
                        .build());
                // 加入最新榜单
                adminService.putIntoHotList(blogId);
                for(var label : blog.getLabel().split(",")){
                    adminService.putIntoHotLabelList(blogId,label);
                }

                // 删除redis记录
                redisTemplate.opsForHash().delete(RedisConstant.MAP_BLOG_EXAMINE,String.valueOf(blogId));
                // 发送通过通知
                String content = StrUtil.format(MessageConstant.BLOG_PASS_UNCOMPLETED, blog.getTitle());
                messageService.send(Message.builder()
                        .title(MessageConstant.BLOG_PASS)
                        .content(content)
                        .senderId(DefaultConstant.SYSTEM_USER_ID)
                        .receiverId(blog.getAutherId())
                        .createTime(new Date()).build());
            }
            case OperationType.FAIL-> {
                // 发送博客被驳回通知
                String content = StrUtil.format(MessageConstant.BLOG_REFUSE_UNCOMPLETED, blog.getTitle());
                messageService.send(Message.builder()
                        .title(MessageConstant.BLOG_REJECT)
                        .content(content)
                        .senderId(DefaultConstant.SYSTEM_USER_ID)
                        .receiverId(blog.getAutherId())
                        .createTime(new Date()).build());
            }
            default -> {
                throw new InvalidArgsException(MessageConstant.INVALID_ARGS);
            }
        }
    }
    @ApiOperation("获取所有待验证举报")
    @GetMapping("/examine/accuse")
    public IPage<AccuseVO> getAccuseIntro(@RequestParam("curPage") int curPage,@RequestParam("type") int type){
        return blogService.getAccuseIntro(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), type);
    }

    @ApiOperation("驳回举报")
    @PostMapping("/examine/accuse/{accuseId}/reject")
    public void reject(@PathVariable("accuseId") int accuseId){
        if(blogService.isAccuseExist(accuseId)){
            blogService.rejectAccuse(accuseId);
        }
    }

    @ApiOperation("封禁用户")
    @PostMapping("/examine/user/ban")
    public void banUser(@RequestBody @Validated BanUserDTO banUserDTO){
        userService.banUser(banUserDTO.getUserId(),banUserDTO.getTime(), banUserDTO.getReason());
    }
    @ApiOperation("解封用户")
    @PostMapping("/examine/user/{userId}/recover")
    public void recoverUser(@PathVariable Long userId){
        userService.recoverUser(userId);
    }
    @ApiOperation("下架博客")
    @PostMapping("/examine/blog/{blogId}/delist")
    public void delistBlog(@PathVariable Integer blogId){
        if(blogService.checkBlogExist(blogId)){
            blogService.updateById(Blog.builder().id(blogId).status(BlogStatus.UNCHECK.getCode()).build());
            Blog blog = blogService.getById(blogId);
            String title = StrUtil.format(MessageConstant.DELIST_BLOG_UNCOMPLETED,blog.getTitle());
            // 发送通知
            messageService.send(Message.builder()
                    .title(title)
                    .content("")
                    .senderId(DefaultConstant.SYSTEM_USER_ID)
                    .receiverId(blog.getAutherId())
                    .createTime(new Date()).build());
        }
    }
    @ApiOperation("删除评论")
    @PostMapping("/examine/comment/{commentId}")
    public void removeComment(@PathVariable String commentId){
        if(commentService.getById(commentId)!=null){
            Comment comment = commentService.getById(commentId);
            commentService.removeById(commentId);
            String content = StrUtil.format(MessageConstant.COMMENT_ACCUSE_REMOVE_UNCOMPLETED,comment.getContent());
            messageService.send(Message.builder()
                    .title(MessageConstant.COMMENT_ACCUSE_REMOVE_TITLE)
                    .content(content)
                    .senderId(DefaultConstant.SYSTEM_USER_ID)
                    .receiverId(comment.getUserId())
                    .createTime(new Date()).build());
        }else{
            throw new ResourceNotFoundException(MessageConstant.RES_NOT_FOUND);
        }
    }

    @ApiOperation("添加博客标签")
    @PostMapping("/column/label/add")
    public void addLabel(@RequestBody LabelDTO labelDTO){
        blogService.addLabel(labelDTO.getLabel().trim());
    }
    @ApiOperation("删除博客标签")
    @DeleteMapping ("/column/label/remove")
    public void removeLabel(@RequestBody LabelDTO labelDTO){
        blogService.removeLabel(labelDTO.getLabel().trim());
    }
}

//    @ApiOperation("发送系统通知")
//    @PostMapping("/message/system")
//    public void sendSystemMessage(@RequestBody SysMessageDTO sysMessageDTO){
//        messageService.send(Message.builder()
//                .title(MessageConstant.ADMIN_MESSAGE)
//                .content(sysMessageDTO.getContent())
//                .senderId(UserContext.getUserId())
//                .receiverId().build());
//    }