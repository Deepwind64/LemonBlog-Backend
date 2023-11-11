package com.bluemsun.lemonserver.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.DefaultConstant;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemoncommon.exception.InvalidFileException;
import com.bluemsun.lemonpojo.dto.CollectionContentDTO;
import com.bluemsun.lemonpojo.dto.CollectionContentsDTO;
import com.bluemsun.lemonpojo.dto.UserinfoDTO;
import com.bluemsun.lemonpojo.entity.Collection;
import com.bluemsun.lemonpojo.entity.CollectionContent;
import com.bluemsun.lemonpojo.entity.Message;
import com.bluemsun.lemonpojo.entity.Userinfo;
import com.bluemsun.lemonpojo.vo.*;
import com.bluemsun.lemonserver.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

/**
 * @author deepwind
 */
@RestController
@Slf4j
@RequestMapping("/userinfo")
@Api("用户信息相关接口")
public class UserinfoController {
    @Resource
    UserinfoService userinfoService;
    @Resource
    BlogService blogService;
    @Resource
    MessageService messageService;
    @Resource
    FileService fileService;
    @ApiOperation("查看用户信息")
    @GetMapping("/{userId}")
    public UserinfoVO getUserinfo(@PathVariable("userId") Long userId){
        Userinfo userinfo = userinfoService.getByUserId(userId);
        UserinfoVO userinfoVO = new UserinfoVO();
        BeanUtil.copyProperties(userinfo,userinfoVO);
        // 字符形式存储的标签转为列表返回
        String labels = userinfo.getLabel();
        userinfoVO.setLabel(labels==null ? null: labels.split(","));
        return userinfoVO;
    }
    @ApiOperation("更新用户信息")
    @PutMapping("/{userId}/update")
    public void setUserinfo(@PathVariable("userId") Long userId, @RequestBody UserinfoDTO userinfoDTO){
        // 鉴权
        UserService.userAuthenticate(userId);

        Userinfo userinfo = userinfoService.getByUserId(userId);
        BeanUtil.copyProperties(userinfoDTO,userinfo);
        userinfo.setLabel(StrUtil.join(",", (Object) userinfoDTO.getLabel()));
        userinfoService.updateById(userinfo);
        log.info("用户 id：{}更新资料成功",userId);
    }
    @ApiOperation("查看用户头像")
    @GetMapping("/avatar/{userId}")
    public String getAvatar(@PathVariable("userId") Long userId){
        return userinfoService.getAvatarUrlByUserId(userId);
    }
    @ApiOperation("更新用户头像")
    @PutMapping("/avatar/{userId}/update")
    public String setAvatar(@PathVariable("userId") Long userId, @RequestParam("avatar") MultipartFile avatar) throws IOException {
        // 鉴权
        UserService.userAuthenticate(userId);

        if(DefaultConstant.MAX_AVATAR_SIZE < avatar.getSize()){
            throw new InvalidFileException(MessageConstant.TOO_LARGE_PICTURE_ERROR);
        };

        // 保存图片至硬盘并存图片信息入数据库
        PictureVO pictureVO = fileService.savePic(avatar, UserContext.getUserId());
        // 更新用户头像信息
        Userinfo userinfo = userinfoService.getByUserId(userId);
        userinfo.setAvatarId(pictureVO.getId());
        userinfoService.updateById(userinfo);

        log.info("用户id：{}，头像更新成功",userId);
        return pictureVO.getUrl();

    }
    // TODO 将收藏操作用redis实现
    @ApiOperation("创建收藏夹")
    @PostMapping("/{userId}/collection")
    public CollectionVO addCollection(@PathVariable Long userId,@RequestParam String name){
        UserService.userAuthenticate(userId);
        return userinfoService.addCollection(Collection.builder()
                .name(name)
                .userId(userId)
                .size(0)
                .build());
    }
    @ApiOperation("删除收藏夹")
    @DeleteMapping("/{userId}/collection")
    public void removeCollection(@PathVariable Long userId,@RequestParam int collectionId){
        UserService.userAuthenticate(userId);
        userinfoService.removeCollection(collectionId);
        log.info("用户{}已删除收藏夹{}",userId,collectionId);
    }

    @ApiOperation("用户页面-展示收藏夹")
    @GetMapping("/{userId}/collections")
    public List<CollectionVO> showCollections(@PathVariable Long userId){
        UserService.userAuthenticate(userId);
        return userinfoService.getCollections(userId);
    }
    @ApiOperation("添加博客进收藏夹")
    @PostMapping("/{userId}/collection/blog")
    public void collectBlog(@PathVariable long userId, @RequestBody @Validated CollectionContentDTO collectionContentDTO){
        UserService.userAuthenticate(userId);
        userinfoService.collectBlog(collectionContentDTO);
    }
    @ApiOperation("取消收藏博客")
    @DeleteMapping("/{userId}/collection/blog")
    public void cancelCollectBlog(@PathVariable long userId,@RequestBody CollectionContentDTO collectionContentDTO){
        UserService.userAuthenticate(userId);
        userinfoService.cancelCollectBlog(collectionContentDTO);
    }
    @ApiOperation("展示收藏夹内博客")
    @GetMapping("/{userId}/collection")
    public IPage<CollectionContentVO> showCollectionContent(@PathVariable long userId, @RequestParam("curPage") int curPage, @RequestParam("collectionId") int collectionId){
        UserService.userAuthenticate(userId);
        return userinfoService.getCollectionContent(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY),collectionId);
    }

    @ApiOperation(value = "查看个人博客列表",notes = "公开api")
    @GetMapping("/{userId}/blog")
    public IPage<BlogBasicInfoVO> getUserBlogWithoutToken(@PathVariable long userId, @RequestParam int curPage){
        return blogService.getUserBlogs(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), userId, false);
    }
    @ApiOperation(value = "查看个人博客列表")
    @GetMapping("/{userId}/blog/view")
    public IPage<BlogBasicInfoVO> getUserBlog(@PathVariable long userId, @RequestParam int curPage){
        return blogService.getUserBlogs(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), userId, userId==UserContext.getUserId());
    }
    @ApiOperation(value = "查看个人消息")
    @GetMapping("/{userId}/message")
    public IPage<Message> getUserMessage(@PathVariable long userId, @RequestParam int curPage){
        UserService.userAuthenticate(userId);
        return messageService.getUserMessage(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), userId);
    }
}
