package com.bluemsun.lemonserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.DefaultConstant;
import com.bluemsun.lemoncommon.context.UserContext;
import com.bluemsun.lemonpojo.vo.BlogBasicInfoVO;
import com.bluemsun.lemonpojo.vo.ColumnVO;
import com.bluemsun.lemonserver.service.BlogService;
import com.bluemsun.lemonserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author deepwind
 */
@Slf4j
@RestController
@RequestMapping("/column")
@Api("专栏访问与管理相关接口")
public class ColumnController {
    @Resource
    BlogService blogService;
    @ApiOperation("添加专栏")
    @PostMapping("/add")
    public void addColumn(@RequestParam String name){
        blogService.addColumn(name, UserContext.getUserId());
    }
    @ApiOperation("向专栏添加博客")
    @PutMapping("/{columnId}")
    public void addColumnBlog(@PathVariable int columnId,@RequestParam int blogId){
        // 校验专栏所有权
        UserService.userAuthenticate(blogService.getColumnOwnerId(columnId));
        // 校验博客所有权
        UserService.userAuthenticate(blogService.getById(blogId).getAutherId());
        blogService.addColumnBlog(columnId, blogId, UserContext.getUserId());
    }
    @ApiOperation("移除专栏内博客")
    @DeleteMapping("/{columnId}")
    public void removeColumnBlog(@PathVariable int columnId,@RequestParam int blogId){
        UserService.userAuthenticate(blogService.getColumnOwnerId(columnId));
        UserService.userAuthenticate(blogService.getById(blogId).getAutherId());
        blogService.removeColumnBlog(columnId, blogId);
    }
    @ApiOperation("移除专栏")
    @PostMapping("/{columnId}/remove")
    public void removeColumn(@PathVariable int columnId){
        UserService.userAuthenticate(blogService.getColumnOwnerId(columnId));
        blogService.removeColumn(columnId);
    }
    @ApiOperation(value = "查看用户所有专栏",notes = "公开API")
    @GetMapping("/user/{userId}")
    public IPage<ColumnVO> getUserColumn(@PathVariable int userId, @RequestParam int curPage){
        return blogService.getUserColumn(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), userId);
    }
    @ApiOperation("查看用户专栏-添加专栏")
    @GetMapping("/user/{userId}/add-view")
    public IPage<ColumnVO> getUserColumnForAdd(@PathVariable int userId, @RequestParam("curPage") int curPage,@RequestParam("blogId") int blogId){
        return blogService.getUserColumnForAdd(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), userId, blogId);
    }
    @ApiOperation(value = "查看专栏内所有博客" ,notes = "公开API")
    @GetMapping("/{columnId}/view")
    public IPage<BlogBasicInfoVO> getColumns(@PathVariable int columnId, @RequestParam int curPage){
        return blogService.getColumns(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY), columnId);
    }
    @ApiOperation(value = "查看所有博客标签",notes = "公开api")
    @GetMapping("/label")
    public List<String> getLabels(){
        return blogService.getLabels();
    }
}
