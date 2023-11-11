package com.bluemsun.lemonserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluemsun.lemoncommon.constant.DefaultConstant;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.constant.OperationType;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemoncommon.exception.InvalidArgsException;
import com.bluemsun.lemonpojo.entity.RankVO;
import com.bluemsun.lemonpojo.vo.BlogBasicInfoVO;
import com.bluemsun.lemonpojo.vo.ClassifiedColumnVO;
import com.bluemsun.lemonpojo.vo.UserinfoLiteVO;
import com.bluemsun.lemonserver.service.BlogService;
import com.bluemsun.lemonserver.service.UserinfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/index")
@Api("主页访问与管理相关接口")
public class IndexController {
    @Resource
    BlogService blogService;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Resource
    UserinfoService userinfoService;

    @ApiOperation(value = "博客搜索")
    @GetMapping("/search/blog")
    public IPage<BlogBasicInfoVO> searchBlog(@RequestParam("curPage") int curPage, @RequestParam("content") String content){
        return blogService.searchBlog(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY),content);

    }
    @ApiOperation(value = "用户搜索")
    @GetMapping("/search/user")
    public IPage<UserinfoLiteVO> searchUser(@RequestParam("curPage") int curPage, @RequestParam("content") String content){
        return blogService.searchUser(new Page<>(curPage, DefaultConstant.PAGE_CAPACITY),content);
    }
    @ApiOperation(value = "查看排行榜")
    @GetMapping("/rank")
    public List<RankVO> rank(@RequestParam("type") String type){
        List<RankVO> rankVO = new ArrayList<>();
        List<String> rankIds;
        switch (type){
            case OperationType.HOT -> rankIds = redisTemplate.opsForZSet().reverseRange(RedisConstant.ZSET_HOT_BLOG_RANK,0,DefaultConstant.RANK_CAPACITY).stream().toList();
            case OperationType.TIME -> rankIds = redisTemplate.opsForList().range(RedisConstant.LIST_TIME_BLOG_RANK, 0, DefaultConstant.RANK_CAPACITY);
            default -> {
                throw new InvalidArgsException(MessageConstant.INVALID_ORDER);
            }
        }
        assert rankIds!=null;
        // 产生返回对象
        for(int i=0;i<rankIds.size();i++ ){
            long blogId = Integer.parseInt(rankIds.get(i));
            rankVO.add(RankVO.builder()
                    .objectId(blogId)
                    .rank((long) (i+1))
                    .name(blogService.getById(blogId).getTitle()).build());
        }
        return rankVO;
    }
    @ApiOperation(value = "查看分类专栏")
    @GetMapping("/column")
    public ClassifiedColumnVO getLabelColumn(@RequestParam("label") String label, @RequestParam("type") String type, @RequestParam("curPage") int curPage){
        int startIndex = (curPage-1) * DefaultConstant.RANK_CAPACITY.intValue();
        int endIndex = startIndex + DefaultConstant.RANK_CAPACITY.intValue();
        List<RankVO> rankVO = new ArrayList<>();
        List<String> rankIds;
        long size = redisTemplate.opsForList().size(RedisConstant.LIST_LABEL);
        switch (type){
//            case OperationType.HOT -> rankIds = redisTemplate.opsForZSet().reverseRange(RedisConstant.ZSET_LABEL+label.toUpperCase(),startIndex,endIndex).stream().toList();
            case OperationType.TIME -> rankIds = redisTemplate.opsForList().range(RedisConstant.LIST_LABEL+label.toUpperCase(),startIndex,endIndex);
            default -> {
                throw new InvalidArgsException(MessageConstant.INVALID_ORDER);
            }
        }
        assert rankIds!=null;
        // 产生返回对象
        for(int i=0;i<rankIds.size();i++ ){
            long blogId = Integer.parseInt(rankIds.get(i));
            rankVO.add(RankVO.builder()
                    .objectId(blogId)
                    .rank((long) (i+1))
                    .name(blogService.getById(blogId).getTitle()).build());
        }
        return ClassifiedColumnVO.builder()
                .pages((int)Math.ceil(size * 1.0 /DefaultConstant.PAGE_CAPACITY))
                .total(size)
                .column(rankVO).build();
    }
}
