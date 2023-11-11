package com.bluemsun.lemonserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bluemsun.lemoncommon.constant.MarkConstant;
import com.bluemsun.lemoncommon.constant.MessageConstant;
import com.bluemsun.lemoncommon.constant.RedisConstant;
import com.bluemsun.lemoncommon.constant.UrlConstant;
import com.bluemsun.lemoncommon.exception.OccupiedException;
import com.bluemsun.lemoncommon.exception.ResourceNotFoundException;
import com.bluemsun.lemonpojo.dto.CollectionContentDTO;
import com.bluemsun.lemonpojo.entity.*;
import com.bluemsun.lemonpojo.vo.CollectionContentVO;
import com.bluemsun.lemonpojo.vo.CollectionVO;
import com.bluemsun.lemonserver.dao.BlogMapper;
import com.bluemsun.lemonserver.dao.CollectionContentMapper;
import com.bluemsun.lemonserver.dao.CollectionMapper;
import com.bluemsun.lemonserver.dao.UserinfoMapper;
import com.bluemsun.lemonserver.service.PictureService;
import com.bluemsun.lemonserver.service.UserinfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author deepwind
 */
@Slf4j
@Service
public class UserinfoServiceImpl extends ServiceImpl<UserinfoMapper,Userinfo> implements UserinfoService {
    @Resource
    UserinfoMapper userinfoMapper;
    @Resource
    PictureService pictureService;
    @Resource
    RedisTemplate<String,String> redisTemplate;
    @Resource
    CollectionMapper collectionMapper;
    @Resource
    CollectionContentMapper collectionContentMapper;
    @Resource
    BlogMapper blogMapper;
    @Value("${server.location}")
    public String serverLocation;
    @Override
    public List<String> splitLabel(String label){
        return new ArrayList<String>(List.of());
    }

    @Override
    public Userinfo getByUserId(Long userId) {
        QueryWrapper<Userinfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return userinfoMapper.selectOne(queryWrapper);
    }

    @Override
    public String getAvatarUrlByUserId(Long userId) {
        Picture picture = pictureService.getById(getByUserId(userId).getAvatarId());
        return  serverLocation + UrlConstant.PICTURE_LOCATION + picture.getName() + "." +picture.getType();
    }

    @Override
    public List<CollectionVO> getCollections(Long userId) {
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // 转换为VO
        List<CollectionVO> voList = new ArrayList<>();
        collectionMapper.selectList(queryWrapper).forEach(item->{
            CollectionVO collectionVO = new CollectionVO();
            BeanUtil.copyProperties(item, collectionVO);
            voList.add(collectionVO);
            }
        );
        return voList;
    }
    @Override
    public List<CollectionVO> getCollections(Long userId, int blogId) {
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // 转换为VO
        List<CollectionVO> voList = new ArrayList<>();
        collectionMapper.selectList(queryWrapper).forEach(item->{
                    CollectionVO collectionVO = new CollectionVO();
                    BeanUtil.copyProperties(item, collectionVO);
                    // 判断收藏夹中是否包含当前博客对象
                    collectionVO.setInclude(getCollectionContent(blogId, item.getId())!=null);
                    voList.add(collectionVO);
                }
        );
        return voList;
    }

    @Override
    public CollectionVO addCollection(Collection collection) {
        if(isCollectionExisted(collection.getName())){
            throw new OccupiedException(collection.getName());
        }
        CollectionVO collectionVO = new CollectionVO();
        collectionMapper.insert(collection);
        BeanUtil.copyProperties(collection,collectionVO);
        log.info("用户{}，添加收藏夹: {}",collection.getUserId(),collection.getName());
        return collectionVO;
    }

    @Override
    public void removeCollection(int collectionId) {
        if(!isCollectionExisted(collectionId)){
            throw new ResourceNotFoundException(collectionId+MessageConstant.RES_NOT_FOUND);
        }
        collectionMapper.deleteById(collectionId);
    }

    @Override
    public void collectBlog(CollectionContentDTO collectionContentDTO) {
        int collectionId = collectionContentDTO.getCollectionId();
        // 仅当未收藏时进行收藏
        if(getCollectionContent(collectionContentDTO)==null){
            // 保存收藏记录
            collectionContentMapper.insert(CollectionContent.builder().blogId(collectionContentDTO.getBlogId()).collectionId(collectionId).build());
            // 热榜加收藏分+10
            redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,collectionContentDTO.getBlogId().toString(), MarkConstant.COLLECT);
            // 收藏夹大小+1
            int size = collectionMapper.selectById(collectionId).getSize();
            collectionMapper.updateById(Collection.builder().id(collectionId).size(size+1).build());
        }
    }

    @Override
    public void cancelCollectBlog(CollectionContentDTO collectionContentDTO) {
        CollectionContent collectionContent = getCollectionContent(collectionContentDTO);
        // 仅在存在时删除
        if(collectionContent!=null){
            collectionContentMapper.deleteById(collectionContent);
            // 热榜减收藏分-10
            redisTemplate.opsForZSet().incrementScore(RedisConstant.ZSET_HOT_BLOG_RANK,collectionContentDTO.getBlogId().toString(), -1 * MarkConstant.COLLECT);
            // 收藏夹大小-1
            int collectionId = collectionContent.getCollectionId();
            int size = collectionMapper.selectById(collectionId).getSize();
            collectionMapper.updateById(Collection.builder().id(collectionId).size(size-1).build());
        }
    }

    /**
     * 检查收藏中是否存在对应博客记录，是则返回收藏夹内容
     * @param collectionContentDTO
     * @return
     */
    private CollectionContent getCollectionContent(CollectionContentDTO collectionContentDTO){
        QueryWrapper<CollectionContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("blog_id",collectionContentDTO.getBlogId());
        queryWrapper.eq("collection_id",collectionContentDTO.getCollectionId());
        return collectionContentMapper.selectOne(queryWrapper);
    }
    private CollectionContent getCollectionContent(int blogId, int collectionId){
        QueryWrapper<CollectionContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("blog_id",blogId);
        queryWrapper.eq("collection_id",collectionId);
        return collectionContentMapper.selectOne(queryWrapper);
    }

    @Override
    public IPage<CollectionContentVO> getCollectionContent(Page<Blog> page, int collectionId) {
        Page<Blog> blogs = blogMapper.getCollectionContent(page,collectionId);
        return blogs.convert(result -> CollectionContentVO.builder()
                .blogId(result.getId())
                .blogTitle(result.getTitle()).build());
    }
    private boolean isCollectionExisted(int collectionId){
        return collectionMapper.selectById(collectionId)!=null;
    }
    private boolean isCollectionExisted(String collectionName){
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",collectionName);
        return collectionMapper.exists(queryWrapper);
    }
}
